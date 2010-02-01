/**
 * This file Copyright (c) 2010 Magnolia International
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

import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.DefaultComponentProvider;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $) 
 */
public class ComponentsTestUtil {

    /**
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.DefaultComponentProvider#setDefaultImplementation(Class, Class)}
     *             todo - this is only used in tests
     */
    public static void setDefaultImplementation(Class interf, Class impl) {
        ((DefaultComponentProvider) Components.getComponentProvider()).setDefaultImplementation(interf, impl);
    }

    /**
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.DefaultComponentProvider#setDefaultImplementation(Class, String)}
     *             todo - this is only used in tests
     */
    public static void setDefaultImplementation(Class interf, String impl) {
        ((DefaultComponentProvider) Components.getComponentProvider()).setDefaultImplementation(interf, impl);
    }

    /**
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.DefaultComponentProvider#setImplementation(Class, Class)}
     *             todo - this is only used in tests
     */
    public static void setImplementation(Class interf, Class impl) {
        ((DefaultComponentProvider) Components.getComponentProvider()).setImplementation(interf, impl);
    }

    /**
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.DefaultComponentProvider#setImplementation(Class, String)}
     *             todo - this is not used
     */
    public static void setImplementation(Class interf, String impl) {
        ((DefaultComponentProvider) Components.getComponentProvider()).setImplementation(interf, impl);
    }

    /**
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.DefaultComponentProvider#setInstance(Class, Object)}
     *             todo - this is only used in tests
     */
    public static void setInstance(Class interf, Object instance) {
        ((DefaultComponentProvider) Components.getComponentProvider()).setInstance(interf, instance);
    }

    /**
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.DefaultComponentProvider#setInstanceFactory(Class, info.magnolia.objectfactory.ComponentFactory)}
     *             todo - this is only used in tests
     */
    public static void setInstanceFactory(Class interf, ComponentFactory factory) {
        ((DefaultComponentProvider) Components.getComponentProvider()).setInstanceFactory(interf, factory);
    }

    /**
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.DefaultComponentProvider#clear()}
     *             todo - this is only used in tests
     */
    public static void clear() {
        ((DefaultComponentProvider) Components.getComponentProvider()).clear();
    }}
