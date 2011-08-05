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
package info.magnolia.cms.servlets;

import info.magnolia.cms.util.ClasspathResourcesUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A simple spool servlet that load resources from the classpath. Resources folder is configurable via the servlet <code>resourcesRoot</code> init parameter.
 * If none is provided, it defaults to <code>mgnl-resources</code>. Files in this folder will be loaded by this servlet (corresponding to the mapped url
 * <code>/.resources/*</code>. This servlet should be used for authoring-only resources, like rich editor images and
 * scripts. It's not suggested for public website resources. Content length and last modification date are not set on
 * files returned from the classpath.
 *
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class ClasspathSpool extends HttpServlet {

    /**
     * Default root directory for resources streamed from the classpath. Resources folder is configurable via the servlet <code>resourcesRoot</code> init parameter.
     */
    public static final String MGNL_DEFAULT_RESOURCES_ROOT = "/mgnl-resources";

    private static final long serialVersionUID = 222L;

    private final static Logger log = LoggerFactory.getLogger(ClasspathSpool.class);

    private String resourcesRoot;

    @Override
    protected long getLastModified(HttpServletRequest req) {
        String filePath = this.getFilePath(req);
        try {
            URL url = ClasspathResourcesUtil.getResource(resourcesRoot + filePath);
            if(url != null){
                URLConnection connection = url.openConnection();

                connection.setDoInput(false);
                connection.setDoOutput(false);

                long lastModified = connection.getLastModified();
                InputStream is = null;
                try{
                    is = connection.getInputStream();
                }
                finally{
                    IOUtils.closeQuietly(is);
                }
                return lastModified;
            }
        }
        catch (IOException e) {
            // just ignore
        }

        return -1;
    }

    /**
     * All static resource requests are handled here.
     * @throws IOException for error in accessing the resource or the servlet output stream
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String filePath = getFilePath(request);

        if (StringUtils.contains(filePath, "*")) {
            streamMultipleFile(response, filePath);
        }
        else if (StringUtils.contains(filePath, "|")) {
            String[] paths = StringUtils.split(filePath, "|");
            streamMultipleFile(response, paths);
        }
        else {
            streamSingleFile(response, filePath);
        }
    }

    protected String getFilePath(HttpServletRequest request) {
        // handle includes
        String filePath = (String) request.getAttribute("javax.servlet.include.path_info");

        // handle forwards
        if (StringUtils.isEmpty(filePath)) {
            filePath = (String) request.getAttribute("javax.servlet.forward.path_info");
        }

        // standard request
        if (StringUtils.isEmpty(filePath)) {
            filePath = request.getPathInfo();
        }
        return filePath;
    }

    private Map<String, String[]> multipleFilePathsCache;

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        super.init();
        multipleFilePathsCache = new Hashtable<String, String[]>();
        resourcesRoot = StringUtils.defaultIfEmpty(getInitParameter("resourcesRoot"), MGNL_DEFAULT_RESOURCES_ROOT);
        //test if the folder is really there, else log warning and fall back to default.
        URL url = ClasspathResourcesUtil.getResource(resourcesRoot);
        log.debug("resources root is {}", resourcesRoot);
        if(url == null) {
            log.warn("Resource classpath root {} does not seem to exist. Some resources might not be available, please check your configuration. Falling back to default resouces root {}", resourcesRoot, MGNL_DEFAULT_RESOURCES_ROOT);
            // in case of misconfiguration, this should mitigate the risk of ending up with an unusable Magnolia instance.
            resourcesRoot = MGNL_DEFAULT_RESOURCES_ROOT;
        }
    }

    /**
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
        multipleFilePathsCache.clear();
    }

    /**
     * Join and stream multiple files, using a "regexp-like" pattern. Only a single "*" is allowed as keyword in the
     * request URI.
     */
    private void streamMultipleFile(HttpServletResponse response, String filePath) throws IOException {
        log.debug("aggregating files for request {}", filePath);

        String[] paths = multipleFilePathsCache.get(filePath);
        if (paths == null) {
            final String startsWith = resourcesRoot + StringUtils.substringBefore(filePath, "*");
            final String endsWith = StringUtils.substringAfterLast(filePath, "*");

            paths = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter() {

                @Override
                public boolean accept(String name) {
                    return name.startsWith(startsWith) && name.endsWith(endsWith);
                }
            });
        }
        multipleFilePathsCache.put(filePath, paths);

        if (paths.length == 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        streamMultipleFile(response, paths);
    }

    private void streamMultipleFile(HttpServletResponse response, String[] paths) throws IOException {
        ServletOutputStream out = response.getOutputStream();
        InputStream in = null;

        for (String path : paths) {
            try {
                if (!path.startsWith(resourcesRoot)) {
                    path = resourcesRoot + path;
                }
                in = ClasspathResourcesUtil.getStream(path);
                if (in != null) {
                    IOUtils.copy(in, out);
                }
            }
            finally {
                IOUtils.closeQuietly(in);
            }
        }

        out.flush();
        IOUtils.closeQuietly(out);
    }

    private void streamSingleFile(HttpServletResponse response, String filePath) throws IOException {
        InputStream in = null;
        // this method caches content if possible and checks the magnolia.develop property to avoid
        // caching during the development process
        try {
            in = ClasspathResourcesUtil.getStream(resourcesRoot + filePath);
        }
        catch (IOException e) {
            IOUtils.closeQuietly(in);
        }

        if (in == null) {
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            return;
        }

        try {
            ServletOutputStream out = response.getOutputStream();
            IOUtils.copy(in, out);
            out.flush();
            IOUtils.closeQuietly(out);
        }
        catch (IOException e) {
            // only log at debug level
            // tomcat usually throws a ClientAbortException anytime the user stop loading the page
            log.debug("Unable to spool resource due to a {} exception", e.getClass().getName());
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        finally {
            IOUtils.closeQuietly(in);
        }
    }

}
