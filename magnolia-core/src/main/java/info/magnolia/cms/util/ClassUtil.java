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

import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility methods for classes.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public final class ClassUtil {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ClassUtil.class);

    private static Map subClassCache = new LRUMap(200);

    /**
     * Don't instantiate.
     */
    private ClassUtil() {
        // unused
    }

    /**
     * Load a class trying both with the standard that with the thread classloader.
     * @param className class name
     * @return loaded class
     * @throws ClassNotFoundException if the given class can't be loaded by both classloaders.
     */
    public static Class classForName(String className) throws ClassNotFoundException {
        Class loadedClass;
        try {
            loadedClass = Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            loadedClass = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        }
        return loadedClass;
    }

    /**
     * Shortcut for <code>ClassUtil.classForName(className).newInstance()</code>
     * @param className class name
     * @return instance of the given class
     * @throws InstantiationException exception thrown by newInstance()
     * @throws IllegalAccessException exception thrown by newInstance()
     * @throws ClassNotFoundException if the given class can't be loaded by both classloaders
     */
    public static Object newInstance(String className) throws InstantiationException, IllegalAccessException,
        ClassNotFoundException {
        return classForName(className).newInstance();
    }

    /**
     * Checks if this class is a subclass
     */
    public static boolean isSubClass(Class subClass, Class parentClass) {
        if(subClass.equals(parentClass)){
            return true;
        }
        String key = subClass.getName() + "-" +parentClass.getName();

        // lru map
        Boolean isSubClass = (Boolean) subClassCache.get(key);
        if(isSubClass != null){
            return isSubClass.booleanValue();
        }

        if(parentClass.isInterface()){
            isSubClass = Boolean.valueOf(ClassUtils.getAllInterfaces(subClass).contains(parentClass));
        }
        else{
            isSubClass = Boolean.valueOf(ClassUtils.getAllSuperclasses(subClass).contains(parentClass));
        }
        subClassCache.put(key, isSubClass);
        return isSubClass.booleanValue();
    }
}
