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
package info.magnolia.objectfactory.guice;

import java.lang.reflect.InvocationTargetException;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.ComponentFactoryUtil;
import info.magnolia.objectfactory.ComponentProvider;


/**
 * Guice Provider which serves as an adapter to a ComponentFactory. Performs member injection on its target
 * ComponentFactory before using it for the first time.
 *
 * @param <T> the type of the object this provider returns
 * @version $Id$
 */
public class GuiceComponentFactoryProviderAdapter<T> implements Provider<T> {

    @Inject
    private Injector injector;
    @Inject
    private ComponentProvider componentProvider;
    private Class<? extends ComponentFactory<T>> factoryClass;
    private ComponentFactory<T> factory;
    private boolean injected = false;

    public GuiceComponentFactoryProviderAdapter(Class<? extends ComponentFactory<T>> implementation) {
        this.factoryClass = implementation;
    }

    public GuiceComponentFactoryProviderAdapter(ComponentFactory<T> factory) {
        this.factory = factory;
    }

    @Override
    public synchronized T get() {
        if (this.factory == null) {
            try {
                this.factory = ComponentFactoryUtil.createFactory(factoryClass, componentProvider);
            } catch (InstantiationException e) {
                throw new ProvisionException("Failed to create ComponentFactory [" + factoryClass + "]", e);
            } catch (IllegalAccessException e) {
                throw new ProvisionException("Failed to create ComponentFactory [" + factoryClass + "]", e);
            } catch (InvocationTargetException e) {
                throw new ProvisionException("Failed to create ComponentFactory [" + factoryClass + "]", e);
            }
        }
        if (!injected) {
            injector.injectMembers(factory);
            injected = true;
        }
        return this.factory.newInstance();
    }
}
