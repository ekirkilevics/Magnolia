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
package info.magnolia.ui.admincentral.tree.action;

import java.util.HashMap;
import java.util.Map;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;

import javax.jcr.Item;


/**
 * Creates an action based on an {@link ActionDefinition}.
 */
public class EditWorkspaceActionFactory {

    private ComponentProvider componentProvider;
    private Map<Class<? extends ActionDefinition>, Class<? extends Action>> mapping = new HashMap<Class<? extends ActionDefinition>, Class<? extends Action>>();

    public EditWorkspaceActionFactory(ComponentProvider componentProvider) {
        // FIXME don't cast
        this.componentProvider = componentProvider;

        // TODO this should be configured
        mapping.put(AddNodeActionDefinition.class, AddNodeAction.class);
        mapping.put(AddPageActionDefinition.class, AddPageAction.class);
        mapping.put(AddPropertyActionDefinition.class, AddPropertyAction.class);
        mapping.put(DeleteItemActionDefinition.class, DeleteItemAction.class);

        mapping.put(OpenEditDialogActionDefinition.class, OpenEditDialogAction.class);
        mapping.put(OpenPageActionDefinition.class, OpenPageAction.class);
    }

    public Action createAction(ActionDefinition actionDefinition, Item item) {
        final Class<? extends Action> actionClass = mapping.get(actionDefinition.getClass());
        return componentProvider.newInstance(actionClass, actionDefinition, item);
    }

}