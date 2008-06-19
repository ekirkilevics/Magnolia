/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.util;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;
import info.magnolia.context.MgnlContext;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

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
            if (isInRepositoryDefinition(className)) {
                String repository = ContentRepository.CONFIG;
                String path = className;
                if (className.indexOf(':') >= 0) {
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
            // TODO throw exception ! the system can't work !!!!
            log.error("can't instantiate an implementation of this class [" + interf.getName() + "]", e);
        }
        return null;
    }

    private static boolean isInRepositoryDefinition(String className) {
        return className.startsWith("/") || className.indexOf(':') >= 0;
    }

    public static Class getImplementation(Class interf) throws ClassNotFoundException {
        String className = StringUtils.defaultIfEmpty(SystemProperty.getProperty(interf.getName()), interf.getName());
        if(!isInRepositoryDefinition(className)){
            return ClassUtil.classForName(className);
        }
        else{
            return interf;
        }
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

    public static synchronized Object getSingleton(Class interf) {
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
        setImplementation(interf, impl.getName());
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
    public static class ObservedObjectFactory implements EventListener{

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
            load();
            startObservation(path);
        }

        protected void startObservation(String handle) {
            ObservationUtil.registerDefferedChangeListener(ContentRepository.CONFIG, handle, this, 1000, 5000);
        }

        public void onEvent(EventIterator events) {
            reload();
         }

        protected void reload(){
            load();
        }

        protected void load() {
            HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(repository);
            if(hm.isExist(path)){
                Content node;
                try {
                    node = hm.getContent(path);
                    onRegister(node);
                }
                catch (RepositoryException e) {
                    log.error("can't read configuration for object " + interf + " from [" + repository + ":" + path + "]", e);
                }
            }
            else{
                log.warn("can't read configuration for object " + interf + " from [" + repository + ":" + path + "]");
            }
        }

        protected void onRegister(Content node) {
            try {
                Object instance = transformNode(node);

                if(this.observedObject != null){
                    log.info(this.interf.getName()+" reloaded [ "+node.getHandle()+" ]");
                }
                this.observedObject = instance;
            }
            catch (Exception e) {
                log.error("can't instantiate object [" + repository + ":" + path + "]", e);
            }
        }

        protected Object transformNode(Content node) throws Content2BeanException {
            return Content2BeanUtil.toBean(node, true, getContent2BeanTransformer());
        }

        protected Content2BeanTransformer getContent2BeanTransformer() {
            // we can not discover again the same class we are building
            return new Content2BeanTransformerImpl(){
                public Object newBeanInstance(TransformationState state, Map properties) throws Content2BeanException {
                    if(state.getCurrentType().getType().equals(interf)){
                        return FactoryUtil.newInstanceWithoutDiscovery(interf.getName());
                    }
                    return super.newBeanInstance(state, properties);
                }
            };
        }

        /**
         * Is not synchronized!
         */
        public Object getObservedObject() {
            return this.observedObject;
        }

        public String toString() {
            return super.toString() + ":" + interf +  "(" + repository + ":" +  path + ")";
        }
    }

}
