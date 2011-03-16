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
package info.magnolia.ui.admincentral.tree.builder;

import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.column.LabelColumn;
import info.magnolia.ui.admincentral.column.MetaDataColumn;
import info.magnolia.ui.admincentral.column.NodeDataColumn;
import info.magnolia.ui.admincentral.column.NodeDataTypeColumn;
import info.magnolia.ui.admincentral.column.NodeDataValueColumn;
import info.magnolia.ui.admincentral.column.StatusColumn;
import info.magnolia.ui.admincentral.column.TemplateColumn;
import info.magnolia.ui.model.tree.definition.ColumnDefinition;
import info.magnolia.ui.model.tree.definition.LabelColumnDefinition;
import info.magnolia.ui.model.tree.definition.MetaDataColumnDefinition;
import info.magnolia.ui.model.tree.definition.NodeDataColumnDefinition;
import info.magnolia.ui.model.tree.definition.NodeDataTypeColumnDefinition;
import info.magnolia.ui.model.tree.definition.NodeDataValueColumnDefinition;
import info.magnolia.ui.model.tree.definition.StatusColumnDefinition;
import info.magnolia.ui.model.tree.definition.TemplateColumnDefinition;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Vaadin specific builder.
 *
 * TODO: Builder should be configurable.
 * TODO: Naming - this is actually not tree-dependant
 */
public class VaadinTreeBuilder implements TreeBuilder {

    private Map<Class<?>, Class<?>> definitionToColumnRegistry = new LinkedHashMap<Class<?>, Class<?>>();

    public VaadinTreeBuilder() {
        register(LabelColumnDefinition.class, LabelColumn.class);
        register(TemplateColumnDefinition.class, TemplateColumn.class);
        register(StatusColumnDefinition.class, StatusColumn.class);
        register(NodeDataColumnDefinition.class, NodeDataColumn.class);
        register(NodeDataValueColumnDefinition.class, NodeDataValueColumn.class);
        register(NodeDataTypeColumnDefinition.class, NodeDataTypeColumn.class);
        register(MetaDataColumnDefinition.class, MetaDataColumn.class);
    }

    public void register(Class<? extends ColumnDefinition> columnDef, Class<? extends Column<?, ?>> classOfColumn) {
        this.definitionToColumnRegistry.put(columnDef, classOfColumn);
    }

    public Column<?, ?> createTreeColumn(ColumnDefinition definition) {
        Class<?> classOfColumn = this.definitionToColumnRegistry.get(definition.getClass());
        if (classOfColumn == null) {
            throw new IllegalArgumentException("No Column-Type registered for definition of type "
                    + definition.getClass());
        }
        try {
            Constructor<?> columnConstructor = classOfColumn.getConstructor(definition.getClass());
            Column<?, ?> column = (Column<?, ?>) columnConstructor.newInstance(definition);
            return column;
        } catch (Exception e) {
            throw new RuntimeException("Error trying to create instance of type " + classOfColumn.getName(), e);
        }
    }
}
