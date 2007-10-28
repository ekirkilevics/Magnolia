/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
import java.util.Iterator;
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

    static Set collectPropertyNames(Content rootNode, String subnodeName, String repositoryName, boolean isDeep) {
        final SortedSet set = new TreeSet(String.CASE_INSENSITIVE_ORDER);
        try {
            final Content node = rootNode.getContent(subnodeName);
            collectPropertyNames(node, repositoryName, set, isDeep);
        } catch (PathNotFoundException e) {
            log.warn("{} does not have any {}", rootNode.getHandle(), repositoryName);
        } catch (Throwable t) {
            log.error("Failed to read " + repositoryName, t);
        }
        return set;
    }

    static void collectPropertyNames(Content node, String repositoryName, Collection set, boolean isDeep) throws Throwable {
        Collection c = node.getNodeDataCollection();
        Iterator it = c.iterator();
        NodeData nd = null;
        while (it.hasNext()) {
            nd = (NodeData) it.next();
            String uuid = nd.getString();
            try {
                final HierarchyManager hierarchyManager = getSystemHierarchyManager(repositoryName);
                Content targetNode = hierarchyManager.getContentByUUID(uuid);
                set.add(targetNode.getName());
                if (isDeep && targetNode.hasContent("groups")) {
                    collectPropertyNames(targetNode.getContent("groups"), repositoryName, set, true);
                }
            }
            catch (ItemNotFoundException t) {
                String path = node.getHandle();
                if (nd != null) {
                    path = nd.getHandle();
                }
                // todo: why we are using UUIDs here? shouldn't be better to use group names, since uuids can change???
                log.warn("Can't find {} node by UUID {} referred by node {}", new Object[]{ repositoryName, t.getMessage(), path});
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
