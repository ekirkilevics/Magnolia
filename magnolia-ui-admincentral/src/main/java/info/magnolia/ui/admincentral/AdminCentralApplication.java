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
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.pico.PicoComponentProvider;
import info.magnolia.ui.admincentral.dialog.builder.DialogBuilder;
import info.magnolia.ui.admincentral.dialog.builder.VaadinDialogBuilder;
import info.magnolia.ui.admincentral.dialog.view.DialogPresenter;
import info.magnolia.ui.admincentral.embedded.view.EmbeddedView;
import info.magnolia.ui.admincentral.embedded.view.EmbeddedViewImpl;
import info.magnolia.ui.admincentral.navigation.NavigationView;
import info.magnolia.ui.admincentral.navigation.NavigationViewImpl;
import info.magnolia.ui.admincentral.navigation.action.NavigationActionFactory;
import info.magnolia.ui.admincentral.navigation.activity.NavigationActivity;
import info.magnolia.ui.admincentral.navigation.activity.NavigationActivityMapper;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.navigation.registry.NavigationPermissionSchema;
import info.magnolia.ui.model.navigation.registry.NavigationPermissionSchemaImpl;
import info.magnolia.ui.model.navigation.registry.NavigationProvider;
import info.magnolia.ui.model.settings.Direction;
import info.magnolia.ui.model.settings.InputDevice;
import info.magnolia.ui.model.settings.UISettings;
import info.magnolia.ui.vaadin.integration.shell.ShellImpl;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;

/**
 * Application class for AdminCentral. Provides a scoped IoC container and performs initialization of the UI.
 */
public class AdminCentralApplication extends Application implements HttpServletRequestListener {

    private PicoComponentProvider componentProvider;

    @Override
    public void init() {

        // Initialize the view first since ShellImpl depends on it being set up when it's constructor is called
        componentProvider.getComponent(AdminCentralView.class).init();

        // Now initialize the presenter to start up MVP
        componentProvider.getComponent(AdminCentralPresenter.class).init();
    }

    private void createComponentProvider() {

        PicoComponentProvider provider = (PicoComponentProvider) Components.getComponentProvider();
        PicoBuilder builder = new PicoBuilder(provider.getContainer()).withConstructorInjection().withCaching();

        MutablePicoContainer container = builder.build();


        componentProvider = new PicoComponentProvider(container, provider);

        //FIXME better solution than just creating properties
        Properties properties = new Properties();
        properties.put(NavigationProvider.class.getName(), "/modules/admin-central/components/navigationProvider");
        componentProvider.parseConfiguration(properties);

        container.addComponent(ComponentProvider.class, componentProvider);

        container.addComponent(Application.class, this);
        container.addComponent(AdminCentralView.class, AdminCentralViewImpl.class);
        container.addComponent(AdminCentralPresenter.class, AdminCentralPresenter.class);
        container.addComponent(MainActivityMapper.class, MainActivityMapper.class);

        container.addComponent(DialogBuilder.class.getName(), VaadinDialogBuilder.class.getName());
        container.addComponent(DialogPresenter.class, DialogPresenter.class);

        container.addComponent(NavigationView.class, NavigationViewImpl.class);
        container.addComponent(NavigationPermissionSchema.class, NavigationPermissionSchemaImpl.class);
        container.addComponent(NavigationActivityMapper.class, NavigationActivityMapper.class);
        container.addComponent(NavigationActivity.class, NavigationActivity.class);

        container.addComponent(EmbeddedView.class, EmbeddedViewImpl.class);

        container.addComponent(EventBus.class, SimpleEventBus.class);
        container.addComponent(Shell.class, ShellImpl.class);
        container.addComponent(PlaceController.class, PlaceController.class);
        container.addComponent(NavigationActionFactory.class, NavigationActionFactory.class);

        container.addComponent(User.class, MgnlContext.getUser());

        // TODO do it dynamic
        container.addComponent(UISettings.class, new UISettings(Direction.LTR, InputDevice.MOUSE));

        // TODO how do we find and register classes from other modules that will be used by AdminCentral
        // TODO maybe configured in the module descriptors with scopes specified
    }

    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        if (componentProvider == null)
            createComponentProvider();

        // TODO keeping scopes in ThreadLocal is not necessary if we allow components to have their ComponentProvider injected, expect that since Content2Bean is a static service it needs to get it this way which is a shame..

        Components.pushScope(componentProvider);
    }

    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        Components.popScope(componentProvider);
    }
}
