/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.objectfactory;

import info.magnolia.cms.core.SystemProperty;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Modifier;

/**
 * Entry point to the currently configured ClassFactory, as well as some additional utility methods for manipulating Class objects.
 *
 * @see info.magnolia.objectfactory.ClassFactory
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class Classes {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Classes.class);

    public static boolean isConcrete(Class<?> clazz) {
        if (clazz == null) {
            // null class is not concrete .... ever
            return false;
        }
        return !Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Convenience/shortcut method for instantiating new classes.
     * @see info.magnolia.objectfactory.ClassFactory
     * @see #getClassFactory()
     * @throws ClassNotFoundException
     * @throws MgnlInstantiationException
     */
    public static <T> T newInstance(String className, Object... params) throws ClassNotFoundException {
        final ClassFactory cf = getClassFactory();
        final Class<T> cl = cf.forName(className);
        return cf.newInstance(cl, params);
    }

    /**
     * Convenience/shortcut for {@link #newInstance(String, Object...)}, returning null both in case
     * of a ClassNotFoundException or if the class could not be instantiated.
     * (which could be related to the parameters, etc)
     */
    public static <T> T quietNewInstance(String className, Object... params) {
        try {
            return Classes.<T>newInstance(className, params);
        } catch (ClassNotFoundException e) {
            log.warn("Couldn't find class with name {}", className);
            return null;
        } catch (MgnlInstantiationException e) {
            log.warn("Couldn't instantiate {}: {}", className, e.getMessage());
            return null;
        }
    }

    public static ClassFactory getClassFactory() {
        return cfp.current();
    }

    // this field should be final but isn't, for tests' sake
    private static ClassFactoryProvider cfp = new ClassFactoryProvider(new DefaultClassFactory());

    /**
     * ClassFactoryProvider is used to hide the "swapability" of ClassFactory.
     * This current implementation checks the current instance versus the configured property
     * on every call.
     * TODO : This could possibly be optimized or "blocked" as soon as we can now this property has indeed been configured.
     * I don't think the above would be interesting until we can do some refactoring of the ConfigLoader, PropertiesInitializer and SystemProperty classes.
     */
    protected static class ClassFactoryProvider {
        private ClassFactory initial;
        private ClassFactory current;
        private boolean swapping;

        public ClassFactoryProvider(ClassFactory initial) {
            this.initial = initial;
            this.current = initial;
        }

        public ClassFactory current() {
            check();
            return current;
        }

        private void check() {
            final String configuredCFClassName = getCurrentlyConfiguredClassName();
            final String currentCFClassName = current.getClass().getName();
            if (StringUtils.isNotEmpty(configuredCFClassName) && !currentCFClassName.equals(configuredCFClassName) && !swapping) {
                // we need to swap this instance, but other calls should not try to do it until we're done
                swapping = true;

                // whichever ClassFactory is registered will be instantiated with the initial ClassFactory.
                try {
                    final Class<ClassFactory> c = initial.forName(configuredCFClassName);
                    current = initial.newInstance(c);
                } catch (ClassNotFoundException e) {
                    log.error("Could not find {}, will keep on using {} for now", configuredCFClassName, current.getClass().getSimpleName());
                }
                swapping = false;
            }
        }

        protected String getCurrentlyConfiguredClassName() {
            return SystemProperty.getProperty(ClassFactory.class.getName());
        }
    }

}
