/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admincentral.model;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.tree.MenuItem;
import info.magnolia.module.admincentral.tree.TreeDefinition;
import info.magnolia.module.admincentral.tree.TreeRegistry;
import info.magnolia.module.admincentral.tree.action.TreeAction;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import com.vaadin.terminal.ExternalResource;


/**
 * @author pbaerfuss
 * @version $Id$
 *
 */
public class UIModel {


    public List<TreeAction> getActionsForItem(String treeName, Item item){

        TreeDefinition treeDefinition;
        try {
            treeDefinition = TreeRegistry.getInstance().getTree(treeName);
        }
        catch (RepositoryException e) {
            throw new IllegalStateException(e);
        }

        ArrayList<TreeAction> actions = new ArrayList<TreeAction>();
        for (MenuItem mi : treeDefinition.getContextMenuItems()) {
            TreeAction action = mi.getAction();
            action.setName(mi.getName());

            if (!action.isAvailable(item))
                continue;

            action.setCaption(mi.getLabel());
            action.setIcon(new ExternalResource(MgnlContext.getContextPath() + mi.getIcon()));
            actions.add(action);

        }
        return actions;
    }



}
