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
import info.magnolia.ui.model.tree.definition.LabelColumnDefinition;
import info.magnolia.ui.model.tree.definition.MetaDataColumnDefinition;
import info.magnolia.ui.model.tree.definition.NodeDataColumnDefinition;
import info.magnolia.ui.model.tree.definition.NodeDataTypeColumnDefinition;
import info.magnolia.ui.model.tree.definition.NodeDataValueColumnDefinition;
import info.magnolia.ui.model.tree.definition.StatusColumnDefinition;
import info.magnolia.ui.model.tree.definition.TemplateColumnDefinition;
import info.magnolia.ui.model.tree.definition.ColumnDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Vaadin specific builder.
 */
public class VaadinTreeBuilder implements TreeBuilder {

    private Map<ColumnDefinition,Column> definitionToColumnRegistry = new LinkedHashMap<ColumnDefinition, Column>();

    public Column<?,?> createTreeColumn(ColumnDefinition definition) {

        // TODO: quick hack - check how to make more nice/flexible
        if (definition instanceof LabelColumnDefinition) {
            return new LabelColumn((LabelColumnDefinition) definition);
        }

        if (definition instanceof TemplateColumnDefinition) {
            return new TemplateColumn((TemplateColumnDefinition) definition);
        }

        if (definition instanceof StatusColumnDefinition) {
            return new StatusColumn((StatusColumnDefinition) definition);
        }

        if (definition instanceof StatusColumnDefinition) {
            return new StatusColumn((StatusColumnDefinition) definition);
        }

        if (definition instanceof NodeDataColumnDefinition) {
            return new NodeDataColumn((NodeDataColumnDefinition) definition);
        }

        if (definition instanceof NodeDataValueColumnDefinition) {
            return new NodeDataValueColumn((NodeDataValueColumnDefinition) definition);
        }

        if (definition instanceof NodeDataTypeColumnDefinition) {
            return new NodeDataTypeColumn((NodeDataTypeColumnDefinition) definition);
        }

        if (definition instanceof MetaDataColumnDefinition) {
            return new MetaDataColumn((MetaDataColumnDefinition) definition);
        }

        throw new IllegalArgumentException("Unkown definition type: " + definition.getClass());
    }

}
