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
package info.magnolia.ui.admincentral.tree.action;

import info.magnolia.jcr.util.JCRUtil;
import info.magnolia.ui.admincentral.tree.container.ContainerItemId;
import info.magnolia.ui.admincentral.tree.view.JcrBrowser;
import info.magnolia.ui.model.command.Command;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Action for creating a new property.
 */
public class AddPropertyCommand extends Command {

    @Override
    public boolean isAvailable(Item item) {
        return item instanceof Node;
    }

    @Override
    public void execute(Item item) throws RepositoryException {
        if (item instanceof Node) {
            Node node = (Node) item;

            String name = JCRUtil.getUniqueLabel(node, "untitled");
            Property property = node.setProperty(name, "");
            node.getSession().save();
//
//            if (jcrBrowser != null)
//            jcrBrowser.addItem(new ContainerItemId(property));
//            if (jcrBrowser != null)
//            jcrBrowser.setCollapsed(new ContainerItemId(item), false);
        }
    }
}
