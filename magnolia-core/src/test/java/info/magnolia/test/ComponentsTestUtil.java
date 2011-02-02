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
package info.magnolia.test;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.DefaultComponentProvider;

import java.io.IOException;
import java.io.InputStream;

/**
 * A utility to be used in tests which allows to set default implementations or instances,
 * when IoC can't be used yet.
 * This implementation sets a new {@link DefaultComponentProvider}, and
 * delegates to its set* methods.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $) 
 */
public class ComponentsTestUtil {

    private static DefaultComponentProvider defaultComponentProvider;

    static {
        // Oh yeah, this is ugly !
        // As soon as you use ComponentsTestUtil, it has been decided that you were in troubled water.
        // So here goes:
        resetConf();
    }

    private static void resetConf() {
        try {
            final InputStream in = ComponentsTestUtil.class.getResourceAsStream("/test-magnolia.properties");
            final TestMagnoliaConfigurationProperties cfg = new TestMagnoliaConfigurationProperties(in);
            SystemProperty.setMagnoliaConfigurationProperties(cfg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void setImplementation(Class<T> interf, Class<? extends T> impl) {
        setImplementation(interf, impl.getName());
    }

    public static <T> void setImplementation(Class<T> interf, String impl) {
        getComponentProvider().setImplementation(interf, impl);
    }

    public static <T> void setInstance(Class<T> interf, T instance) {
        getComponentProvider().setInstance(interf, instance);
    }

    public static <T> void setInstanceFactory(Class<T> interf, ComponentFactory<T> factory) {
        getComponentProvider().setInstanceFactory(interf, factory);
    }

    /**
     * <strong>Warning:</strong> this does NOT clear the *mappings*. With the current/default implementation,
     * this means tests also have to call SystemProperty.clear()
     */
    public static void clear() {
        getComponentProvider().clear();
        // defaultComponentProvider = null;
        resetConf();
        // SystemProperty.setMagnoliaConfigurationProperties(null);
    }

    private static DefaultComponentProvider getComponentProvider() {
        if (defaultComponentProvider == null) {
            defaultComponentProvider = new DefaultComponentProvider(SystemProperty.getProperties());
            Components.setProvider(defaultComponentProvider);
        }

        return ((DefaultComponentProvider) Components.getComponentProvider());
    }
}
