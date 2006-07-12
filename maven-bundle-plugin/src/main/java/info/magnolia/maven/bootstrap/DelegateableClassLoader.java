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
 */
package info.magnolia.maven.bootstrap;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


/**
 * A class loader which loads all classes matching one of the patterns from the parent. Otherwise it tries to load the
 * class first from the own urls.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class DelegateableClassLoader extends URLClassLoader {

    /**
     * If matched the class is loaded by the parent
     */
    protected Pattern[] patterns;

    /**
     * Creat the loader
     * @param urls the urls checked first
     * @param parent the parent to use
     * @param delegatePatterns delegate allways if matched
     */
    public DelegateableClassLoader(URL[] urls, ClassLoader parent, String[] delegatePatterns) {
        super(urls, parent);
        patterns = new Pattern[delegatePatterns.length];
        for (int i = 0; i < delegatePatterns.length; i++) {
            String pattern = delegatePatterns[i];
            pattern = StringUtils.replace(pattern, ".", "\\.");
            pattern = StringUtils.replace(pattern, "*", ".*");
            patterns[i] = Pattern.compile(pattern);
        }
    }

    /**
     * Custom class loading.
     */
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (isDelegated(name)) {
            return super.loadClass(name, resolve);
        }
        else {
            Class klass = findLoadedClass(name);
            if (klass != null) {
                return klass;
            }

            try {
                return findClass(name);
            }
            catch (ClassNotFoundException e) {
                return super.loadClass(name, resolve);
            }
        }
    }

    /**
     * True if the class is delegated
     * @param name name of the class
     * @return
     */
    protected boolean isDelegated(String name) {
        for (int i = 0; i < patterns.length; i++) {
            Pattern pattern = patterns[i];
            if (pattern.matcher(name).matches()) {
                return true;
            }
        }
        return false;
    }

}
