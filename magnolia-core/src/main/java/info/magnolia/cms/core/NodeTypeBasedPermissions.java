/**
 * This file Copyright (c) 2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.core;

import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.conversion.CachingPathResolver;
import org.apache.jackrabbit.spi.commons.conversion.IllegalNameException;
import org.apache.jackrabbit.spi.commons.conversion.NameResolver;
import org.apache.jackrabbit.spi.commons.conversion.ParsingPathResolver;
import org.apache.jackrabbit.spi.commons.conversion.PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Permissions are retrieved from requested node or from its ancestor, if the node isn't one of valid node types specified via constructor.
 * Permission based on user ACL for given workspace. Caches the result of resolving paths from ids, the caching
 * implementation based {@link org.apache.jackrabbit.core.security.authorization.principalbased.ACLProvider.CompiledPermissionImpl}.
 */
public class NodeTypeBasedPermissions extends DefaultACLBasedPermissions {

    private final Set<String> allowedNodeTypes = new HashSet<String>();

    private static final Logger log = LoggerFactory.getLogger(NodeTypeBasedPermissions.class);

    /**
     * Used to convert a jackrabbit Path abstraction into a path string with slashes and no namespaces.
     */
    private final PathResolver pathResolver = new CachingPathResolver(new ParsingPathResolver(null, new NameResolver() {

        @Override
        public Name getQName(String name) throws IllegalNameException, NamespaceException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getJCRName(Name name) throws NamespaceException {
            return name.getLocalName();
        }
    }));

    /**
     * Constructor.
     * @param permissions list of permissions
     * @param session workspace session
     * @param configuration AccessControlProvider configuration, parameters from workspace.xml, in this class for obtaining noTypes parameter
     */
    public NodeTypeBasedPermissions(List<Permission> permissions, SessionImpl session, Map<?, ?> configuration) {
        super(permissions,session, configuration);
        String nodeTypes = (String)configuration.get("nodeTypes");

        if (nodeTypes != null) {
            String[] splittedTypes = nodeTypes.split(",");
            for (String type: splittedTypes) {
                    allowedNodeTypes.add(type);
            }
        }
    }

    @Override
    public boolean canRead(Path itemPath, ItemId itemId) throws RepositoryException {

        if ((itemId != null && "cafebabe-cafe-babe-cafe-babecafebabe".equals(itemId.toString())) || (itemPath != null && "/".equals(itemPath.toString()))) {
            // quick check - allow access to root to all like in old mgnl security
            return true;
        }

        if (itemPath == null) {

            // we deal only with permissions on nodes
            if (!itemId.denotesNode()) {
                itemId = ((PropertyId)itemId).getParentId();
            }

            synchronized (monitor) {

                if (readCache.containsKey(itemId)) {
                    return readCache.get(itemId);
                }

                itemPath = session.getHierarchyManager().getPath(itemId);
                boolean canRead = canRead(itemPath, itemId);
                readCache.put(itemId, canRead);
                return canRead;
            }
        }

        String originalPath = pathResolver.getJCRPath(itemPath);
        int emersion = getEmersion(originalPath);
        String path = pathResolver.getJCRPath(itemPath.getAncestor(emersion));

        log.debug("Read request for " + originalPath + " :: " + itemId + ". The permissions will be retrieved from ancestor path: " + path + ".");
        return ami.isGranted(path, Permission.READ);
    }

    @Override
    public boolean grants(Path itemPath, int permissions) throws RepositoryException {

        long magnoliaPermissions = convertJackrabbitPermissionsToMagnoliaPermissions(permissions);
        String originalPath = pathResolver.getJCRPath(itemPath);
        int emersion = getEmersion(originalPath);
        String path = pathResolver.getJCRPath(itemPath.getAncestor(emersion));

        log.debug(PermissionImpl.getPermissionAsName(permissions) + "permission request for " + originalPath +
                ". The permissions will be retrieved from ancestor path: " + path + ".");
        return ami.isGranted(path, magnoliaPermissions);
    }

    private int getEmersion(String originalPath) throws AccessDeniedException, RepositoryException {
        int emersion = 0;

        if (session.nodeExists(originalPath)) {
            Node node = session.getNode(originalPath);
            try {
                while (!isAllowedNodeType(node)) {
                    node = node.getParent();
                    emersion++;
                }
            } catch (ItemNotFoundException e) {
                return 0; //node with an allowed type wasn't find, behave like DefaultACLBasedPermissions
            }
        }
        return emersion;
    }

    private boolean isAllowedNodeType(Node node) throws RepositoryException {
        if (allowedNodeTypes.isEmpty()) {
            return true;  //allowed node types aren't specified, behave like DefaultACLBasedPermissions
        }
        for(String nodeType: allowedNodeTypes) {
            if (node.isNodeType(nodeType)) {
                return true;
            }
        }
        return false;
    }
}
