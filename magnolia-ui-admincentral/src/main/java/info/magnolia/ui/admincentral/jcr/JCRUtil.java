/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.ui.admincentral.jcr;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * JCR Utilities.
 *
 * @deprecated temporary
 */
public class JCRUtil {

    public static final String PATH_SEPARATOR = "/";

    public static String getRelativePathToRoot(String fullPath) {
        if (!fullPath.startsWith(PATH_SEPARATOR)) {
            throw new IllegalArgumentException("path not relative to target");
        }
        return fullPath.substring(PATH_SEPARATOR.length());
    }

    public static String getItemIdWithoutPath(String fullPath) {
        if (!fullPath.startsWith(PATH_SEPARATOR)) {
            throw new IllegalArgumentException("path not relative to target");
        }
        return fullPath.substring(fullPath.lastIndexOf(PATH_SEPARATOR) + 1, fullPath.length());
    }

    public static String getPathWithoutItemId(String fullPath) {
        if (!fullPath.startsWith(PATH_SEPARATOR)) {
            throw new IllegalArgumentException("path not relative to target");
        }
        return fullPath.substring(fullPath.lastIndexOf(PATH_SEPARATOR));
    }

    /**
     * Mimics getUniqueLabel(Content,String) in Path.
     */
    public static String getUniqueLabel(Node node, String label) throws RepositoryException {
        while (node.hasNode(label) || node.hasProperty(label)) { //$NON-NLS-1$
            label = createUniqueName(label);
        }
        return label;
    }

    /**
     * Copy paste from Path.createUniqueName() only to use it form the method above.
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
        } else {
            pos++;
            base = baseName.substring(0, pos);
            if (pos == baseName.length()) {
                cnt = -1;
            } else {
                cnt = new Integer(baseName.substring(pos)).intValue();
            }
        }
        return (base + ++cnt);
    }

    public static String getPropertyString(Node node, String propertyName) throws RepositoryException {
        return getPropertyString(node, propertyName, "");
    }

    public static String getPropertyString(Node node, String propertyName, String defaultValue) throws RepositoryException {
        if (node.hasProperty(propertyName)) {
            return node.getProperty(propertyName).getString();
        }
        return defaultValue;
    }
}
