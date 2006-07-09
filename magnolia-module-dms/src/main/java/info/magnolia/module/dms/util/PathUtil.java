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
 *
 */
package info.magnolia.module.dms.util;

import org.apache.commons.lang.StringUtils;


/**
 * Used for the DMS to handle pathes
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class PathUtil {

    public static String createPath(String path, String label) {
        String res;
        if (StringUtils.isEmpty(path) || (path.equals("/"))) { //$NON-NLS-1$
            res = label;
        }
        else {
            res = path + "/" + label;
        }
        return addLeadingSlash(res);
    }

    public static String addLeadingSlash(String path) {
        if (!path.startsWith("/")) { //$NON-NLS-1$
            return "/" + path; //$NON-NLS-1$
        }
        return path;
    }

    public static String getFolder(String path) {
        String res;
        res = StringUtils.substringBeforeLast(path, "/"); //$NON-NLS-1$
        if (StringUtils.isEmpty(res)) {
            return "/"; //$NON-NLS-1$
        }
        return res;
    }

    public static String getFileName(String path) {
        if (path.indexOf("/") >= 0) {
            return StringUtils.substringAfterLast(path, "/");
        }
        else {
            return path;
        }
    }

}
