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
package info.magnolia.ui.admincentral.navigation;

import java.util.Collection;

import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionFactory;
import info.magnolia.ui.model.navigation.definition.NavigationItemConfiguration;
import info.magnolia.ui.model.navigation.registry.NavigationRegistry;

/**
 * NavigationActivity.
 * @author fgrilli
 *
 */
public class NavigationActivity extends AbstractActivity implements NavigationView.Presenter {

    private ActionFactory actionFactory;
    private NavigationRegistry navigationRegistry;

    public NavigationActivity(ActionFactory actionFactory, NavigationRegistry navigationRegistry) {
        this.actionFactory = actionFactory;
        this.navigationRegistry = navigationRegistry;
    }

    public void start(ViewPort viewPort, EventBus eventBus) {
        Collection<NavigationItemConfiguration> navigationItems = this.navigationRegistry.getMenuDefinition().values();
        NavigationViewImpl menu = new NavigationViewImpl(this, navigationItems);
        viewPort.setView(menu);
    }

    public void onMenuSelection(NavigationItemConfiguration menuConfig) {
        final Action action = actionFactory.createAction(menuConfig.getActionDefinition());
        action.execute();
    }
}
