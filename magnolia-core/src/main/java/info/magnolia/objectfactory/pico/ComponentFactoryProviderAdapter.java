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

import java.lang.reflect.InvocationTargetException;

import info.magnolia.objectfactory.ComponentFactories;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.ComponentProvider;

import org.picocontainer.injectors.ProviderAdapter;

/**
 * A PicoContainer {@link org.picocontainer.ComponentAdapter} wrapping our {@link info.magnolia.objectfactory.ComponentFactory}
 *
 * TODO : there is definitely room for improvement, cleanup, and removal of unneeded code here.
 *      : we might even go as far as deprecating ComponentFactory altogether.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ComponentFactoryProviderAdapter extends ProviderAdapter {

    private final Class<?> componentKey;
    private final Class<? extends ComponentFactory<?>> factoryClass;
    private ComponentFactory<?> factory;
    private ComponentProvider componentProvider;

    public ComponentFactoryProviderAdapter(Class<?> componentKey, Class<? extends ComponentFactory<?>> factoryClass, ComponentProvider componentProvider) {
        this.componentKey = componentKey;
        this.factoryClass = factoryClass;
        this.componentProvider = componentProvider;
    }

    public ComponentFactoryProviderAdapter(Class<?> componentKey, ComponentFactory<?> factory) {
        this.componentKey = componentKey;
        this.factoryClass = null;
        this.factory = factory;
    }

    @Override
    public Object getComponentKey() {
        return componentKey;
    }

    @Override
    public Class<?> getComponentImplementation() {
        return componentKey;
    }

    public Object provide() {
        // TODO -- well this is completely wrong for now, those should be cached
        try {
            if (this.factory == null) {
                this.factory = ComponentFactories.createFactory(factoryClass, componentProvider);
            }
            return this.factory.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e); // TODO
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e); // TODO
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e); // TODO
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e); // TODO
        }

    }
}
