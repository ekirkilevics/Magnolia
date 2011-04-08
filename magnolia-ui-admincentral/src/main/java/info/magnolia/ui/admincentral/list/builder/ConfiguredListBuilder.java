/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.ui.admincentral.list.builder;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.list.view.ListViewImpl;
import info.magnolia.ui.admincentral.tree.action.WorkbenchActionFactory;
import info.magnolia.ui.admincentral.tree.builder.DefinitionToImplementationMapping;
import info.magnolia.ui.admincentral.tree.model.TreeModel;
import info.magnolia.ui.admincentral.tree.view.JcrView;
import info.magnolia.ui.model.builder.FactoryBase;
import info.magnolia.ui.model.column.definition.ColumnDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.model.workbench.registry.WorkbenchRegistry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ListBuilder configured via content to bean.
 * TODO: how much can be extracted in a common base class to be extended by both Tree- and List builders?
 */
public class ConfiguredListBuilder extends FactoryBase<ColumnDefinition, Column<ColumnDefinition>> implements ListBuilder, Serializable {

    /**
     * List as retrieved out of JCR-config (via Content2Bean).
     */
    private List<DefinitionToImplementationMapping<ColumnDefinition, Column<ColumnDefinition>>> definitionToImplementationMappings = new ArrayList<DefinitionToImplementationMapping<ColumnDefinition, Column<ColumnDefinition>>>();

    private ComponentProvider componentProvider;
    private WorkbenchActionFactory workbenchActionFactory;

    // TODO: why is WorkbenchRegistry handed over here?
    public ConfiguredListBuilder(ComponentProvider componentProvider, WorkbenchRegistry workbenchRegistry, WorkbenchActionFactory workbenchActionFactory) {
        super(componentProvider);
        this.componentProvider = componentProvider;
        this.workbenchActionFactory = workbenchActionFactory;
    }

    public List<DefinitionToImplementationMapping<ColumnDefinition, Column<ColumnDefinition>>> getDefinitionToImplementationMappings() {
        return this.definitionToImplementationMappings;
    }

    public void setDefinitionToImplementationMappings(List<DefinitionToImplementationMapping<ColumnDefinition, Column<ColumnDefinition>>> definitionToImplementationMappings) {
        this.definitionToImplementationMappings = definitionToImplementationMappings;
        for (DefinitionToImplementationMapping<ColumnDefinition, Column<ColumnDefinition>> definitionToImplementationMapping : definitionToImplementationMappings) {
            addDefinitionToImplementationMapping(definitionToImplementationMapping);
        }
    }

    public void addDefinitionToImplementationMapping(DefinitionToImplementationMapping<ColumnDefinition, Column<ColumnDefinition>> mapping) {
        addMapping(mapping.getDefinition(), mapping.getImplementation());
    }

    public Column<ColumnDefinition> createTreeColumn(ColumnDefinition definition) {
        return create(definition);
    }

    public JcrView build(WorkbenchDefinition workbenchDefinition) {
        Map<String, Column<?>> columns = new LinkedHashMap<String, Column<?>>();
        for (ColumnDefinition columnDefinition : workbenchDefinition.getColumns()) {
            Column<?> column = createTreeColumn(columnDefinition);
            // only add if not null - null meaning there's no definitionToImplementationMapping defined for that column.
            if (column != null) {
                columns.put(columnDefinition.getName(), column);
            }
        }

        // FIXME the model should be set by the presenter
        TreeModel treeModel = new TreeModel(workbenchDefinition, columns, workbenchActionFactory);
        return componentProvider.newInstance(ListViewImpl.class, workbenchDefinition, treeModel);
    }
}
