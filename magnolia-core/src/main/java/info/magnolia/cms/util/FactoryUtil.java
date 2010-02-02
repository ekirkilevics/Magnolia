/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.DefaultComponentProvider;
import info.magnolia.objectfactory.ObservedComponentFactory;

/**
 * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.Components#getComponentProvider()}
 *
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class FactoryUtil {

    private FactoryUtil() {

    }

    /**
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.Components#getComponentProvider()#newInstance(Class)}
     */
    public static Object newInstance(Class interf) {
        return Components.getComponentProvider().newInstance(interf);
    }

    /**
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.Components#getComponentProvider()#getImplementation(Class)}
     */
    public static Class getImplementation(Class interf) throws ClassNotFoundException {
        return Components.getComponentProvider().getImplementation(interf);
    }

    /**
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.Classes}
     */
    public static Object newInstanceWithoutDiscovery(String className, Object[] args) {
        return Classes.quietNewInstance(className, args);
    }

    /**
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.Classes#getClassFactory()}
     */
    public static Object newInstanceWithoutDiscovery(String className) {
        return newInstanceWithoutDiscovery(className, new Object[]{});
    }

    /**
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.Components#getSingleton(Class)}
     */
    public static Object getSingleton(Class interf) {
        return Components.getSingleton(interf);
    }

    /**
     * @deprecated since 4.3 - For tests, use {@link info.magnolia.test.ComponentsTestUtil}, otherwise see {@link info.magnolia.objectfactory.DefaultComponentProvider#setDefaultImplementation(Class, Class)}
     */
    public static void setDefaultImplementation(Class interf, Class impl) {
        ((DefaultComponentProvider) Components.getComponentProvider()).setImplementation(interf, impl.getName());
    }

    /**
     * @deprecated since 4.3 - For tests, use {@link info.magnolia.test.ComponentsTestUtil}, otherwise see  {@link info.magnolia.objectfactory.DefaultComponentProvider#setDefaultImplementation(Class, String)}
     */
    public static void setDefaultImplementation(Class interf, String impl) {
        ((DefaultComponentProvider) Components.getComponentProvider()).setImplementation(interf, impl);
    }

    /**
     * @deprecated since 4.3 - For tests, use {@link info.magnolia.test.ComponentsTestUtil}, otherwise see {@link info.magnolia.objectfactory.DefaultComponentProvider#setImplementation(Class, Class)}
     */
    public static void setImplementation(Class interf, Class impl) {
        ((DefaultComponentProvider) Components.getComponentProvider()).setImplementation(interf, impl.getName());
    }

    /**
     * @deprecated since 4.3 - For tests, use {@link info.magnolia.test.ComponentsTestUtil}, otherwise see {@link info.magnolia.objectfactory.DefaultComponentProvider#setImplementation(Class, String)}
     */
    public static void setImplementation(Class interf, String impl) {
        ((DefaultComponentProvider) Components.getComponentProvider()).setImplementation(interf, impl);
    }

    /**
     * @deprecated since 4.3 - For tests, use {@link info.magnolia.test.ComponentsTestUtil}, otherwise see {@link info.magnolia.objectfactory.DefaultComponentProvider#setInstance(Class, Object)}
     */
    public static void setInstance(Class interf, Object instance) {
        ((DefaultComponentProvider) Components.getComponentProvider()).setInstance(interf, instance);
    }

    /**
     * @deprecated since 4.3 - For tests, use {@link info.magnolia.test.ComponentsTestUtil}, otherwise see {@link info.magnolia.objectfactory.DefaultComponentProvider#setInstanceFactory(Class, info.magnolia.objectfactory.ComponentFactory)}
     */
    public static void setInstanceFactory(Class interf, InstanceFactory factory) {
        ((DefaultComponentProvider) Components.getComponentProvider()).setInstanceFactory(interf, factory);
    }

    /**
     * @deprecated since 4.3 - For tests, use {@link info.magnolia.test.ComponentsTestUtil}, otherwise see {@link info.magnolia.objectfactory.DefaultComponentProvider#clear()}
     */
    public static void clear() {
        ((DefaultComponentProvider) Components.getComponentProvider()).clear();
    }

    /**
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.ComponentFactory}
     */
    public interface InstanceFactory extends ComponentFactory {
    }

    /**
     * @author philipp
     * @version $Id$
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.ObservedComponentFactory}
     */
    public static class ObservedObjectFactory extends ObservedComponentFactory {
        public ObservedObjectFactory(String repository, String path, Class interf) {
            super(repository, path, interf);
        }
    }

}
