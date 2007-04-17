/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.util;

import info.magnolia.cms.core.SystemProperty;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.discovery.tools.DiscoverClass;
import org.apache.commons.discovery.tools.DiscoverSingleton;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class FactoryUtil {

    public interface InstanceFactory {
        public Object newInstance();
    }

    /**
     * Logger.
     */
    protected static Logger log = LoggerFactory.getLogger(FactoryUtil.class);

    protected static DiscoverClass discovery = new DiscoverClass();

    /**
     * Registered singleton instances
     */
    protected static Map instances = new HashMap();

    /**
     * Registered Prototypes used for new Instance
     */
    protected static Map factories = new HashMap();

    private FactoryUtil() {

    }

    /**
     * @deprecated use newInstance
     */
    public static Object getInstance(Class interf) {
        return newInstance(interf);
    }

    public static Object newInstance(Class interf) {
        if(interf == null){
            log.error("interf can't be null", new Throwable());
            return null;
        }
        try {
            if (factories.containsKey(interf)) {
                return ((InstanceFactory) factories.get(interf)).newInstance();
            }
            // make interf the default implementation
            return discovery.newInstance(interf, SystemProperty.getProperties(), interf.getName());
        }
        catch (Exception e) {
            log.error("can't instantiate an implementation of this class [" + interf.getName() + "]");
        }
        return null;
    }

    /**
     * @deprecated use newInstance
     */
    public static Object getInstanceWithoutDiscovery(String className, Object[] args) {
        return newInstanceWithoutDiscovery(className, args);
    }

    /**
     * This method does not use discovery! It is a util method for easy instantiating. In any case of an exception null is returned.
     *
     * @param className
     * @return
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
            log.error("can't find class: " + className, e);
            return null;
        }
        try {
            return ConstructorUtils.invokeConstructor(clazz, args);
        }
        catch (Exception e) {
            log.error("can't instantiate: " + className, e);
        }
        return null;
    }

    /**
     * @deprecated use newInstance
     */
    public static Object getInstanceWithoutDiscovery(String className) {
        return newInstanceWithoutDiscovery(className);
    }

    public static Object newInstanceWithoutDiscovery(String className) {
        return newInstanceWithoutDiscovery(className, new Object[]{});
    }

    public static Object getSingleton(Class interf) {
        Object instance = instances.get(interf);
        if (instance == null) {
            if (factories.containsKey(interf)) {
                instance = ((InstanceFactory) factories.get(interf)).newInstance();
            } else {
                instance = DiscoverSingleton.find(interf, SystemProperty.getProperties(), interf.getName());
            }
            instances.put(interf, instance);
        }
        return instance;
    }

    public static void setDefaultImplementation(Class interf, Class impl) {
        setDefaultImplementation(interf, impl.getName());
    }

    /**
     * @param interf
     * @param impl
     */
    public static void setDefaultImplementation(Class interf, String impl) {
        if(!SystemProperty.getProperties().containsKey(interf.getName())){
            setImplementation(interf, impl);
        }
    }

    public static void setImplementation(Class interf, Class impl) {
        setDefaultImplementation(interf, impl.getName());
    }

    /**
     * @param interf
     * @param impl
     */
    public static void setImplementation(Class interf, String impl) {
        SystemProperty.getProperties().setProperty(interf.getName(), impl);
    }


    /**
     * Register an instance which will be returned by getSingleton()
     *
     * @param interf
     * @param instance
     */
    public static void setInstance(Class interf, Object instance) {
        instances.put(interf, instance);
    }

    /**
     * newInstance will use this prototype for cloning a new object
     *
     * @param interf
     * @param factory
     */
    public static void setInstanceFactory(Class interf, InstanceFactory factory) {
        factories.put(interf, factory);
    }

    public static void clear() {
        factories.clear();
        instances.clear();
        DiscoverSingleton.release();
    }

}
