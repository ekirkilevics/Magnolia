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

import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.model.tree.definition.ColumnDefinition;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TreeBuild configured via content to bean.
 */
public class ConfiguredTreeBuilder implements TreeBuilder, Serializable {

    private static final long serialVersionUID = 6702977290186078418L;
    private static final Logger log = LoggerFactory.getLogger(ConfiguredTreeBuilder.class);
    /**
     * Registry of types of ColumnDefinitions being mapped to Column types.
     */
    private Map<Class<?>, Class<?>> defininitionToImplementationRegistries = new LinkedHashMap<Class<?>, Class<?>>();

    /**
     * List as retrieved out of JCR-config (via Content2Bean).
     */
    private List<DefinitionToImplementationMapping> defininitionToImplementationMappings =
            new ArrayList<DefinitionToImplementationMapping>();

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
        defininitionToImplementationRegistries.put(mapping.getDefinition(), mapping.getImplementation());
    }

    public Column<?, ?> createTreeColumn(ColumnDefinition definition) {
        Class<?> classOfColumn = this.defininitionToImplementationRegistries.get(definition.getClass());
        if (classOfColumn == null) {
            log.warn("No Column-Type registered for definition of type {}", definition.getClass());
            return null;
        }
        Column<?, ?> column = null;
        try {
            Constructor<?> columnConstructor = classOfColumn.getConstructor(definition.getClass());
            column = (Column<?, ?>) columnConstructor.newInstance(definition);
        } catch (Exception e) {
            log.warn("Error trying to create instance of type {}", classOfColumn.getName(), e);
        }
        return column;
    }
}
