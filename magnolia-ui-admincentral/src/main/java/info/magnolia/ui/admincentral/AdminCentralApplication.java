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
package info.magnolia.ui.admincentral;


import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.ComponentProviderUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.MutableComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.ui.admincentral.configuration.AdminCentralConfiguration;
import info.magnolia.ui.admincentral.configuration.AdminCentralConfigurationProvider;
import info.magnolia.ui.model.settings.Direction;
import info.magnolia.ui.model.settings.InputDevice;
import info.magnolia.ui.model.settings.UISettings;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;

/**
 * Application class for AdminCentral. Provides a scoped IoC container and performs initialization of the UI.
 */
@SuppressWarnings("serial")
public class AdminCentralApplication extends Application implements HttpServletRequestListener {

    private MutableComponentProvider componentProvider;

    @Override
    public void init() {

        // Initialize the view first since ShellImpl depends on it being set up when it's constructor is called
        componentProvider.getComponent(AdminCentralView.class).init();

        // Now initialize the presenter to start up MVP
        componentProvider.getComponent(AdminCentralPresenter.class).init();
    }

    private void createComponentProvider() {

        final AdminCentralConfigurationProvider configurationProvider = Components.getComponent(AdminCentralConfigurationProvider.class);
        final User user = MgnlContext.getUser();
        final UISettings uiSettings = new UISettings(Direction.LTR, InputDevice.MOUSE);

        final AdminCentralConfiguration configuration = configurationProvider.getConfiguration(user, uiSettings);
        final ComponentProviderConfiguration componentsConfiguration = configuration.getApplication().getComponents();

        // now create the ui componentProvider
        componentProvider = ComponentProviderUtil.createChild(Components.getComponentProvider(), componentsConfiguration);
        componentProvider.registerInstance(Application.class, this);
        componentProvider.registerInstance(User.class, user);
        componentProvider.registerInstance(UISettings.class, uiSettings);
        componentProvider.registerInstance(AdminCentralConfiguration.class, configuration);


// TODO remove, but kept if something with the configuration goes wrong
//        componentProvider.registerConfiguredComponent(NavigationProvider.class, ContentRepository.CONFIG, "/modules/admin-central/components/navigationProvider", false);
//
//        componentProvider.registerImplementation(AdminCentralView.class, AdminCentralViewImpl.class);
//        componentProvider.registerImplementation(AdminCentralPresenter.class, AdminCentralPresenter.class);
//        componentProvider.registerImplementation(MainActivityMapper.class, MainActivityMapper.class);
//
//        componentProvider.registerImplementation(DialogBuilder.class, VaadinDialogBuilder.class);
//        componentProvider.registerImplementation(DialogPresenter.class, DialogPresenter.class);
//
//        componentProvider.registerImplementation(NavigationView.class, NavigationViewImpl.class);
//        componentProvider.registerImplementation(NavigationPermissionSchema.class, NavigationPermissionSchemaImpl.class);
//        componentProvider.registerImplementation(NavigationActivityMapper.class, NavigationActivityMapper.class);
//        componentProvider.registerImplementation(NavigationActivity.class, NavigationActivity.class);
//        componentProvider.registerImplementation(NavigationActionFactory.class, NavigationActionFactory.class);
//
//        componentProvider.registerImplementation(EmbeddedView.class, EmbeddedViewImpl.class);
//
//        componentProvider.registerImplementation(Shell.class, ShellImpl.class);
//        componentProvider.registerImplementation(EventBus.class, SimpleEventBus.class);
//        componentProvider.registerImplementation(PlaceController.class, PlaceController.class);
    }

    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        if (componentProvider == null) {
            createComponentProvider();
        }

        // TODO keeping scopes in ThreadLocal is not necessary if we allow components to have their ComponentProvider injected, expect that since Content2Bean is a static service it needs to get it this way which is a shame..

        Components.pushScope(componentProvider);
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        Components.popScope(componentProvider);
    }
}
