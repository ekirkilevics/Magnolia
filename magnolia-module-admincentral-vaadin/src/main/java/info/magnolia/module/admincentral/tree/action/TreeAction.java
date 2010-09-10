/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.tree.action;

import com.vaadin.event.Action;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.tree.TreeDefinition;
import info.magnolia.module.admincentral.views.AbstractTreeTableView;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;

/**
 * Base class for all tree actions.
 */
public abstract class TreeAction extends Action {

    public TreeAction() {
        super(null);
    }

    public boolean isAvailable(Content content, NodeData nodeData) {
        return true;
    }

    public void handleAction(AbstractTreeTableView treeTable, TreeDefinition treeDefinition, Object sender, Object target) throws RepositoryException {

        String itemId = (String) target;

        // TODO what about itemId==null ?

        if (itemId.indexOf('@') == -1) {
            Content content = MgnlContext.getHierarchyManager(treeDefinition.getRepository()).getContent(itemId);

            handleAction(treeTable, content);

        } else {
            String uuid = StringUtils.substringBefore(itemId, "@");
            String nodeDataName = StringUtils.substringAfter(itemId, "@");
            Content content = MgnlContext.getHierarchyManager(treeDefinition.getRepository()).getContentByUUID(uuid);

            NodeData nodeData = content.getNodeData(nodeDataName);

            handleAction(treeTable, content, nodeData);
        }
    }

    protected void handleAction(AbstractTreeTableView treeTable, Content content, NodeData nodeData) {
        System.out.println(content.getHandle() + " @ " + nodeData.getName());
    }

    protected void handleAction(AbstractTreeTableView treeTable, Content content) {
        System.out.println(content.getHandle());
    }
}
