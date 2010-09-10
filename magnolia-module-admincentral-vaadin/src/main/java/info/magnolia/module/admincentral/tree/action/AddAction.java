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

import info.magnolia.cms.core.Content;
import info.magnolia.module.admincentral.tree.container.NodeItem;
import info.magnolia.module.admincentral.views.AbstractTreeTableView;

import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;

import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.data.Item;

/**
 * Tree action for adding a page to the website repository.
 *
 * TODO this class is a mock up that only adds an item to the tree table. It will need to be changed into accessing the JCR.
 */
public class AddAction extends TreeAction {

    @Override
    protected void handleAction(AbstractTreeTableView treeTableView, Content content) {

        TreeTable treeTable = treeTableView.getTreeTable();

        String bogusUuid = "/someNewPath" + RandomStringUtils.randomNumeric(12);

        Item item = treeTable.addItem(bogusUuid);
        treeTable.setParent(((NodeItem)item).getWrappedContent().getHandle(), content.getHandle());

        item.getItemProperty("Page").setValue("untitled");
        item.getItemProperty("Status").setValue(0);
        item.getItemProperty("Mod. date").setValue(new Date());
    }
}
