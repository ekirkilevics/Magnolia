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

/**
 * @deprecated since 4.3 - use info.magnolia.objectfactory.FactoryUtil
 *
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class FactoryUtil {
    private FactoryUtil() {

    }

    public static Object newInstance(Class interf) {
        return info.magnolia.objectfactory.FactoryUtil.newInstance(interf);
    }

    public static Class getImplementation(Class interf) throws ClassNotFoundException {
        return info.magnolia.objectfactory.FactoryUtil.getImplementation(interf);
    }

    public static Object newInstanceWithoutDiscovery(String className, Object[] args) {
        return info.magnolia.objectfactory.FactoryUtil.newInstanceWithoutDiscovery(className, args);
    }

    public static Object newInstanceWithoutDiscovery(String className) {
        return info.magnolia.objectfactory.FactoryUtil.newInstanceWithoutDiscovery(className);
    }

    public static Object getSingleton(Class interf) {
        return info.magnolia.objectfactory.FactoryUtil.getSingleton(interf);
    }

    public static void setDefaultImplementation(Class interf, Class impl) {
        info.magnolia.objectfactory.FactoryUtil.setDefaultImplementation(interf, impl);
    }

    public static void setDefaultImplementation(Class interf, String impl) {
        info.magnolia.objectfactory.FactoryUtil.setDefaultImplementation(interf, impl);
    }

    public static void setImplementation(Class interf, Class impl) {
        info.magnolia.objectfactory.FactoryUtil.setImplementation(interf, impl);
    }

    public static void setImplementation(Class interf, String impl) {
        info.magnolia.objectfactory.FactoryUtil.setImplementation(interf, impl);
    }

    public static void setInstance(Class interf, Object instance) {
        info.magnolia.objectfactory.FactoryUtil.setInstance(interf, instance);
    }

    public static void setInstanceFactory(Class interf, InstanceFactory factory) {
        info.magnolia.objectfactory.FactoryUtil.setInstanceFactory(interf, factory);
    }

    public static void clear() {
        info.magnolia.objectfactory.FactoryUtil.clear();
    }

    /**
     * @deprecated since 4.3 - use info.magnolia.objectfactory.InstanceFactory
     */
    public interface InstanceFactory extends info.magnolia.objectfactory.InstanceFactory {
    }

    /**
     * @deprecated since 4.3 - use info.magnolia.objectfactory.ObservedObjectFactory
     * 
     * @author philipp
     * @version $Id$
     */
    public static class ObservedObjectFactory extends info.magnolia.objectfactory.ObservedObjectFactory {
        public ObservedObjectFactory(String repository, String path, Class interf) {
            super(repository, path, interf);
        }
    }

}
