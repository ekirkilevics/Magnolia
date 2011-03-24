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
import org.picocontainer.PicoBuilder;

import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.pico.ComponentFactoryProviderAdapter;
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
public abstract class AbstractActivityProxy extends AbstractActivity {

    private final class PicoMutableComponentProvider implements MutableComponentProvider {

        private ComponentProvider componentProvider;
        private MutablePicoContainer container;

        public PicoMutableComponentProvider(ComponentProvider componentProvider, MutablePicoContainer container) {
            this.componentProvider = componentProvider;
            this.container = container;
        }

        public void addComponent(Object componentKey, Object componentImplementationOrInstance) {
            container.addComponent(componentKey, componentImplementationOrInstance);
        }

        public <T> void addComponentFactory(Class<T> componentKey, ComponentFactory<T> componentFactory) {
            container.addAdapter(new ComponentFactoryProviderAdapter(componentKey, componentFactory));
        }

        public <T> Class< ? extends T> getImplementation(Class<T> type) throws ClassNotFoundException {
            return componentProvider.getImplementation(type);
        }

        public <T> T getSingleton(Class<T> type) {
            return componentProvider.getSingleton(type);
        }

        public <T> T getComponent(Class<T> type) {
            return componentProvider.getComponent(type);
        }

        public <T> T newInstance(Class<T> type, Object... parameters) {
            return componentProvider.newInstance(type, parameters);
        }
    }

    /**
     * Used in the {@link MVPSubContainer#populateComponentProvider(MutableComponentProvider)} to hide the pico specifics.
     * TODO move to the core? see patch in MAGNOLIA-3592
     */
    protected interface MutableComponentProvider extends ComponentProvider {

        void addComponent(Object componentKey, Object componentImplementationOrInstance);

        <T> void addComponentFactory(Class<T> componentClass, ComponentFactory<T> componentFactory);
    }

    private String id;

    private EventBus innerEventBus;

    private PlaceController innerPlaceController;

    private Shell shell;

    private PlaceHistoryHandler historyHandler;

    private Shell subShell;

    private HandlerRegistration historyReg;

    private Activity activity;


    public AbstractActivityProxy(String id, Shell shell) {
        this.id = id;
        this.shell = shell;
    }

    public void start(ViewPort viewPort, EventBus outerEventBus) {

        PicoComponentProvider provider = (PicoComponentProvider) Components.getComponentProvider();
        PicoBuilder builder = new PicoBuilder(provider.getContainer()).withConstructorInjection().withCaching();

        final MutablePicoContainer container = builder.build();

        ComponentProvider componentProvider = new PicoComponentProvider(container, provider);

        container.addComponent(ComponentProvider.class, componentProvider);
        container.addComponent(EventBus.class, SimpleEventBus.class);
        // TODO use IoC with parameters instead? newInstance(SubShell.class, id)
        container.addComponent(Shell.class, shell.createSubShell(id));
        container.addComponent(PlaceController.class, PlaceController.class);

        populateComponentProvider(new PicoMutableComponentProvider(componentProvider, container));

        subShell = componentProvider.getComponent(Shell.class);
        innerEventBus = componentProvider.getComponent(EventBus.class);
        innerPlaceController = componentProvider.getComponent(PlaceController.class);

        final Class<? extends Activity> activityClass = getActivityClass();

        activity = componentProvider.newInstance(activityClass, getAdditionalConstructorParameters());
        // add the activity we built so that sub components can ask for injection
        container.addComponent(activityClass, activity);

        activity.start(viewPort, innerEventBus);

        historyHandler = new PlaceHistoryHandler(new PlaceHistoryMapperImpl(getSupportedPlaces()), subShell);
        historyReg = historyHandler.register(innerPlaceController, innerEventBus, getDefaultPlace());

        historyHandler.handleCurrentHistory();
    }

    @Override
    public void onStop() {
        activity.onStop();
        historyReg.removeHandler();
        subShell.setFragment(null, false);
    }

    @Override
    public String mayStop() {
        return activity.mayStop();
    }

    public String getId() {
        return id;
    }

    protected abstract Class< ? extends Activity> getActivityClass();

    protected abstract Object[] getAdditionalConstructorParameters();

    /**
     * Prepare the IoC container.
     * TODO should this really relay on the Pico API
     */
    protected abstract void populateComponentProvider(MutableComponentProvider mutableComponentProvider);

    protected abstract Class< ? extends Place>[] getSupportedPlaces();

    protected abstract Place getDefaultPlace();


}
