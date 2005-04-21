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
package info.magnolia.cms.core;

import info.magnolia.cms.beans.runtime.SystemProperty;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.0
 */
public final class Path {

    private static final String ENCODING_DEFAULT = "UTF-8";

    private static final String ATTRIBUTE_URI = "mgnl_decodedURI";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Path.class);

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
        return isAbsolute(path) ? new File(path) : new File(Path.getAppRootDir(), path);
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
        return isAbsolute(path) ? new File(path) : new File(Path.getAppRootDir(), path);
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
     * Returns the URI of the current request, without the context path.
     * @param req request
     * @return request URI without servlet context
     */
    public static String getURI(HttpServletRequest req) {
        String uri = (String) req.getAttribute(ATTRIBUTE_URI);
        if (StringUtils.isEmpty(uri)) {
            // add to request avoiding unnecessary decoding
            uri = getDecodedURI(req);
            req.setAttribute(ATTRIBUTE_URI, uri);
        }
        return uri;
    }

    /**
     * Resets the existing URI request attribute
     * */
    public static void resetURI(HttpServletRequest req) {
        req.removeAttribute(ATTRIBUTE_URI);
    }

    /**
     * Returns the decoded URI of the current request, without the context path.
     * @param req request
     * @return request URI without servlet context
     */
    private static String getDecodedURI(HttpServletRequest req) {
        String encoding = StringUtils.defaultString(req.getCharacterEncoding(), ENCODING_DEFAULT);
        String decodedURL = null;
        try {
            decodedURL = URLDecoder.decode(req.getRequestURI(), encoding);
        }
        catch (UnsupportedEncodingException e) {
            decodedURL = req.getRequestURI();
        }
        return StringUtils.substringAfter(decodedURL, req.getContextPath());
    }

    public static String getExtension(HttpServletRequest req) {
        return StringUtils.substringAfterLast(req.getRequestURI(), ".");
    }

    public static String getUniqueLabel(HierarchyManager hierarchyManager, String parent, String label) {
        while (hierarchyManager.isExist(parent + "/" + label)) {
            label = createUniqueName(label);
        }
        return label;
    }

    private static boolean isAbsolute(String path) {
        if (path == null) {
            return false;
        }

        if (path.startsWith("/") || path.startsWith(File.separator)) {
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
                newLabel.append("-");
            }
        }
        if (newLabel.length() == 0) {
            newLabel.append("untitled");
        }
        return newLabel.toString();
    }

    /**
     * @param baseName
     * @return
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
        if (StringUtils.isEmpty(path) || (path.equals("/"))) {
            return "/" + label;
        }

        return path + "/" + label;
    }

    public static String getAbsolutePath(String path) {
        if (!path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    public static String getNodePath(String path, String label) {
        if (StringUtils.isEmpty(path) || (path.equals("/"))) {
            return label;
        }
        return getNodePath(path + "/" + label);
    }

    public static String getNodePath(String path) {
        if (path.startsWith("/")) {
            path = path.replaceFirst("/", StringUtils.EMPTY);
        }
        return path;
    }

    public static String getParentPath(String path) {
        int lastIndexOfSlash = path.lastIndexOf("/");
        if (lastIndexOfSlash > 0) {
            return path.substring(0, lastIndexOfSlash);
        }
        return "/";
    }
}
