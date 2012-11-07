/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.freemarker;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wraps the servlet context expecially for freemarker taglib resolution. This will trick freemarker TaglibFactory class
 * to "see" all jars in classpath as if they were jars in /WEB-INF/lib
 * @author Danilo Ghirardelli
 */
public class FreemarkerServletContextWrapper implements ServletContext {

    private ServletContext parentContext;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(FreemarkerServletContextWrapper.class);

    public FreemarkerServletContextWrapper(ServletContext parentServletContext) {
        // allow also a null parent context for unit tests
        this.parentContext = parentServletContext;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {

        URL result = parentContext.getResource(path);
        if (result == null) {
            // Trying the absolute path if the parent context fails.
            File file = new File(path);
            if ((file.exists()) && (file.isFile())) {
                result = file.toURI().toURL();
            }
        }
        return result;
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        InputStream is = parentContext.getResourceAsStream(path);
        if (is == null) {
            // Trying the absolute path if the parent context fails.
            File file = new File(path);
            if ((file.exists()) && (file.isFile())) {
                try {
                    return new FileInputStream(file);
                }
                catch (FileNotFoundException e) {
                    // Ignore, file not found
                }
            }
        }
        return is;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set getResourcePaths(String path) {
        if (StringUtils.equals(path, "/WEB-INF/lib")) {
            log.debug("returning resources from classpath");
            // Just when asking libraries, pass the classpath ones.
            final Set<String> resources = new HashSet<String>();
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            // if the classloader is an URLClassloader we have a better method for discovering resources
            // whis will also fetch files from jars outside WEB-INF/lib, useful during development
            if (cl instanceof URLClassLoader) {
                final URLClassLoader urlClassLoader = (URLClassLoader) cl;
                final URL[] urls = urlClassLoader.getURLs();
                for (int j = 0; j < urls.length; j++) {
                    final File tofile = sanitizeToFile(urls[j]);
                    if (tofile.isDirectory()) {
                        for (File file : ((List<File>) FileUtils.listFiles(tofile, null, true))) {
                            resources.add(file.getAbsolutePath());
                        }
                    }
                    else {
                        resources.add(tofile.getAbsolutePath());
                    }
                }

                return resources;
            }
            try {
                // be friendly to WAS developers too...
                // in development mode under RAD 7.5 here we have an instance of com.ibm.ws.classloader.WsClassLoader
                // and jars are NOT deployed to WEB-INF/lib by default, so they can't be found without this explicit
                // check
                //
                // but since we don't want to depend on WAS stuff we just check if the cl exposes a "classPath" property
                PropertyDescriptor pd = new PropertyDescriptor("classPath", cl.getClass());
                if (pd != null && pd.getReadMethod() != null) {
                    String classpath = (String) pd.getReadMethod().invoke(cl, new Object[]{});
                    if (StringUtils.isNotBlank(classpath)) {
                        String[] paths = StringUtils.split(classpath, File.pathSeparator);
                        for (int j = 0; j < paths.length; j++) {
                            final File tofile = new File(paths[j]);
                            // there can be several missing (optional?) paths here...
                            if (tofile.exists()) {
                                if (tofile.isDirectory()) {
                                    for (File file : ((List<File>) FileUtils.listFiles(tofile, null, true))) {
                                        resources.add(file.getAbsolutePath());
                                    }
                                }
                                else {
                                    resources.add(tofile.getAbsolutePath());
                                }
                            }
                        }
                        return resources;
                    }
                }
            }
            catch (Throwable e) {
                // no, it's not a classloader we can handle in a special way
            }
            // no way, we have to assume a standard war structure and look in the WEB-INF/lib and WEB-INF/classes dirs
            // read the jars in the lib dir
        }
        return parentContext.getResourcePaths(path);
    }

    /**
     * Clean url and get the file.
     * @param url url to cleanup
     * @return file to url
     */
    private File sanitizeToFile(URL url) {
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

    // Below here, just methods that redirects to the wrapped context.
    @Override
    public Object getAttribute(String name) {
        return parentContext.getAttribute(name);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getAttributeNames() {
        return parentContext.getAttributeNames();
    }

    @Override
    public ServletContext getContext(String uripath) {
        return parentContext.getContext(uripath);
    }

    @Override
    public String getContextPath() {
        return parentContext.getContextPath();
    }

    @Override
    public String getInitParameter(String name) {
        return parentContext.getInitParameter(name);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getInitParameterNames() {
        return parentContext.getInitParameterNames();
    }

    @Override
    public int getMajorVersion() {
        return parentContext.getMajorVersion();
    }

    @Override
    public String getMimeType(String file) {
        return parentContext.getMimeType(file);
    }

    @Override
    public int getMinorVersion() {
        return parentContext.getMinorVersion();
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return parentContext.getNamedDispatcher(name);
    }

    @Override
    public String getRealPath(String path) {
        return parentContext.getRealPath(path);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return parentContext.getRequestDispatcher(path);
    }

    @Override
    public String getServerInfo() {
        return parentContext.getServerInfo();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Servlet getServlet(String name) throws ServletException {
        return parentContext.getServlet(name);
    }

    @Override
    public String getServletContextName() {
        return parentContext.getServletContextName();
    }

    @SuppressWarnings({"rawtypes", "deprecation"})
    @Override
    public Enumeration getServletNames() {
        return parentContext.getServletNames();
    }

    @SuppressWarnings({"rawtypes", "deprecation"})
    @Override
    public Enumeration getServlets() {
        return parentContext.getServlets();
    }

    @Override
    public void log(String msg) {
        parentContext.log(msg);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void log(Exception exception, String msg) {
        parentContext.log(exception, msg);
    }

    @Override
    public void log(String message, Throwable throwable) {
        parentContext.log(message, throwable);
    }

    @Override
    public void removeAttribute(String name) {
        parentContext.removeAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object object) {
        parentContext.setAttribute(name, object);
    }
}
