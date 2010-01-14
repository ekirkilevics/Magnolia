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
package info.magnolia.cms.util;

import java.util.Collections;
import java.util.Map;

import info.magnolia.objectfactory.ObjectFactory;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.ClassUtils;


/**
 * Utility methods for classes.
 *
 * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.ObjectFactory}.
 *
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public final class ClassUtil {

    private static Map<String, Boolean> subClassCache = Collections.synchronizedMap(new LRUMap(200));

    /**
     * Don't instantiate.
     */
    private ClassUtil() {
        // unused
    }

    /**
     * Load a class trying both with the standard that with the thread classloader.
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.ObjectFactory#classes()}.
     *
     * @param className class name
     * @return loaded class
     * @throws ClassNotFoundException if the given class can't be loaded by both classloaders.
     */
    public static Class classForName(String className) throws ClassNotFoundException {
        return ObjectFactory.classes().forName(className);
    }

    /**
     * @deprecated since 4.3 - use {@link info.magnolia.objectfactory.ObjectFactory#classes()}.
     *
     * Shortcut for <code>ClassUtil.classForName(className).newInstance()</code>
     * 
     * @param className class name
     * @return instance of the given class
     * @throws InstantiationException exception thrown by newInstance()
     * @throws IllegalAccessException exception thrown by newInstance()
     * @throws ClassNotFoundException if the given class can't be loaded by both classloaders
     */
    public static Object newInstance(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        final Class clazz = classForName(className);
        return ObjectFactory.classes().newInstance(clazz);
    }

    /**
     * Checks if this class is a subclass
     */
    public static boolean isSubClass(Class<?> subClass, Class<?> parentClass) {
        // TODO replace this with class.asSubclass as soon we compile with 1.5
        // TODO or rather ?? parentClass.isAssignableFrom(subClass) ??
        if(subClass.equals(parentClass)){
            return true;
        }
        String key = subClass.getName() + "-" +parentClass.getName();

        // lru map
        Boolean isSubClass = subClassCache.get(key);
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
