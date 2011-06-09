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

import org.apache.commons.lang.StringUtils;


/**
 * Utility class used to handle paths for documents in DMS.
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

    /**
     * Returns the extension of the file denoted by the path excluding the dot. More specifically returns the part of
     * the string after the last dot. If there's no dot in the path it returns the empty string.
     */
    public static String getExtension(String path) {
        if (path == null) {
            return null;
        }
        return StringUtils.substringAfterLast(getFileName(path), ".");
    }

    /**
     * Removes the extension from a path if one exists.
     */
    public static String stripExtension(String path) {
        return StringUtils.substringBeforeLast(path, ".");
    }
}
