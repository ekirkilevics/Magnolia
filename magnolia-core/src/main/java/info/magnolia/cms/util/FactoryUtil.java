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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.ConstructorUtils;
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

            String className = StringUtils.defaultIfEmpty(SystemProperty.getProperty(interf.getName()), interf.getName());
            if(className.startsWith("/") || className.contains(":")){
                String repository = ContentRepository.CONFIG;
                String path = className;
                if(className.contains(":")){
                    repository = StringUtils.substringBefore(className, ":");
                    path = StringUtils.substringAfter(className, ":");
                }
                return new ObservedObjectFactory(repository, path, interf);
            }
            else{
                Class clazz = ClassUtil.classForName(className);
                Object instance = clazz.newInstance();

                if(instance instanceof InstanceFactory){
                    setInstanceFactory(interf, (InstanceFactory)instance);
                    return ((InstanceFactory)instance).newInstance();
                }
                return instance;
            }
        }
        catch (Exception e) {
            log.error("can't instantiate an implementation of this class [" + interf.getName() + "]", e);
        }
        return null;
    }

    public static Class getImplementation(Class interf) throws ClassNotFoundException {
        String className = StringUtils.defaultIfEmpty(SystemProperty.getProperty(interf.getName()), interf.getName());
        return ClassUtil.classForName(className);
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
            instance = newInstance(interf);
            instances.put(interf, instance);
        }
        if(instance instanceof ObservedObjectFactory){
            instance = ((ObservedObjectFactory)instance).getObservedObject();
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
    }

    /**
     * Generic observerd singleton factory.
     * @author philipp
     * @version $Id$
     *
     */
    public static class ObservedObjectFactory extends ObservedManager{

        /**
         * Path to the node in the config repository
         */
        private String path;

        /**
         * Repository name used
         */
        private String repository;

        /**
         * Logger.
         */
        private static Logger log = LoggerFactory.getLogger(ObservedObjectFactory.class);

        /**
         * The object delivered by this factory
         */
        protected Object observedObject;

        protected Class interf;

        public ObservedObjectFactory(String repository, String path, Class interf) {
            this.path = path;
            this.repository = repository;
            this.interf = interf;

            Content node;
            try {
                node = MgnlContext.getSystemContext().getHierarchyManager(repository).getContent(path);
                register(node);
            }
            catch (RepositoryException e) {
                log.error("can't read configuration for object [" + repository + ":" + path + "]", e);
            }
        }

        protected void onRegister(Content node) {
            try {
                this.observedObject = Content2BeanUtil.toBean(node, true);
                log.info(this.interf.getName()+" realoaded [ "+node.getHandle()+" ]");
            }
            catch (Exception e) {
                log.error("can't instantiate object [" + repository + ":" + path + "]", e);
            }
        }

        /**
         * We do not unset the instance. This allows getObject() to be unsynchronized.
         */
        protected void onClear() {
            this.observedObject = null;
        }

        /**
         * Is not synchronized!
         */
        public Object getObservedObject() {
            return this.observedObject;
        }
    }

}
