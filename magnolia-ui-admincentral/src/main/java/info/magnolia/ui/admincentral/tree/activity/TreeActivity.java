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
package info.magnolia.ui.admincentral.tree.activity;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.jcr.Item;
import javax.jcr.RepositoryException;

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.editworkspace.event.ContentChangedEvent;
import info.magnolia.ui.admincentral.editworkspace.event.ContentChangedEvent.Handler;
import info.magnolia.ui.admincentral.editworkspace.place.ItemSelectedPlace;
import info.magnolia.ui.admincentral.tree.action.EditWorkspaceActionFactory;
import info.magnolia.ui.admincentral.tree.builder.TreeBuilder;
import info.magnolia.ui.admincentral.tree.container.JcrContainerBackend;
import info.magnolia.ui.admincentral.tree.view.TreeView;
import info.magnolia.ui.admincentral.tree.view.TreeViewImpl;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.model.tree.definition.ColumnDefinition;
import info.magnolia.ui.model.tree.definition.TreeDefinition;
import info.magnolia.ui.model.tree.registry.TreeRegistry;

/**
 * Activity for displaying dialogs.
 *
 * @author tmattsson
 */
public class TreeActivity extends AbstractActivity implements TreeView.Presenter, Handler {

    private final String treeName;
    private PlaceController placeController;
    private TreeView treeView;
    private String path;
    private TreeBuilder builder;
    private ComponentProvider componentProvider;
    private Shell shell;
    private TreeRegistry treeRegistry;
    private JcrContainerBackend jcrContainerBackend;

    public TreeActivity(String treeName, String path, PlaceController placeController, TreeBuilder builder, ComponentProvider componentProvider, Shell shell, TreeRegistry treeRegistry) {
        this.treeName = treeName;
        this.path = path;
        this.placeController = placeController;
        this.builder = builder;
        this.componentProvider = componentProvider;
        this.shell = shell;
        this.treeRegistry = treeRegistry;
    }

    // TODO is this good practice?
    public void update(String path){
        if(!this.path.equals(path)){
            this.path = path;
            treeView.select(path);
        }
    }

    public void start(ViewPort viewPort, EventBus eventBus) {
        try {
            TreeDefinition treeDefinition = this.treeRegistry.getTree(treeName);

            Map<String, Column<?, ?>> columns = new LinkedHashMap<String, Column<?, ?>>();
            for (ColumnDefinition columnDefinition : treeDefinition.getColumns()) {
                // FIXME use getName() not getLabel()
                Column<?, ?> column = builder.createTreeColumn(columnDefinition);
                // only add if not null - null meaning there's no definitionToImplementationMapping defined for that column.
                if (column != null) {
                    columns.put(columnDefinition.getLabel(), column);
                }
            }

            EditWorkspaceActionFactory actionFactory = new EditWorkspaceActionFactory(componentProvider);

            jcrContainerBackend = new JcrContainerBackend(treeDefinition, columns, actionFactory);

            this.treeView = new TreeViewImpl(this, treeDefinition, jcrContainerBackend, shell);

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        treeView.select(path);
        eventBus.addHandler(ContentChangedEvent.class, this);
        viewPort.setView(treeView);
    }

    public void onItemSelection(Item jcrItem) {
        try {
            this.path = jcrContainerBackend.getPathInTree(jcrItem);
            placeController.goTo(new ItemSelectedPlace(treeName, this.path));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void onContentChanged(ContentChangedEvent event) {
        // FIXME only if we are not the source!
        treeView.refresh();
    }
}
