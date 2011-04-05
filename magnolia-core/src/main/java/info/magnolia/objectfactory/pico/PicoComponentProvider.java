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
package info.magnolia.objectfactory.pico;


import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.HierarchicalComponentProvider;
import info.magnolia.objectfactory.PropertiesComponentProvider;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;

/**
 * A {@link ComponentProvider} using PicoContainer.
 * The getComponent() method simply delegates to the container, while the newInstance()
 * method creates a temporary container as a child of the current container,
 * registers the needed component and fetches it from there, so that any required dependencies
 * can be resolved from the parent containers.
 * TODO: document why this can't be done by registering the component in the *existing* container (perhaps with a special behavior/adapter)
 *       (and removing it afterwards) - this behavior was mimicked from pico-web-webwork2, but maybe there are better/more efficient ways no.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PicoComponentProvider extends PropertiesComponentProvider {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PicoComponentProvider.class);

    private final MutablePicoContainer pico;

    public PicoComponentProvider(MutablePicoContainer pico) {
        this.pico = pico;
    }

    public PicoComponentProvider(MutablePicoContainer pico, HierarchicalComponentProvider parent) {
        super(parent);
        this.pico = pico;
    }

    @Override
    public <T> T getComponent(Class<T> type) {
        try {
            final T found = pico.getComponent(type);
            log.debug("Looking for {}, found {}", type, found);
            if (found == null) {
                // TODO - throw specific exception
                throw new IllegalStateException("No component registered for " + type);
            }
            return found;
        } catch (ClassCastException e) {
            // as silly as it seems pico throws all as class cast even if type instance is null, e.g. when it can't be instantiated.
            log.error("Failed to cast component to type "+type+" with " + e.getMessage(), e);
            if (e.getMessage() == null) {
                throw new NullPointerException("The type " + type + " could not be instantiated. Check log files for messages related to the initalization of the type earlier.");
            } else {
                throw new ClassCastException("Can't cast type " + type);
            }
        }
    }

    @Override
    protected <T> T createInstance(Class<T> type, Object ... parameters) {

        // let's register the component in-place
        // TODO - the container building is copied from info.magnolia.cms.servlets.MgnlServletContextListener#makeContainer - remove redundancy.
        //      - additionally, we should investigate if using PicoBuilder is appropriate here, it might be a performance penalty
        //      - (PicoBuilder uses a container to build the container)
        //      - To check too: any gain in using a TransientPicoContainer instead of a DefaultPicoContainer ?
        final MutablePicoContainer adhocContainer = new PicoBuilder(pico)
            // order of injection matters, so ConstructorInjection must be first. Yes, we could add more types of injection if needed.
            .withConstructorInjection()
            .withAnnotatedFieldInjection()
            // TODO : do we need monitor and lifecycle on such ad-hoc components ?
            // .withMonitor(slf4jComponentMonitor)
            // .withLifecycle(lifecycleStrategy)
            .build();

        for (Object parameter : parameters) {
            adhocContainer.addComponent(parameter.getClass(), parameter);
        }

        adhocContainer.addComponent(type, type);
        return adhocContainer.getComponent(type);
    }

    public PicoContainer getContainer() {
        return pico;
    }

    @Override
    public <T> void registerImplementation(Class<T> type, Class<? extends T> implementationType) {
        super.registerImplementation(type, implementationType);

        // If already registered remove, TODO this should work for all register- methods.
        if (pico.getComponentAdapter(type) != null)
            pico.removeComponent(type);

        if (ComponentFactory.class.isAssignableFrom(implementationType)) {
            pico.addAdapter(new ComponentFactoryProviderAdapter(type, (Class<ComponentFactory<?>>) implementationType, this));
        } else {
            pico.addComponent(type, implementationType);
        }
    }

    @Override
    public <T> void registerComponentFactory(Class<T> type, ComponentFactory<T> componentFactory) {
        super.registerComponentFactory(type, componentFactory);
        pico.addAdapter(new ComponentFactoryProviderAdapter(type, componentFactory));
    }

    @Override
    public <T> void registerInstance(Class<T> type, T instance) {
        super.registerInstance(type, instance);
        pico.addComponent(type, instance);
    }

    @Override
    public PicoComponentProvider createChild() {
        PicoBuilder builder = new PicoBuilder(getContainer()).withConstructorInjection().withCaching();

        final MutablePicoContainer container = builder.build();

        return new PicoComponentProvider(container, this);
    }
}