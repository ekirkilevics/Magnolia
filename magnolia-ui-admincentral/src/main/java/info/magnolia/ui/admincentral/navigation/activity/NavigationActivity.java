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
package info.magnolia.ui.admincentral.navigation.activity;

import info.magnolia.ui.admincentral.navigation.NavigationView;
import info.magnolia.ui.admincentral.navigation.action.NavigationActionFactory;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.action.PlaceChangeActionDefinition;
import info.magnolia.ui.model.menu.definition.MenuItemDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationGroupDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationItemDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationWorkareaDefinition;
import info.magnolia.ui.model.navigation.registry.NavigationProvider;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Navigation activity.
 * @author fgrilli
 *
 */
public class NavigationActivity extends AbstractActivity implements NavigationView.Presenter {

    private static final Logger log = LoggerFactory.getLogger(NavigationActivity.class);
    private NavigationView view;
    private NavigationActionFactory actionFactory;
    private Shell shell;
    private Map<Place, MenuItemDefinition> placeToMenuItemDefinitionMappings = new HashMap<Place, MenuItemDefinition>();

    public NavigationActivity(NavigationView view, NavigationActionFactory actionFactory, NavigationProvider navigationProvider, Shell shell) {
        this.actionFactory = actionFactory;
        this.view = view;
        this.shell = shell;
        view.setPresenter(this);
        createPlaceToMenuItemDefinitionMappings(navigationProvider.getNavigation());
    }
    @Override
    public void start(ViewPort viewPort, EventBus eventBus) {
        viewPort.setView(view);
    }

    @Override
    public void onMenuSelection(MenuItemDefinition menuItem) {
        final ActionDefinition actionDefinition = menuItem.getActionDefinition();
        if(actionDefinition != null){
            final Action action = actionFactory.createAction(actionDefinition);
            try {
                action.execute();
            }
            catch (ActionExecutionException e) {
                shell.showError("Can't navigate", e);
            }
        }
        else{
            shell.showNotification("No action defined for " + menuItem.getName());
        }
    }

    public void update(Place place) {
        MenuItemDefinition menuItemDefinition = getAssignedNavigationItem(place);
        if(menuItemDefinition != null){
            view.select(menuItemDefinition);
        }
    }

    private MenuItemDefinition getAssignedNavigationItem(Place place) {
        return placeToMenuItemDefinitionMappings.get(place);
    }

    private void createPlaceToMenuItemDefinitionMappings(NavigationDefinition navigationDefinition) {

        for(NavigationWorkareaDefinition workareaDefinition:navigationDefinition.getWorkareas()){
            if(!checkMenuItemDefinition(workareaDefinition)){
                continue;
            }
            final Place workareaPlace = ((PlaceChangeActionDefinition)workareaDefinition.getActionDefinition()).getPlace();
            //log.debug("mapping place {} to menu item {}", workareaPlace, workareaDefinition.getName());
            placeToMenuItemDefinitionMappings.put(workareaPlace, workareaDefinition);

            for(NavigationGroupDefinition groupDefinition: workareaDefinition.getGroups()){
                for(NavigationItemDefinition navigationItem : groupDefinition.getItems()){
                    if(!checkMenuItemDefinition(navigationItem)){
                        continue;
                    }
                    final Place menuItemPlace = ((PlaceChangeActionDefinition)navigationItem.getActionDefinition()).getPlace();
                    //log.debug("mapping place {} to menu item {}", menuItemPlace, navigationItem.getName());
                    placeToMenuItemDefinitionMappings.put(menuItemPlace, navigationItem);
                }
            }
        }
    }

    private boolean checkMenuItemDefinition(MenuItemDefinition menuItemDefinition) {
        ActionDefinition actionDefinition = menuItemDefinition.getActionDefinition();
        if(actionDefinition == null) {
            log.warn("No action definition defined for navigation item [{}]. The item will be rendered but nothing will happen when clicking on it. Is that intended?", menuItemDefinition.getName());
            return false;
        }
        if(!PlaceChangeActionDefinition.class.isAssignableFrom(actionDefinition.getClass())){
            log.warn("Type for action definition [{}] does not implement [{}]. Menu item [{}] won't work correctly.",
                    new Object[]{actionDefinition.getClass().getName(), PlaceChangeActionDefinition.class.getName(), menuItemDefinition.getName()});
            return false;
        }
        return true;
    }

}