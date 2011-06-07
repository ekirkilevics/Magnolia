/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Util to find resources in the classpath (WEB-INF/lib and WEB-INF/classes).
 * @author Philipp Bracher
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class ClasspathResourcesUtil {
    private static final Logger log = LoggerFactory.getLogger(ClasspathResourcesUtil.class);

    /**
     * Filter for filtering the resources.
     * @author Philipp Bracher
     * @version $Revision$ ($Author$)
     */
    public static interface Filter {
        public boolean accept(String name);
    }

    /**
     * A filter using a regex pattern.
     */
    public static class PatternFilter implements Filter{
        private final Pattern pattern;

        public PatternFilter(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        @Override
        public boolean accept(String name) {
            return pattern.matcher(name).matches();
        }
    }

    private static boolean isCache() {
        final String devMode = SystemProperty.getProperty("magnolia.develop");
        return !"true".equalsIgnoreCase(devMode);
    }

    /**
     * Return a collection containing the resource names which match the regular expression.
     * @return string array of found resources TODO : (lazy) cache ?
     */
    public static String[] findResources(String regex) {
        return findResources(new PatternFilter(regex));
    }

    /**
     * Return a collection containing the resource names which passed the filter.
     * @param filter
     * @return string array of found resources TODO : (lazy) cache ?
     */
    public static String[] findResources(Filter filter) {
        final Set<String> resources = new HashSet<String>();
        final ClassLoader cl = getCurrentClassLoader();

        // if the classloader is an URLClassloader we have a better method for discovering resources
        // whis will also fetch files from jars outside WEB-INF/lib, useful during development
        if (cl instanceof URLClassLoader) {
            final URLClassLoader urlClassLoader = (URLClassLoader) cl;
            final URL[] urls = urlClassLoader.getURLs();
            if(log.isDebugEnabled()){
                log.debug("Loading resources from: " + ArrayUtils.toString(urls));
            }
            if (urls.length == 1 && urls[0].getPath().endsWith("WEB-INF/classes/")) {
                // working around MAGNOLIA-2577
                log.warn("Looks like we're in a JBoss 5 expanded war directory, will attempt to load resources from the file system instead; see MAGNOLIA-2577.");
            } else {
                collectFromURLs(resources, urls, filter);
                return resources.toArray(new String[resources.size()]);
            }
        }

        try {
            // be friendly to WAS developers too...
            // in development mode under RAD 7.5 here we have an instance of com.ibm.ws.classloader.WsClassLoader
            // and jars are NOT deployed to WEB-INF/lib by default, so they can't be found without this explicit check
            //
            // but since we don't want to depend on WAS stuff we just check if the cl exposes a "classPath" property
            String classpath = BeanUtils.getProperty(cl, "classPath");

            if (StringUtils.isNotEmpty(classpath)) {
                collectFromClasspathString(resources, classpath, filter);
                return resources.toArray(new String[resources.size()]);
            }
        }
        catch (Throwable e) {
            // no, it's not a classloader we can handle in a special way
        }

        // no way, we have to assume a standard war structure and look in the WEB-INF/lib and WEB-INF/classes dirs
        // read the jars in the lib dir
        collectFromFileSystem(filter, resources);
        return resources.toArray(new String[resources.size()]);
    }

    protected static void collectFromURLs(Collection<String> resources, URL[] urls, Filter filter) {
        // tomcat classloader is org.apache.catalina.loader.WebappClassLoader
        for (int j = 0; j < urls.length; j++) {
            final File tofile = sanitizeToFile(urls[j]);
            collectFiles(resources, tofile, filter);
        }
    }

    protected static void collectFromClasspathString(Collection<String> resources, String classpath, Filter filter) {
        String[] paths = classpath.split(File.pathSeparator);
        for (int j = 0; j < paths.length; j++) {
            final File tofile = new File(paths[j]);
            // there can be several missing (optional?) paths here...
            if (tofile.exists()) {
                collectFiles(resources, tofile, filter);
            }
        }
    }

    protected static void collectFromFileSystem(Filter filter, Collection<String> resources) {
        File dir = new File(Path.getAbsoluteFileSystemPath("WEB-INF/lib")); //$NON-NLS-1$
        if (dir.exists()) {
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String name) {
                    return name.endsWith(".jar");
                }
            });

            for (int i = 0; i < files.length; i++) {
                collectFiles(resources, files[i], filter);
            }
        }

        // read files in WEB-INF/classes
        File classFileDir = new File(Path.getAbsoluteFileSystemPath("WEB-INF/classes"));
        if (classFileDir.exists()) {
            collectFiles(resources, classFileDir, filter);
        }
    }

    protected static File sanitizeToFile(URL url) {
        try {
            String fileUrl = url.getFile();
            // needed because somehow the URLClassLoader has encoded URLs, and getFile does not decode them.
            fileUrl = URLDecoder.decode(fileUrl, "UTF-8");
            // needed for Resin - for some reason, its URLs are formed as jar:file:/absolutepath/foo/bar.jar instead of
            // using the :///abs.. notation
            fileUrl = StringUtils.removeStart(fileUrl, "file:");
            fileUrl = StringUtils.removeEnd(fileUrl, "!/");
            return new File(fileUrl);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load resources from jars or directories.
     * @param resources found resources will be added to this collection
     * @param jarOrDir a File, can be a jar or a directory
     * @param filter used to filter resources
     */
    private static void collectFiles(Collection<String> resources, File jarOrDir, Filter filter) {

        if (!jarOrDir.exists()) {
            log.warn("missing file: {}", jarOrDir.getAbsolutePath());
            return;
        }

        if (jarOrDir.isDirectory()) {
            log.debug("looking in dir {}", jarOrDir.getAbsolutePath());

            Collection<File> files = FileUtils.listFiles(jarOrDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
            for (File file : files) {
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
            for (Enumeration<JarEntry> em = jar.entries(); em.hasMoreElements();) {
                JarEntry entry = em.nextElement();
                if (!entry.isDirectory()) {
                    if (filter.accept("/" + entry.getName())) {
                        resources.add("/" + entry.getName());
                    }
                }
            }
            try {
                jar.close();
            }
            catch (IOException e) {
                log.error("Failed to close jar file : " + e.getMessage());
                log.debug("Failed to close jar file", e);
            }
        }
        else {
            log.debug("Unknown (not jar) file in classpath: {}, skipping.", jarOrDir.getName());
        }

    }

    public static InputStream getStream(String name) throws IOException {
        return getStream(name, isCache());
    }

    /**
     * Checks last modified and returns the new content if changed and the cache flag is not set to true.
     * @param name
     * @return the input stream
     * @throws IOException
     */
    public static InputStream getStream(String name, boolean cache) throws IOException {
        if (cache) {
            return getCurrentClassLoader().getResourceAsStream(StringUtils.removeStart(name, "/"));
        }

        // TODO use the last modified attribute
        URL url = getResource(name);
        if (url != null) {
            return url.openStream();
        }

        log.debug("Can't find {}", name);
        return null;
    }

    /**
     * Get the class loader of the current thread.
     * @return current classloader
     */
    private static ClassLoader getCurrentClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Get the resource using the current class loader. The leading / is removed as the call to class.getResource()
     * would do.
     * @param name
     * @return the resource
     */
    public static URL getResource(String name) {
        return getCurrentClassLoader().getResource(StringUtils.removeStart(name, "/"));
    }

}
