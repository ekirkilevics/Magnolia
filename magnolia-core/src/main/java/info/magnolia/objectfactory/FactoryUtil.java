/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.util.ClassUtil;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * Central factory instantiating most Magnolia beans, managers, observers and so on.
 *
 * @author Philipp Bracher
 * @version $Revision: 25238 $ ($Author: pbaerfuss $)
 */
public class FactoryUtil {
    private final static Logger log = LoggerFactory.getLogger(FactoryUtil.class);

    /**
     * Registered singleton instances.
     */
    private final static Map instances = new HashMap();

    /**
     * Registered Prototypes used for new Instance.
     */
    private final static Map factories = new HashMap();

    private FactoryUtil() {

    }

    /**
     * Creates a new instance of the passed interface / class by using the registered implementation. If this fails a {@link IllegalStateException} is thrown.
     *
     * @throws IllegalStateException
     */
    public static Object newInstance(Class interf) {
        if (interf == null) {
            log.error("interf can't be null", new Throwable());
            return null;
        }
        try {
            if (factories.containsKey(interf)) {
                return ((InstanceFactory) factories.get(interf)).newInstance();
            }

            String className = StringUtils.defaultIfEmpty(SystemProperty.getProperty(interf.getName()), interf.getName());
            if (isInRepositoryDefinition(className)) {
                String repository = ContentRepository.CONFIG;
                String path = className;
                if (className.indexOf(':') >= 0) {
                    repository = StringUtils.substringBefore(className, ":");
                    path = StringUtils.substringAfter(className, ":");
                }
                return new ObservedObjectFactory(repository, path, interf);
            } else {
                Class clazz = ClassUtil.classForName(className);
                Object instance = clazz.newInstance();

                if (instance instanceof InstanceFactory) {
                    setInstanceFactory(interf, (InstanceFactory) instance);
                    return ((InstanceFactory) instance).newInstance();
                }
                return instance;
            }
        }
        catch (Exception e) {
            throw new IllegalStateException("Can't instantiate an implementation of this class [" + interf.getName() + "]: " + ExceptionUtils.getMessage(e), e);
        }
    }

    private static boolean isInRepositoryDefinition(String className) {
        return className.startsWith("/") || className.indexOf(':') >= 0;
    }

    public static Class getImplementation(Class interf) throws ClassNotFoundException {
        String className = StringUtils.defaultIfEmpty(SystemProperty.getProperty(interf.getName()), interf.getName());
        if (!isInRepositoryDefinition(className)) {
            return ClassUtil.classForName(className);
        } else {
            return interf;
        }
    }

    /**
     * This method does not use discovery! It is a util method for easy instantiating. In any case of an exception null is returned.
     */
    public static Object newInstanceWithoutDiscovery(String className, Object[] args) {

        if (StringUtils.isEmpty(className)) {
            return null;
        }

        Class clazz;
        try {
            clazz = ClassUtil.classForName(className);
        }
        catch (ClassNotFoundException e) {
            log.error("Can't find class: " + className, e);
            return null;
        }
        try {
            return ConstructorUtils.invokeConstructor(clazz, args);
        }
        catch (Exception e) {
            log.error("Can't instantiate: " + className, e);
        }
        return null;
    }

    public static Object newInstanceWithoutDiscovery(String className) {
        return newInstanceWithoutDiscovery(className, new Object[]{});
    }

    public static synchronized Object getSingleton(Class interf) {
        Object instance = instances.get(interf);
        if (instance == null) {
            instance = newInstance(interf);
            instances.put(interf, instance);
        }
        if (instance instanceof ObservedObjectFactory) {
            instance = ((ObservedObjectFactory) instance).getObservedObject();
        }
        return instance;
    }

    public static void setDefaultImplementation(Class interf, Class impl) {
        setDefaultImplementation(interf, impl.getName());
    }

    public static void setDefaultImplementation(Class interf, String impl) {
        if (!SystemProperty.getProperties().containsKey(interf.getName())) {
            setImplementation(interf, impl);
        }
    }

    public static void setImplementation(Class interf, Class impl) {
        setImplementation(interf, impl.getName());
    }

    public static void setImplementation(Class interf, String impl) {
        SystemProperty.getProperties().setProperty(interf.getName(), impl);
    }


    /**
     * Register an instance which will be returned by getSingleton().
     */
    public static void setInstance(Class interf, Object instance) {
        instances.put(interf, instance);
    }

    /**
     * newInstance will use this prototype for cloning a new object.
     */
    public static void setInstanceFactory(Class interf, InstanceFactory factory) {
        factories.put(interf, factory);
    }

    public static void clear() {
        factories.clear();
        instances.clear();
    }

}