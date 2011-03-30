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
package info.magnolia.test.mock;

import info.magnolia.objectfactory.ComponentConfigurationPath;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.LazyObservedComponentFactory;
import info.magnolia.objectfactory.PropertiesComponentProvider;

import java.util.Properties;

/**
 * ComponentProvider that is useful for tests as it can be configured and reconfigured.
 *
 * @author tmattsson
 */
public class MockComponentProvider extends PropertiesComponentProvider {

    public MockComponentProvider() {
    }

    public MockComponentProvider(Properties mappings) {
        super(mappings);
    }

    /**
     * Used only in tests.
     * @see {@link info.magnolia.test.ComponentsTestUtil}
     */
    public <T> void setImplementation(Class<T> keyType, String value) {
        removeComponent(keyType);

        if (ComponentConfigurationPath.isComponentConfigurationPath(value)) {

            ComponentConfigurationPath path = new ComponentConfigurationPath(value);
            ComponentFactory<T> factory = new LazyObservedComponentFactory<T>(path.getRepository(), path.getPath(), keyType, this);
            registerComponentFactory(keyType, factory);
        } else {
            Class<? extends T> valueType = (Class<? extends T>) classForName(value);
            if (valueType == null) {
                // TODO
            } else {
                registerComponent(keyType, valueType);
            }
        }
    }

    /**
     * Used only in tests.
     * @see {@link info.magnolia.test.ComponentsTestUtil}
     */
    public <T> void setInstance(Class<T> type, T instance) {
        removeComponent(type);
        registerInstance(type, instance);
    }

    /**
     * Used only in tests.
     * @see {@link info.magnolia.test.ComponentsTestUtil}
     */
    public <T> void setInstanceFactory(Class<T> type, ComponentFactory<T> factory) {
        removeComponent(type);
        registerComponentFactory(type, factory);
    }

    /**
     * Used only in tests.
     * <strong>Warning:</strong> this does NOT clear the *mappings*. With the current/default implementation,
     * this means tests also have to call SystemProperty.clearr()
     * @see {@link info.magnolia.test.ComponentsTestUtil}
     */
    public void clear() {
        super.clear();
    }
}
