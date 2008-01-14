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
package info.magnolia.cms.core;

import info.magnolia.context.MgnlContext;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.safehaus.uuid.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version 2.0 $Id$
 */
public final class Path {

    public static Logger log = LoggerFactory.getLogger(Path.class);

    /**
     * @deprecated not used anymore
     */
    public static final String JAVAX_FORWARD_SERVLET_PATH = "javax.servlet.forward.servlet_path"; //$NON-NLS-1$

    /**
     * The current set URI. This is the URI after the virtual uri mapping
     * @deprecated use AggregationState.getCurrentURI()
     */
    public static final String MGNL_REQUEST_URI_CURRENT = "mgnl.request.uri.current"; //$NON-NLS-1$

    /**
     * @deprecated use MGNL_REQUEST_URI_CURRENT
     */
    public static final String MGNL_REQUEST_URI_DECODED = MGNL_REQUEST_URI_CURRENT; //$NON-NLS-1$

    /**
     * The original request URI. Once set never overwritten
     * @deprecated use AggregationState.getOriginalURI()
     */
    public static final String MGNL_REQUEST_URI_ORIGINAL = "mgnl.request.uri.original"; //$NON-NLS-1$

    /**
     * New unlabeled nodes default name
     */
    private static final String DEFAULT_UNTITLED_NODE_NAME = "untitled";

    /**
     * Default encoding is UTF-8
     */
    private static final String ENCODING_DEFAULT = "UTF-8"; //$NON-NLS-1$

    /**
     * Utility class, don't instantiate.
     */
    private Path() {
        // unused
    }

    /**
     * Gets the cache directory path (cms.cache.startdir) as set with Java options while startup or in web.xml.
     * @return Cache directory path
     */
    public static String getCacheDirectoryPath() {
        return getCacheDirectory().getAbsolutePath();
    }

    public static File getCacheDirectory() {
        String path = SystemProperty.getProperty(SystemProperty.MAGNOLIA_CACHE_STARTDIR);
        File dir = isAbsolute(path) ? new File(path) : new File(Path.getAppRootDir(), path);
        dir.mkdirs();
        return dir;
    }

    /**
     * Gets the temporary directory path (cms.upload.tmpdir) as set with Java options while startup or in web.xml.
     * @return Temporary directory path
     */
    public static String getTempDirectoryPath() {
        return getTempDirectory().getAbsolutePath();
    }

    public static File getTempDirectory() {
        String path = SystemProperty.getProperty(SystemProperty.MAGNOLIA_UPLOAD_TMPDIR);
        File dir = isAbsolute(path) ? new File(path) : new File(Path.getAppRootDir(), path);
        dir.mkdirs();
        return dir;
    }

    /**
     * Gets cms.exchange.history file location as set with Java options while startup or in web.xml.
     * @return exchange history file location
     */
    public static String getHistoryFilePath() {
        return getHistoryFile().getAbsolutePath();
    }

    public static File getHistoryFile() {
        String path = SystemProperty.getProperty(SystemProperty.MAGNOLIA_EXCHANGE_HISTORY);
        return isAbsolute(path) ? new File(path) : new File(Path.getAppRootDir(), path);
    }

    /**
     * Gets repositories file location as set with Java options while startup or in web.xml.
     * @return file location
     */
    public static String getRepositoriesConfigFilePath() {
        return getRepositoriesConfigFile().getAbsolutePath();
    }

    public static File getRepositoriesConfigFile() {
        String path = SystemProperty.getProperty(SystemProperty.MAGNOLIA_REPOSITORIES_CONFIG);
        return isAbsolute(path) ? new File(path) : new File(Path.getAppRootDir(), path);
    }

    /**
     * Gets the root directory for the magnolia web application.
     * @return magnolia root dir
     */
    public static File getAppRootDir() {
        return new File(SystemProperty.getProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR));
    }

    /**
     * Gets absolute filesystem path, adds application root if path is not absolute
     */
    public static String getAbsoluteFileSystemPath(String path) {
        if (Path.isAbsolute(path)) {
            return path;
        }
        // using the file() constructor will allow relative paths in the form ../../apps
        return new File(Path.getAppRootDir(), path).getAbsolutePath();
    }

    /**
     * @deprecated do not pass the request
     */
    public static String getURI(HttpServletRequest req) {
        return getURI();
    }

    /**
     * Returns the URI of the current request, without the context path.
     * This is the URI after the virtual uri mapping.
     * @return request URI without servlet context
     * @deprecated use AggregationState.getCurrentURI()
     */
    public static String getURI() {
        return MgnlContext.getAggregationState().getCurrentURI();
    }

    /**
     * @deprecated do not pass the request
     */
    public static void setURI(String uri, HttpServletRequest req) {
        setURI(uri);
    }

    /**
     * Set the current URI. If the original URI was not set, this URI is set as the original URI too.
     * @param uri
     * @deprecated use AggregationState
     */
    public static void setURI(String uri) {
        MgnlContext.getAggregationState().setCurrentURI(uri);
    }

    /**
     * @deprecated do not pass the request
     */
    public static String getHandle(HttpServletRequest req) {
        return MgnlContext.getAggregationState().getHandle();
    }

    /**
     * Returns the URI of the current request, but uses the uri to repository mapping to remove any prefix.
     * @return request URI without servlet context and without repository mapping prefix
     * @deprecated Use {@link info.magnolia.cms.core.AggregationState#getHandle()} instead
     */
    public static String getHandle() {
        return MgnlContext.getAggregationState().getHandle();
    }

    /**
     * @deprecated do not pass the request
     */
    public static String getExtension(HttpServletRequest req) {
        return getExtension();
    }

    /**
     * Get the current extesion of the request
     * @return
     * @deprecated Use {@link info.magnolia.cms.core.AggregationState#getExtension()} instead
     */
    public static String getExtension() {
        return MgnlContext.getAggregationState().getExtension();
    }

    /**
     * @deprecated do not pass the request
     */
    public static String getOriginalURI(HttpServletRequest req) {
        return getOriginalURI();
    }

    /**
     * This is the URI (without context) when the request started.
     * @deprecated use AggregationState.getOriginalURI()
     */
    public static String getOriginalURI() {
        return MgnlContext.getAggregationState().getOriginalURI();
    }

    /**
     * URI is only set if there was no original uri set
     * @param uri
     *
     *
     * @deprecated use AggregationState
     */
    public static void setOriginalURI(String uri) {
        MgnlContext.getAggregationState().setOriginalURI(uri);
    }

    /**
     * Decodes the URI with the passed encoding and removes the context path.
     * @return URI without servlet context
     * TODO : move to AggregationState ?
     */
    public static String decodedURI(String uri, String encoding) {
        String decodedURL = null;
        try {
            decodedURL = URLDecoder.decode(uri, encoding);
        }
        catch (UnsupportedEncodingException e) {
            decodedURL = uri;
        }
        return StringUtils.removeStart(decodedURL, MgnlContext.getContextPath());
    }

    /**
     * Use the requests encoding
     * @param uri
     * @return
     *
     * @deprecated pass an encoding
     */
    public static String decodedURI(String uri) {
        String encoding = StringUtils.defaultString(MgnlContext.getAggregationState().getCharacterEncoding(), ENCODING_DEFAULT);
        return decodedURI(uri, encoding);
    }

    public static String getUniqueLabel(HierarchyManager hierarchyManager, String parent, String label) {
        if (parent.equals("/")) { //$NON-NLS-1$
            parent = StringUtils.EMPTY;
        }
        while (hierarchyManager.isExist(parent + "/" + label)) { //$NON-NLS-1$
            label = createUniqueName(label);
        }
        return label;
    }

    public static String getUniqueLabel(Content parent, String label) {
        try {
            while (parent.hasContent(label) || parent.hasNodeData(label)) { //$NON-NLS-1$
                label = createUniqueName(label);
            }
        }
        catch (RepositoryException e) {
            label = UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
        }
        return label;
    }

    protected static boolean isAbsolute(String path) {

        if (path == null) {
            return false;
        }

        if (path.startsWith("/") || path.startsWith(File.separator)) { //$NON-NLS-1$
            return true;
        }

        // windows c:
        if (path.length() >= 3 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':') {
            return true;
        }

        return false;
    }

    /**
     * <p>
     * Replace illegal characters by [_] [0-9], [A-Z], [a-z], [-], [_]
     * </p>
     * @param label label to validate
     * @return validated label
     */
    public static String getValidatedLabel(String label) {
        StringBuffer s = new StringBuffer(label);
        StringBuffer newLabel = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            int charCode = s.charAt(i);
            // charCodes: 48-57: [0-9]; 65-90: [A-Z]; 97-122: [a-z]; 45: [-]; 95:[_]
            if (((charCode >= 48) && (charCode <= 57))
                || ((charCode >= 65) && (charCode <= 90))
                || ((charCode >= 97) && (charCode <= 122))
                || charCode == 45
                || charCode == 95) {
                newLabel.append(s.charAt(i));
            }
            else {
                newLabel.append("-"); //$NON-NLS-1$
            }
        }
        if (newLabel.length() == 0) {
            newLabel.append(DEFAULT_UNTITLED_NODE_NAME);
        }
        return newLabel.toString();
    }

    /**
     * @param baseName
     */
    private static String createUniqueName(String baseName) {
        int pos;
        for (pos = baseName.length() - 1; pos >= 0; pos--) {
            char c = baseName.charAt(pos);
            if (c < '0' || c > '9') {
                break;
            }
        }
        String base;
        int cnt;
        if (pos == -1) {
            if (baseName.length() > 1) {
                pos = baseName.length() - 2;
            }
        }
        if (pos == -1) {
            base = baseName;
            cnt = -1;
        }
        else {
            pos++;
            base = baseName.substring(0, pos);
            if (pos == baseName.length()) {
                cnt = -1;
            }
            else {
                cnt = new Integer(baseName.substring(pos)).intValue();
            }
        }
        return (base + ++cnt);
    }

    public static String getAbsolutePath(String path, String label) {
        if (StringUtils.isEmpty(path) || (path.equals("/"))) { //$NON-NLS-1$
            return "/" + label; //$NON-NLS-1$
        }

        return path + "/" + label; //$NON-NLS-1$
    }

    public static String getAbsolutePath(String path) {
        if (!path.startsWith("/")) { //$NON-NLS-1$
            return "/" + path; //$NON-NLS-1$
        }
        return path;
    }

    public static String getNodePath(String path, String label) {
        if (StringUtils.isEmpty(path) || (path.equals("/"))) { //$NON-NLS-1$
            return label;
        }
        return getNodePath(path + "/" + label); //$NON-NLS-1$
    }

    public static String getNodePath(String path) {
        if (path.startsWith("/")) { //$NON-NLS-1$
            return path.replaceFirst("/", StringUtils.EMPTY); //$NON-NLS-1$
        }
        return path;
    }

    public static String getParentPath(String path) {
        int lastIndexOfSlash = path.lastIndexOf("/"); //$NON-NLS-1$
        if (lastIndexOfSlash > 0) {
            return StringUtils.substringBefore(path, "/"); //$NON-NLS-1$
        }
        return "/"; //$NON-NLS-1$
    }
}
