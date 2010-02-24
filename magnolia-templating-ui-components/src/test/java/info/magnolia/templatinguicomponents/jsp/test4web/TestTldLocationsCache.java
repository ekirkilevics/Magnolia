/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.templatinguicomponents.jsp.test4web;
/**
 *
 * Openutils web test utils (http://www.openmindlab.com/lab/products/testing4web.html)
 * Copyright(C) 2008-2010, Openmind S.r.l. http://www.openmindonline.it
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.TldLocationsCache;
import org.apache.jasper.xmlparser.ParserUtils;
import org.apache.jasper.xmlparser.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author fgiust
 * @version $Id$
 */
public class TestTldLocationsCache extends TldLocationsCache {

    private boolean initialized;

    private Map<String, String[]> filemappings;

    /**
     * Logger.
     */
    private Logger log = LoggerFactory.getLogger(TestTldLocationsCache.class);

    private final ServletContext ctxt2;

    /**
     * @param ctxt
     */
    public TestTldLocationsCache(ServletContext ctxt) {
        super(ctxt);
        ctxt2 = ctxt;
    }

    /**
     * @param ctxt
     * @param redeployMode
     */
    public TestTldLocationsCache(ServletContext ctxt, boolean redeployMode) {
        super(ctxt, redeployMode);
        ctxt2 = ctxt;
    }

    public String[] getLocation(String uri) throws JasperException {
        if (!initialized) {
            initFilesInClasspath();
        }

        String[] location = super.getLocation(uri);

        if (location != null) {
            return location;
        }


        return filemappings.get(uri);
    }

    /**
     *
     */
    private void initFilesInClasspath() {
        filemappings = new HashMap<String, String[]>();

        ClassLoader webappLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = webappLoader;

        while (loader != null) {
            if (loader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) loader).getURLs();
                for (int i = 0; i < urls.length; i++) {
                    URL url = urls[i];
                    if (!StringUtils.endsWithIgnoreCase(url.getFile(), ".jar")) {
                        File file = new File(url.getFile());
                        if (file.isDirectory()) {
                            log.debug("Processing {}", url.getFile());
                            findTld(file);
                        }
                    }
                }
            }

            loader = loader.getParent();
        }

    }

    private void findTld(File f) {
        File[] list = f.listFiles();
        for (File file : list) {
            if (file.isDirectory()) {
                findTld(file);
            } else if (StringUtils.endsWithIgnoreCase(file.getName(), ".tld")) {

                String uri = getUriFromTld(file);
                if (uri != null && filemappings.get(uri) == null) {
                    String base = ctxt2.getRealPath("/");

                    String filepath = getRelativePath(new File(base), file);

                    log.debug("Adding {} to cache with url {}", uri, filepath);
                    filemappings.put(uri, new String[]{filepath, null});
                }
            }
        }
    }

    private String getUriFromTld(File file) {
        String resourcePath = file.getAbsolutePath();
        InputStream in;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
        }
        catch (FileNotFoundException e) {
            log.warn("File {} not found", file.getAbsolutePath());
            return null;
        }
        try {
            TreeNode tld = new ParserUtils().parseXMLDocument(resourcePath, in);
            TreeNode uri = tld.findChild("uri");
            if (uri != null) {
                String body = uri.getBody();
                if (body != null) {
                    return body;
                }
            }
        }
        catch (JasperException e) {
            log.error("Error processing " + file.getAbsolutePath(), e);
            return null;
        }
        finally {
            IOUtils.closeQuietly(in);
        }

        return null;
    }

    /**
     * break a path down into individual elements and add to a list. example : if a path is /a/b/c/d.txt, the breakdown
     * will be [d.txt,c,b,a]
     * @param f input file
     * @return a List collection with the individual elements of the path in reverse order
     */
    private static List getPathList(File f) {
        List l = new ArrayList();
        File r;
        try {
            r = f.getCanonicalFile();
            while (r != null) {
                l.add(r.getName());
                r = r.getParentFile();
            }
        }
        catch (IOException e) {
            l = null;
        }
        return l;
    }

    /**
     * figure out a string representing the relative path of 'f' with respect to 'r'
     * @param r home path
     * @param f path of file
     */
    private static String matchPathLists(List r, List f) {
        int i;
        int j;
        String s;
        // start at the beginning of the lists
        // iterate while both lists are equal
        s = "";
        i = r.size() - 1;
        j = f.size() - 1;

        // first eliminate common root
        while ((i >= 0) && (j >= 0) && (r.get(i).equals(f.get(j)))) {
            i--;
            j--;
        }

        // for each remaining level in the home path, add a ..
        for (; i >= 0; i--) {
            s += ".." + File.separator;
        }

        // for each level in the file path, add the path
        for (; j >= 1; j--) {
            s += f.get(j) + File.separator;
        }

        // file name
        s += f.get(j);
        return s;
    }

    /**
     * get relative path of File 'f' with respect to 'home' directory example : home = /a/b/c f = /a/d/e/x.txt s =
     * getRelativePath(home,f) = ../../d/e/x.txt
     * @param home base path, should be a directory, not a file, or it doesn't make sense
     * @param f file to generate path for
     * @return path from home to f as a string
     */
    public static String getRelativePath(File home, File f) {
        File r;
        List homelist;
        List filelist;
        String s;

        homelist = getPathList(home);
        filelist = getPathList(f);
        s = matchPathLists(homelist, filelist);

        return s;
    }

}