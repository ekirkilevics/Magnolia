/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.jcr.util;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.JCRSessionOp;
import info.magnolia.cms.util.JCRPropertiesFilteringNodeWrapper;
import info.magnolia.context.MgnlContext;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various utility methods to collect data from JCR repository.
 * @author had
 * @version $Id: $
 */
public class JCRUtil {

    private static final Logger log = LoggerFactory.getLogger(JCRUtil.class);

    /**
     * Collects all property names of given type, sorting them (case insensitive) and removing duplicates in the process.
     */
    public static Set<String> collectUniquePropertyNames(Node rootNode, String subnodeName, String repositoryName, boolean isDeep) {
        final SortedSet<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        String path = null;
        try {
            path = rootNode.getPath();
            final Node node = rootNode.getNode(subnodeName);
            collectUniquePropertyNames(node, repositoryName, subnodeName, set, isDeep);collectUniquePropertyNames(rootNode.getNode(subnodeName), repositoryName, subnodeName, set, isDeep);
        } catch (PathNotFoundException e) {
            log.debug("{} does not have any {}", path, repositoryName);
        } catch (Throwable t) {
            log.error("Failed to read " + path + " or sub node " + subnodeName + " in repository " + repositoryName, t);
        }
        return set;
    }

    static void collectUniquePropertyNames(final Node node, final String repositoryName, final String subnodeName, final Collection<String> set, final boolean isDeep) throws RepositoryException {
        MgnlContext.doInSystemContext(new JCRSessionOp<Void>(repositoryName) {

            @Override
            public Void exec(Session session) throws RepositoryException {
                for (PropertyIterator props = node.getProperties(); props.hasNext();) {
                    Property property = props.nextProperty();
                    if (property.getName().startsWith("jcr:")) {
                        continue;
                    }
                    final String uuid = property.getString();
                    try {
                        final Node targetNode = session.getNodeByIdentifier(uuid);
                        set.add(targetNode.getName());
                        if (isDeep && targetNode.hasNode(subnodeName)) {
                            collectUniquePropertyNames(targetNode.getNode(subnodeName), repositoryName, subnodeName, set, true);
                        }
                    }
                    catch (ItemNotFoundException t) {
                        final String path = property.getPath();
                        // TODO: why we are using UUIDs here? shouldn't be better to use group names, since uuids can change???
                        log.warn("Can't find {} node by UUID {} referred by node {}", new Object[]{repositoryName, t.getMessage(), path});
                        log.debug("Failed while reading node by UUID", t);
                        // we continue since it can happen that target node is removed
                        // - UUID's are kept as simple strings thus have no referential integrity
                    }
                }
                return null;
            }});
    }

    /**
     * from default content.
     */
    public static boolean hasMixin(Node node, String mixinName) throws RepositoryException {
        if (StringUtils.isBlank(mixinName)) {
            throw new IllegalArgumentException("Mixin name can't be empty.");
        }
        for (NodeType type : node.getMixinNodeTypes()) {
            if (mixinName.equals(type.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * from default content.
     */
    public static String getNodeTypeName(Node node) throws RepositoryException {
        if (node instanceof JCRPropertiesFilteringNodeWrapper) {
            node = ((JCRPropertiesFilteringNodeWrapper) node).deepUnwrap(JCRPropertiesFilteringNodeWrapper.class);
        }

        if (node.hasProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE)) {
            return node.getProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE).getString();
        }
        return node.getProperty(ItemType.JCR_PRIMARY_TYPE).getString();
    }

}