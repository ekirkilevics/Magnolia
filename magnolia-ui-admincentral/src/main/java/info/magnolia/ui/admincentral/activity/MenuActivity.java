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
package info.magnolia.ui.admincentral.activity;

import info.magnolia.ui.admincentral.model.UIModel;
import info.magnolia.ui.admincentral.navigation.NavigationItemConfiguration;
import info.magnolia.ui.admincentral.navigation.NavigationView;
import info.magnolia.ui.admincentral.navigation.NavigationViewImpl;
import info.magnolia.ui.framework.action.Action;
import info.magnolia.ui.framework.action.ActionFactory;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.view.ViewPort;
/**
 * MenuActivity.
 * @author fgrilli
 *
 */
public class MenuActivity extends AbstractActivity implements NavigationView.Presenter {
    private UIModel uiModel;
    private ActionFactory actionFactory;

    public MenuActivity(UIModel uiModel, ActionFactory actionFactory) {
        this.uiModel = uiModel;
        this.actionFactory = actionFactory;
    }
    public void start(ViewPort viewPort, EventBus eventBus) {
        NavigationViewImpl menu = new NavigationViewImpl(this, uiModel);
        viewPort.setView(menu);
    }

    public void onMenuSelection(NavigationItemConfiguration menuConfig) {
        final Action action = actionFactory.createAction(menuConfig.getActionDefinition());
        action.execute();
    }

}