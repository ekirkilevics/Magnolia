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
}
