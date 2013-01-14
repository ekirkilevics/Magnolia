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


import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.AccessManagerImpl;
import info.magnolia.cms.security.Permission;

import java.util.List;
import java.util.Map;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.cache.GrowingLRUMap;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.security.authorization.AbstractCompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.PrivilegeManagerImpl;
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
 * Permission based on user ACL for given workspace. Caches the result of resolving paths from ids, the caching
 * implementation based {@link org.apache.jackrabbit.core.security.authorization.principalbased.ACLProvider.CompiledPermissionImpl}.
 */
public class DefaultACLBasedPermissions extends AbstractCompiledPermissions {

    protected final AccessManager ami = new AccessManagerImpl();
    @SuppressWarnings("unchecked")
    protected final Map<ItemId, Boolean> readCache = new GrowingLRUMap(1024, 5000);
    protected final Object monitor = new Object();
    protected SessionImpl session;

    private static final Logger log = LoggerFactory.getLogger(DefaultACLBasedPermissions.class);

    protected final long permissionMapping[][] = {
            {org.apache.jackrabbit.core.security.authorization.Permission.READ, Permission.READ},
            {org.apache.jackrabbit.core.security.authorization.Permission.SET_PROPERTY, Permission.SET},
            {org.apache.jackrabbit.core.security.authorization.Permission.ADD_NODE, Permission.ADD},
            {org.apache.jackrabbit.core.security.authorization.Permission.REMOVE_NODE, Permission.REMOVE},
            {org.apache.jackrabbit.core.security.authorization.Permission.REMOVE_PROPERTY, Permission.REMOVE},
            {org.apache.jackrabbit.core.security.authorization.Permission.READ_AC, Permission.EXECUTE},
            {org.apache.jackrabbit.core.security.authorization.Permission.MODIFY_AC, Permission.EXECUTE},
            {org.apache.jackrabbit.core.security.authorization.Permission.NODE_TYPE_MNGMT, Permission.ADD},
            {org.apache.jackrabbit.core.security.authorization.Permission.VERSION_MNGMT, Permission.EXECUTE},
            {org.apache.jackrabbit.core.security.authorization.Permission.LOCK_MNGMT, Permission.EXECUTE},
            {org.apache.jackrabbit.core.security.authorization.Permission.LIFECYCLE_MNGMT, Permission.EXECUTE},
            {org.apache.jackrabbit.core.security.authorization.Permission.RETENTION_MNGMT, Permission.EXECUTE},
        };

    protected long convertJackrabbitPermissionsToMagnoliaPermissions(long jackRabbitPermissions) {
        long magnoliaPermissions = 0;
        for (long[] mapping : permissionMapping) {
            long jackrabbitPermission = mapping[0];
            long magnoliaPermission = mapping[1];
            if ((jackRabbitPermissions & jackrabbitPermission) != 0) {
                magnoliaPermissions = magnoliaPermissions | magnoliaPermission;
            }
        }
        return magnoliaPermissions;
    }


    /**
     * Used to convert a jackrabbit Path abstraction into a path string with slashes and no namespaces.
     */
    protected final PathResolver pathResolver = new CachingPathResolver(new ParsingPathResolver(null, new NameResolver() {

        @Override
        public Name getQName(String name) throws IllegalNameException, NamespaceException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getJCRName(Name name) throws NamespaceException {
            return name.getLocalName();
        }
    }));

    public DefaultACLBasedPermissions(List<Permission> permissions, SessionImpl systemSession, Map<?, ?> configuration) {
        // TODO: use provider instead of fixed impl
        ami.setPermissionList(permissions);
        this.session = systemSession;
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

        String path = pathResolver.getJCRPath(itemPath);
        log.debug("Read request for " + path + " :: " + itemId);
        return ami.isGranted(path, Permission.READ);
    }

    @Override
    protected Result buildResult(Path absPath) throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Result getResult(Path absPath) throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean grants(Path absPath, int permissions) throws RepositoryException {
        long magnoliaPermissions = convertJackrabbitPermissionsToMagnoliaPermissions(permissions);
        return ami.isGranted(pathResolver.getJCRPath(absPath), magnoliaPermissions);
    }

    @Override
    public int getPrivileges(Path absPath) throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Result buildRepositoryResult() throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected PrivilegeManagerImpl getPrivilegeManagerImpl() throws RepositoryException {
        throw new UnsupportedOperationException();
    }

}
