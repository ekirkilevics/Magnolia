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
package info.magnolia.ui.framework.activity;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoBuilder;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.pico.PicoComponentProvider;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.HandlerRegistration;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.place.PlaceHistoryHandler;
import info.magnolia.ui.framework.place.PlaceHistoryMapperImpl;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;


/**
 * Builds an inner MVP container having its own {@link ActivityManager}, {@link PlaceController} and
 * {@link EventBus}. {@link PlaceChangeEvent} events are fired to the outer {@link PlaceController}
 * and vice versa.
 *
 * TODO it is not clear how we would provide IoC here. it is comparable to a sub-conversion scope.
 *
 */
public abstract class MVPSubContainerActivity extends AbstractActivity {

    private String id;

    private EventBus innerEventBus;

    private PlaceController innerPlaceController;

    private Shell shell;

    private PlaceHistoryHandler historyHandler;

    private Shell subShell;

    private HandlerRegistration historyReg;

    public MVPSubContainerActivity(String id, Shell shell) {
        this.id = id;
        this.shell = shell;
    }

    private ComponentProvider componentProvider;

    public ComponentProvider getComponentProvider() {
        return componentProvider;
    }

    public void start(ViewPort viewPort, EventBus outerEventBus) {

        PicoComponentProvider provider = (PicoComponentProvider) Components.getComponentProvider();
        PicoBuilder builder = new PicoBuilder(provider.getContainer()).withConstructorInjection().withCaching();

        MutablePicoContainer container = builder.build();

        componentProvider = new PicoComponentProvider(container, provider);

        container.addComponent(ComponentProvider.class, componentProvider);
        container.addComponent(EventBus.class, SimpleEventBus.class);
        // TODO use IoC with parameters instead? newInstance(SubShell.class, id)
        container.addComponent(Shell.class, shell.createSubShell(id));
        container.addComponent(PlaceController.class, PlaceController.class);

        subShell = componentProvider.getComponent(Shell.class);
        innerEventBus = componentProvider.getComponent(EventBus.class);
        innerPlaceController = componentProvider.getComponent(PlaceController.class);

        historyHandler = new PlaceHistoryHandler(new PlaceHistoryMapperImpl(getSupportedPlaces()), subShell);
        historyReg = historyHandler.register(innerPlaceController, innerEventBus, getDefaultPlace());

        // build the container
        onStart(viewPort, innerEventBus);

        historyHandler.handleCurrentHistory();
    }

    @Override
    public void onStop() {
        historyReg.removeHandler();
        subShell.setFragment(null, false);
    }

    protected abstract Class< ? extends Place>[] getSupportedPlaces();

    protected abstract Place getDefaultPlace();

    protected abstract void onStart(ViewPort display, EventBus innerEventBus);

    public String getId() {
        return id;
    }

    protected EventBus getInnerEventBus() {
        return innerEventBus;
    }

    protected PlaceController getInnerPlaceController() {
        return innerPlaceController;
    }
    /**
     * FIXME: this is a hack. We should come up with a better solution.
     * Allows subclasses adding additional components to the parent container.
     */
    protected MutablePicoContainer addComponent(Object componentKey, Object componentImplementationOrInstance, Parameter... parameters){
        return ((MutablePicoContainer)((PicoComponentProvider)componentProvider).getContainer()).addComponent(componentKey, componentImplementationOrInstance, parameters);
    }
}
