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
package info.magnolia.ui.admincentral.workbench.activity;

import java.util.List;
import javax.jcr.Item;
import javax.jcr.RepositoryException;

import info.magnolia.ui.admincentral.jcr.JCRUtil;
import info.magnolia.ui.admincentral.tree.action.EditWorkspaceActionFactory;
import info.magnolia.ui.admincentral.workbench.place.ItemSelectedPlace;
import info.magnolia.ui.admincentral.workbench.view.DetailView;
import info.magnolia.ui.admincentral.workbench.view.DetailViewImpl;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.menu.definition.MenuItemDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;


/**
 * Shows the detail view and command list.
 */
public class DetailViewActivity extends AbstractActivity implements DetailView.Presenter {

    private String path;
    private DetailView detailView;
    private EditWorkspaceActionFactory actionFactory;
    private Shell shell;
    private WorkbenchDefinition workbenchDefinition;

    public DetailViewActivity(ItemSelectedPlace place, WorkbenchDefinition workbenchDefinition, EditWorkspaceActionFactory actionFactory, Shell shell) {
        this.actionFactory = actionFactory;
        this.shell = shell;
        this.workbenchDefinition = workbenchDefinition;
        detailView = new DetailViewImpl(this);
        showItem(place.getPath());
    }

    public void start(ViewPort viewPort, EventBus eventBus) {
        viewPort.setView(detailView);
    }

    private void showItem(String path) {
        // Displaying commands for the root node makes no sense
        if (!"/".equals(path)) {
            this.path = path;
            // FIXME should be dependent on the item type
            detailView.showActions(workbenchDefinition.getMenuItems());
            detailView.showDetails(workbenchDefinition.getWorkspace(), path);
        }
    }

    public void onCommandSelected(String commandName) {
        // TODO we should inject the tree definition or something more abstract
        final List<MenuItemDefinition> contextMenuItems = workbenchDefinition.getMenuItems();
        // TODO should this be a map to avoid such iterations?
        for (MenuItemDefinition menuItemDefinition : contextMenuItems) {
            final Item item;
            try {
                String normalizedPath = (workbenchDefinition.getPath() + path).replaceAll("//", "/");
                item = JCRUtil.getSession(workbenchDefinition.getWorkspace()).getItem(normalizedPath);
                if(menuItemDefinition.getName().equals(commandName)){
                    final Action action = actionFactory.createAction(menuItemDefinition.getActionDefinition(), item);
                    try {
                        action.execute();
                    }
                    catch (ActionExecutionException e) {
                        shell.showError("Can't execute action.", e);
                    }
                }            }
            catch (RepositoryException e) {
                shell.showError("Can't access content.", e);
            }
        }
    }
}
