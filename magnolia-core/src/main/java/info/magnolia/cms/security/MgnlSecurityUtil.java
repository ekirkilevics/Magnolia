/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.cms.security;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.MgnlContext;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Utility methods for magnolia/jcr based security managers.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
class MgnlSecurityUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MgnlSecurityUtil.class);

    static Set<String> collectPropertyNames(Content rootNode, String subnodeName, String repositoryName, boolean isDeep) {
        final SortedSet<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        try {
            final Content node = rootNode.getContent(subnodeName);
            collectPropertyNames(node, repositoryName, set, isDeep);
        } catch (PathNotFoundException e) {
            log.debug("{} does not have any {}", rootNode.getHandle(), repositoryName);
        } catch (Throwable t) {
            log.error("Failed to read " + repositoryName, t);
        }
        return set;
    }

    static void collectPropertyNames(Content node, String repositoryName, Collection<String> set, boolean isDeep) throws Throwable {
        final Collection<NodeData> c = node.getNodeDataCollection();
        for (NodeData nd : c) {
            final String uuid = nd.getString();
            try {
                final HierarchyManager hierarchyManager = getSystemHierarchyManager(repositoryName);
                final Content targetNode = hierarchyManager.getContentByUUID(uuid);
                set.add(targetNode.getName());
                if (isDeep && targetNode.hasContent("groups")) {
                    collectPropertyNames(targetNode.getContent("groups"), repositoryName, set, true);
                }
            }
            catch (ItemNotFoundException t) {
                final String path = nd.getHandle();
                // todo: why we are using UUIDs here? shouldn't be better to use group names, since uuids can change???
                log.warn("Can't find {} node by UUID {} referred by node {}", new Object[]{repositoryName, t.getMessage(), path});
                log.debug("Failed while reading node by UUID", t);
                // we continue since it can happen that target node is removed
                // - UUID's are kept as simple strings thus have no referential integrity
            }
        }
    }

    static HierarchyManager getContextHierarchyManager(String repositoryId) {
        return MgnlContext.getHierarchyManager(repositoryId);
    }

    static HierarchyManager getSystemHierarchyManager(String repositoryName) {
        return MgnlContext.getSystemContext().getHierarchyManager(repositoryName);
    }
}
