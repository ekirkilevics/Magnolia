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

import info.magnolia.cms.beans.runtime.SystemProperty;
import info.magnolia.cms.core.HierarchyManager;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.0
 */
public final class Path {

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
     * <p>
     * Gets the cache directory path (cms.cache.startdir) as set with Java options while startup or in web.xml
     * </p>
     * @return Cache directory path
     */
    public static String getCacheDirectoryPath() {
        return SystemProperty.getProperty("magnolia.cache.startdir");
    }

    /**
     * <p>
     * Gets the temporary directory path (cms.upload.tmpdir) as set with Java options while startup or in web.xml
     * </p>
     * @return Temporary directory path
     */
    public static String getTempDirectoryPath() {
        return SystemProperty.getProperty("magnolia.upload.tmpdir");
    }

    /**
     * <p>
     * Gets log4j.properties file location as set with Java options while startup or in web.xml
     * </p>
     * @return log4j property file location
     */
    public static String getLogPropertiesFilePath() {
        return SystemProperty.getProperty("log4j.properties");
    }

    /**
     * <p>
     * Gets cms.exchange.history file location as set with Java options while startup or in web.xml
     * </p>
     * @return exchange history file location
     */
    public static String getHistoryFilePath() {
        return SystemProperty.getProperty("magnolia.exchange.history");
    }

    /**
     * <p>
     * Gets jcr.itemtypes file location as set with Java options while startup or in web.xml
     * </p>
     * @return supported JCR item types (Node types....) file location
     */
    public static String getJCRItemTypesFile() {
        return SystemProperty.getProperty("jcr.itemtypes");
    }

    /**
     * <p>
     * Gets repositories file location as set with Java options while startup or in web.xml
     * </p>
     * @return file location
     */
    public static String getRepositoriesConfigFilePath() {
        return SystemProperty.getProperty("magnolia.repositories.config");
    }

    /**
     * <p>
     * Gets repository factory config file location as set with Java options while startup or in web.xml
     * </p>
     * @return file location
     */
    public static String getRepositoryFactoryConfigFilePath() {
        return SystemProperty.getProperty("Repository.factory.config");
    }

    public static String getURI(HttpServletRequest req) {
        return req.getRequestURI();
    }

    public static String getExtension(HttpServletRequest req) {
        int lastIndexOfDot = Path.getURI(req).lastIndexOf(".");
        if (lastIndexOfDot > -1) {
            return req.getRequestURI().substring(lastIndexOfDot + 1);
        }
        return "";
    }

    /**
     * @deprecated
     */
    public static String getUniqueLabel(String parent, String label) {
        log.error("Deprecated - use getUniqueLabel(Content parent, String label) instead");
        return label;
    }

    public static String getUniqueLabel(HierarchyManager hierarchyManager, String parent, String label) {
        while (hierarchyManager.isExist(parent + "/" + label)) {
            label = createUniqueName(label);
        }
        return label;
    }

    /**
     * <p>
     * Replace illegal characters by [_] [0-9], [A-Z], [a-z], [-], [_]
     * </p>
     * @param label: label to validate
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
        if (path == null || (path.equals("")) || (path.equals("/"))) {
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
        if (path == null || (path.equals("")) || (path.equals("/"))) {
            return label;
        }
        return getNodePath(path + "/" + label);
    }

    public static String getNodePath(String path) {
        if (path.startsWith("/")) {
            path = path.replaceFirst("/", "");
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
