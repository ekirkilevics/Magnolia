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
import info.magnolia.cms.core.SystemProperty;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Util to find resources in the classpath (WEB-INF/lib and WEB-INF/classes).
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ClasspathResourcesUtil {
    
    private static boolean nocache = BooleanUtils.toBoolean(SystemProperty.getProperty("magnolia.debug"));
    
    /**
     * logger
     */
    private static Logger log = LoggerFactory.getLogger(ClasspathResourcesUtil.class);

    /**
     * Filter for filtering the resources.
     * @author Philipp Bracher
     * @version $Revision$ ($Author$)
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
    public static String[] findResources(Filter filter) {

        Collection resources = new ArrayList();

        ClassLoader cl = ClasspathResourcesUtil.class.getClassLoader();

        // if the classloader is an URLClassloader we have a better method for discovering resources
        // whis will also fetch files from jars outside WEB-INF/lib, useful during development
        if (cl instanceof URLClassLoader) {
            // tomcat classloader is org.apache.catalina.loader.WebappClassLoader
            URL[] urls = ((URLClassLoader) cl).getURLs();

            for (int j = 0; j < urls.length; j++) {
                URL url = urls[j];
                File tofile = new File(url.getFile());
                collectFiles(resources, tofile, filter);
            }
        }
        else {

            // no way, we have to assume a standard war structure and look in the WEB-INF/lib and WEB-INF/classes dirs

            // read the jars in the lib dir
            File dir = new File(Path.getAbsoluteFileSystemPath("WEB-INF/lib")); //$NON-NLS-1$
            if (dir.exists()) {
                File[] files = dir.listFiles(new FilenameFilter() {

                    public boolean accept(File file, String name) {
                        return name.endsWith(".jar");
                    }
                });

                for (int i = 0; i < files.length; i++) {
                    collectFiles(resources, files[i], filter);
                }
            }

            // read files in WEB-INF/classes
            collectFiles(resources, new File(Path.getAbsoluteFileSystemPath("WEB-INF/classes")), filter);
        }

        return (String[]) resources.toArray(new String[resources.size()]);
    }

    /**
     * Load resources from jars or directories
     * @param resources found resources will be added to this collection
     * @param jarOrDir a File, can be a jar or a directory
     * @param filter used to filter resources
     */
    private static void collectFiles(Collection resources, File jarOrDir, Filter filter) {

        if (!jarOrDir.exists()) {
            log.warn("missing file: {}", jarOrDir.getAbsolutePath());
            return;
        }

        if (jarOrDir.isDirectory()) {
            log.debug("looking in dir {}", jarOrDir.getAbsolutePath());

            Collection files = FileUtils.listFiles(jarOrDir, new TrueFileFilter() {
            }, new TrueFileFilter() {
            });
            for (Iterator iter = files.iterator(); iter.hasNext();) {
                File file = (File) iter.next();
                String name = StringUtils.substringAfter(file.getPath(), jarOrDir.getPath());

                // please, be kind to Windows!!!
                name = StringUtils.replace(name, "\\", "/");
                if (!name.startsWith("/")) {
                    name = "/" + name;
                }

                if (filter.accept(name)) {
                    resources.add(name);
                }
            }
        }
        else if (jarOrDir.getName().endsWith(".jar")) {
            log.debug("looking in jar {}", jarOrDir.getAbsolutePath());
            JarFile jar;
            try {
                jar = new JarFile(jarOrDir);
            }
            catch (IOException e) {
                log.error("IOException opening file {}, skipping", jarOrDir.getAbsolutePath());
                return;
            }
            for (Enumeration em = jar.entries(); em.hasMoreElements();) {
                JarEntry entry = (JarEntry) em.nextElement();
                if (filter.accept("/" + entry.getName())) {
                    resources.add("/" + entry.getName());
                }
            }
        }
        else {
            log.debug("Unknown (not jar) file in classpath: {}, skipping.", jarOrDir.getName());
        }

    }
    
    /**
     * Checks last modified and returns the new content if changed.
     * @param name
     * @return the input stream
     * @throws IOException
     */
    public static InputStream getStream(String name) throws IOException{
        if(nocache){
            // TODO use the last modified attribute
            URL url = ClasspathResourcesUtil.class.getResource(name);
            return url.openStream();
        }
        else{
            return ClasspathResourcesUtil.class.getResourceAsStream(name);
        }
    }
 
}
