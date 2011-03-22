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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.jcr.RepositoryException;

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.tree.action.EditWorkspaceActionFactory;
import info.magnolia.ui.admincentral.tree.model.TreeModel;
import info.magnolia.ui.admincentral.tree.view.TreeView;
import info.magnolia.ui.admincentral.tree.view.TreeViewImpl;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.builder.FactoryBase;
import info.magnolia.ui.model.tree.definition.ColumnDefinition;
import info.magnolia.ui.model.tree.definition.TreeDefinition;
import info.magnolia.ui.model.tree.registry.TreeRegistry;

/**
 * TreeBuild configured via content to bean.
 */
public class ConfiguredTreeBuilder extends FactoryBase<ColumnDefinition, Column<?, ColumnDefinition>> implements TreeBuilder, Serializable {

    private static final long serialVersionUID = 6702977290186078418L;

    /**
     * List as retrieved out of JCR-config (via Content2Bean).
     */
    private List<DefinitionToImplementationMapping<ColumnDefinition, Column<?, ColumnDefinition>>> definitionToImplementationMappings = new ArrayList<DefinitionToImplementationMapping<ColumnDefinition, Column<?, ColumnDefinition>>>();

    private ComponentProvider componentProvider;
    private TreeRegistry treeRegistry;

    public ConfiguredTreeBuilder(ComponentProvider componentProvider, TreeRegistry treeRegistry) {
        super(componentProvider);
        this.treeRegistry = treeRegistry;
        this.componentProvider = componentProvider;
    }

    public List<DefinitionToImplementationMapping<ColumnDefinition, Column<?, ColumnDefinition>>> getDefinitionToImplementationMappings() {
        return this.definitionToImplementationMappings;
    }

    public void setDefinitionToImplementationMappings(List<DefinitionToImplementationMapping<ColumnDefinition, Column<?, ColumnDefinition>>> definitionToImplementationMappings) {
        this.definitionToImplementationMappings = definitionToImplementationMappings;
        for (DefinitionToImplementationMapping<ColumnDefinition, Column<?, ColumnDefinition>> definitionToImplementationMapping : definitionToImplementationMappings) {
            addDefinitionToImplementationMapping(definitionToImplementationMapping);
        }
    }

    public void addDefinitionToImplementationMapping(DefinitionToImplementationMapping<ColumnDefinition, Column<?, ColumnDefinition>> mapping) {
        addMapping(mapping.getDefinition(), mapping.getImplementation());
    }

    public Column<?, ColumnDefinition> createTreeColumn(ColumnDefinition definition) {
        return create(definition);
    }

    public TreeView createTreeView(Shell shell, TreeView.Presenter presenter, String treeName) {
        try {
            TreeDefinition treeDefinition = this.treeRegistry.getTree(treeName);

            Map<String, Column<?, ?>> columns = new LinkedHashMap<String, Column<?, ?>>();
            for (ColumnDefinition columnDefinition : treeDefinition.getColumns()) {
                // FIXME use getName() not getLabel()
                Column<?, ?> column = createTreeColumn(columnDefinition);
                // only add if not null - null meaning there's no definitionToImplementationMapping defined for that column.
                if (column != null) {
                    columns.put(columnDefinition.getLabel(), column);
                }
            }

            EditWorkspaceActionFactory actionFactory = new EditWorkspaceActionFactory(componentProvider);

            TreeModel treeModel = new TreeModel(treeDefinition, columns, actionFactory);

            return new TreeViewImpl(presenter, treeDefinition, treeModel, shell);

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
}
