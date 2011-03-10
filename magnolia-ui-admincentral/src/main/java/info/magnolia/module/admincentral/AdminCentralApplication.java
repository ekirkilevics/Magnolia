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
package info.magnolia.module.admincentral;

import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import info.magnolia.module.admincentral.action.ActionFactoryImpl;
import info.magnolia.module.admincentral.dialog.builder.DialogBuilder;
import info.magnolia.module.admincentral.dialog.builder.VaadinDialogBuilder;
import info.magnolia.module.admincentral.dialog.view.DialogPresenter;
import info.magnolia.module.admincentral.model.UIModel;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.pico.PicoComponentProvider;
import info.magnolia.ui.framework.action.ActionFactory;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.vaadin.shell.ShellImpl;

/**
 * Application class for AdminCentral. Provides a scoped IoC container and performs initialization of the UI.
 */
public class AdminCentralApplication extends Application implements HttpServletRequestListener {

    private static final long serialVersionUID = 5773744599513735815L;

    private PicoComponentProvider componentProvider;

    @Override
    public void init() {

        // Initialize the view first since ShellImpl depends on it being set up when it's constructor is called
        componentProvider.getComponent(AdminCentralView.class).init();

        // Now initialize the presenter to start up MVP
        componentProvider.getComponent(AdminCentralPresenter.class).init();
    }

    private void createScopedContainer() {

        PicoComponentProvider provider = (PicoComponentProvider) Components.getComponentProvider();
        PicoBuilder builder = new PicoBuilder(provider.getContainer()).withConstructorInjection().withCaching();

        MutablePicoContainer container = builder.build();

        componentProvider = new PicoComponentProvider(container, provider);
        Properties properties = new Properties();
        properties.put(DialogBuilder.class.getName(), VaadinDialogBuilder.class.getName());
        componentProvider.parseConfiguration(properties);

        container.addComponent(ComponentProvider.class, componentProvider);

        container.addComponent(Application.class, this);
        container.addComponent(AdminCentralView.class, AdminCentralViewImpl.class);
        container.addComponent(AdminCentralPresenter.class, AdminCentralPresenter.class);
        container.addComponent(DialogPresenter.class, DialogPresenter.class);

        container.addComponent(EventBus.class, SimpleEventBus.class);
        container.addComponent(Shell.class, ShellImpl.class);
        container.addComponent(PlaceController.class, PlaceController.class);
        container.addComponent(ActionFactory.class, ActionFactoryImpl.class);
        container.addComponent(UIModel.class, UIModel.class);

        // TODO how do we find and register classes from other modules that will be used by AdminCentral
        // TODO maybe configured in the module descriptors with scopes specified
    }

    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        if (componentProvider == null)
            createScopedContainer();

        // TODO keeping scopes in ThreadLocal is not necessary if we allow components to have their ComponentProvider injected, expect that since Content2Bean is a static service it needs to get it this way which is a shame..

        Components.pushScope(componentProvider);
    }

    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        Components.popScope(componentProvider);
    }
}
