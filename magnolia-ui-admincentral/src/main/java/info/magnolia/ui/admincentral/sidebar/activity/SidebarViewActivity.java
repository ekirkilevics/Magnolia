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
package info.magnolia.ui.admincentral.sidebar.activity;

import java.util.List;
import javax.jcr.Item;
import javax.jcr.RepositoryException;

import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.sidebar.view.SidebarView;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.admincentral.workbench.place.ItemSelectedPlace;
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
public class SidebarViewActivity extends AbstractActivity implements SidebarView.Presenter {

    private String path;
    private SidebarView sidebarView;
    private WorkbenchActionFactory actionFactory;
    private Shell shell;
    private WorkbenchDefinition workbenchDefinition;

    public SidebarViewActivity(ComponentProvider componentProvider, ItemSelectedPlace place, WorkbenchDefinition workbenchDefinition, WorkbenchActionFactory actionFactory, Shell shell) {
        this.actionFactory = actionFactory;
        this.shell = shell;
        this.workbenchDefinition = workbenchDefinition;
        sidebarView = componentProvider.newInstance(SidebarView.class, this);
        sidebarView.setPresenter(this);
        showItem(place);
    }

    public void start(ViewPort viewPort, EventBus eventBus) {
        viewPort.setView(sidebarView);
    }

    private void showItem(ItemSelectedPlace place) {
        String path = place.getPath();
        final Item item;
        try {
            String normalizedPath = (workbenchDefinition.getPath() + path).replaceAll("//", "/");
            item = MgnlContext.getJCRSession(workbenchDefinition.getWorkspace()).getItem(normalizedPath);

        } catch (RepositoryException e) {
            shell.showError("Exception trying to access " + path, e);
            return;
        }
        // Displaying commands for the root node makes no sense
        if (!"/".equals(path)) {
            this.path = path;
            // FIXME should be dependent on the item type
            sidebarView.getActionList().showActions(workbenchDefinition.getMenuItems());
            sidebarView.getDetailView().showDetails(item);
        }
    }

    public void onMenuItemSelected(String menuItemName) {
        MenuItemDefinition menuItemDefinition = getMenuItemDefinition(menuItemName);
        try {
            String normalizedPath = (workbenchDefinition.getPath() + path).replaceAll("//", "/");
            Item item = MgnlContext.getJCRSession(workbenchDefinition.getWorkspace()).getItem(normalizedPath);
            Action action = actionFactory.createAction(menuItemDefinition.getActionDefinition(), item);
            try {
                action.execute();
            } catch (ActionExecutionException e) {
                shell.showError("Can't execute action.", e);
            }
        } catch (RepositoryException e) {
            shell.showError("Can't access content.", e);
        }
    }

    private MenuItemDefinition getMenuItemDefinition(String menuItemName) {
        // TODO should this be a map to avoid such iterations?
        List<MenuItemDefinition> contextMenuItems = workbenchDefinition.getMenuItems();
        for (MenuItemDefinition menuItemDefinition : contextMenuItems) {
            if (menuItemDefinition.getName().equals(menuItemName)) {
                return menuItemDefinition;
            }
        }
        return null;
    }
}
