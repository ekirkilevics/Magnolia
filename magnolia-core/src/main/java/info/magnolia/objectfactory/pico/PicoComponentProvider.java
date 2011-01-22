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

import info.magnolia.cms.util.DeprecationUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.DefaultComponentProvider;
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
public class PicoComponentProvider implements ComponentProvider {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PicoComponentProvider.class);

    private final PicoContainer pico;
    private final DefaultComponentProvider def;

    /**
     * @deprecated TODO - we shouldn't need DefaultComponentProvider
     */
    public PicoComponentProvider(MutablePicoContainer pico, DefaultComponentProvider def) {
        this.pico = pico;
        this.def = def;
    }

    public <C> Class<? extends C> getImplementation(Class<C> type) throws ClassNotFoundException {
        // TODO - do we need this ? If so, then implement properly.
        return def.getImplementation(type);
    }

    public <T> T getSingleton(Class<T> type) {
        DeprecationUtil.isDeprecated();
        return getComponent(type);
    }

    public <T> T getComponent(Class<T> type) {
        final T found = pico.getComponent(type);
        log.debug("Looking for {}, found {}", type, found);
        if (found == null) {
            // TODO - throw specific exception
            throw new IllegalStateException("No component registered for " + type);
        }
        return found;
    }

    public <T> T newInstance(Class<T> type) {
        // TODO : this check unfortunately triggers org.picocontainer.ComponentMonitor.noComponentFound()
        if (pico.getComponentAdapter(type) != null) {
            // TODO - throw specific exception
            throw new IllegalStateException("The powers that be have decided that it was illegal to instantiate a component that is already registered. (" + type + " in this case)");
        }

        // let's register the component in-place
        // TODO - the container building is copied from info.magnolia.cms.servlets.MgnlServletContextListener#makeContainer - remove redundancy.
        //      - additionally, we should investigate if using PicoBuilder is appropriate here, it might be a performance penalty
        //      - (PicoBuilder uses a container to build the container)
        //      - To check too: any gain in using a TransientPicoContainer instead of a DefaultPicoContainer ?
        final MutablePicoContainer adhocContainer = new PicoBuilder(pico)
                // order of injection matters, so ConstructorInjection must be first. Yes, we could add more types of injection if needed.
                .withConstructorInjection()
                .withAnnotatedFieldInjection()
                .withCaching()
                // TODO : do we need monitor and lifecycle on such ad-hoc components ?
                // .withMonitor(slf4jComponentMonitor)
                // .withLifecycle(lifecycleStrategy)
                .build();

        adhocContainer.addComponent(type);
        return adhocContainer.getComponent(type);
    }

}
