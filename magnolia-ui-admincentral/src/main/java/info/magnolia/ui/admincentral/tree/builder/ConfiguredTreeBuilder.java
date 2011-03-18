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
package info.magnolia.ui.admincentral.tree.builder;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.model.builder.FactoryBase;
import info.magnolia.ui.model.tree.definition.ColumnDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TreeBuild configured via content to bean.
 */
public class ConfiguredTreeBuilder extends FactoryBase<ColumnDefinition, Column<?, ColumnDefinition>> implements TreeBuilder, Serializable {

    private static final long serialVersionUID = 6702977290186078418L;
    /**
     * List as retrieved out of JCR-config (via Content2Bean).
     */
    private List<DefinitionToImplementationMapping> defininitionToImplementationMappings =
            new ArrayList<DefinitionToImplementationMapping>();

    public ConfiguredTreeBuilder(ComponentProvider componentProvider) {
        super(componentProvider);
    }

    public List<DefinitionToImplementationMapping> getDefininitionToImplementationMappings() {
        return this.defininitionToImplementationMappings;
    }

    public void setDefininitionToImplementationMappings(List<DefinitionToImplementationMapping> defininitionToImplementationMappings) {
        this.defininitionToImplementationMappings = defininitionToImplementationMappings;
        for (Iterator<DefinitionToImplementationMapping> iterator = defininitionToImplementationMappings.iterator(); iterator
                .hasNext();) {
            addDefininitionToImplementationMapping(iterator.next());
        }
    }

    public void addDefininitionToImplementationMapping(DefinitionToImplementationMapping mapping) {
        // TODO: better usage of generics?
        addMapping((Class<ColumnDefinition>) mapping.getDefinition(), (Class<Column<?,ColumnDefinition>>) mapping.getImplementation());
    }

    public Column<?, ColumnDefinition> createTreeColumn(ColumnDefinition definition) {
        return create(definition);
    }
}
