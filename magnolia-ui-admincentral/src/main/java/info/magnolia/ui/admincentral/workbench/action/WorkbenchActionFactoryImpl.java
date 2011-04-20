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
package info.magnolia.ui.admincentral.workbench.action;

import java.util.ArrayList;
import java.util.List;
import javax.jcr.Item;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.jcr.view.builder.DefinitionToImplementationMapping;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.builder.FactoryBase;

/**
 * Creates an action based on an {@link info.magnolia.ui.model.action.ActionDefinition}.
 */
public class WorkbenchActionFactoryImpl extends FactoryBase<ActionDefinition, Action> implements WorkbenchActionFactory {

    private List<DefinitionToImplementationMapping<ActionDefinition, Action>> definitionToImplementationMappings = new ArrayList<DefinitionToImplementationMapping<ActionDefinition, Action>>();

    public WorkbenchActionFactoryImpl(ComponentProvider componentProvider) {
        super(componentProvider);
    }

    public List<DefinitionToImplementationMapping<ActionDefinition, Action>> getDefinitionToImplementationMappings() {
        return this.definitionToImplementationMappings;
    }

    public void setDefinitionToImplementationMappings(List<DefinitionToImplementationMapping<ActionDefinition, Action>> definitionToImplementationMappings) {
        this.definitionToImplementationMappings = definitionToImplementationMappings;
        for (DefinitionToImplementationMapping<ActionDefinition, Action> definitionToImplementationMapping : definitionToImplementationMappings) {
            addDefinitionToImplementationMapping(definitionToImplementationMapping);
        }
    }

    public void addDefinitionToImplementationMapping(DefinitionToImplementationMapping<ActionDefinition, Action> mapping) {
        addMapping(mapping.getDefinition(), mapping.getImplementation());
    }

    public Action createAction(ActionDefinition actionDefinition, Item item) {
        return create(actionDefinition, item);
    }

}
