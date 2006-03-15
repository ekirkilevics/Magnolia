/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.util;

import info.magnolia.cms.core.Path;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;


/**
 * Util to find resources in the classpath (WEB-INF/lib and WEB-INF/classes).
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ClasspathResourcesUtil {

    /**
     * Filter for filtering the resources.
     * @author Philipp Bracher
     * @version $Revision$ ($Author$)
     *
     */
    public static abstract class Filter {

        public abstract boolean accept(String name);
    }

    /**
     * Return a collection containing the resource names which passed the filter.
     * @param filter
     * @return
     * @throws IOException
     */
    public static Collection findResources(Filter filter) throws IOException {
        Collection resources = new ArrayList();
        // read the jars in the lib dir
        File dir = new File(Path.getAbsoluteFileSystemPath("WEB-INF/lib")); //$NON-NLS-1$
        if (dir.exists()) {
            File[] files = dir.listFiles(new FilenameFilter() {

                public boolean accept(File file, String name) {
                    return name.endsWith(".jar");
                }
            });

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File jarFile = files[i];
                    JarFile jar = new JarFile(jarFile);
                    for (Enumeration em = jar.entries(); em.hasMoreElements();) {
                        JarEntry entry = (JarEntry) em.nextElement();
                        if (filter.accept("/" + entry.getName())) {
                            resources.add("/" + entry.getName());
                        }
                    }
                }
            }
        }

        dir = new File(Path.getAbsoluteFileSystemPath("WEB-INF/classes"));
        if (dir.exists()) {
            Collection files = FileUtils.listFiles(dir, new TrueFileFilter() {
            }, new TrueFileFilter() {
            });
            for (Iterator iter = files.iterator(); iter.hasNext();) {
                File file = (File) iter.next();
                String name = StringUtils.substringAfter(file.getPath(), "WEB-INF/classes");
                if (filter.accept(name)) {
                    resources.add(name);
                }
            }
        }
        return resources;
    }

}
