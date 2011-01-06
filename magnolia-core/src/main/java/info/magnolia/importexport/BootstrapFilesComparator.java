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
package info.magnolia.importexport;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Comparator;

/**
 * Comparator implementation used to ensure bootstrap files are imported according to the name length (shortest first).
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
class BootstrapFilesComparator implements Comparator<File> {
    // remove file with the same name in different dirs
    public int compare(File file1, File file2) {
        String name1 = getName(file1);
        String name2 = getName(file2);

        String ext1 = getExtension(file1);
        String ext2 = getExtension(file2);

        if (StringUtils.equals(ext1, ext2)) {
            // a simple way to detect nested nodes
            if (name1.length() != name2.length()) {
                return name1.length() - name2.length();
            }
        } else {
            // import xml first
            if (ext1.equalsIgnoreCase("xml")) {
                return -1;
            } else if (ext2.equalsIgnoreCase("xml")) {
                return 1;
            }
        }

        return name1.compareTo(name2);
    }

    private static String getExtension(File file) {
        String ext = StringUtils.substringAfterLast(file.getName(), ".");
        if (("." + ext).equals(DataTransporter.GZ) || ("." + ext).equals(DataTransporter.ZIP)) {
            ext = StringUtils.substringAfterLast(StringUtils.substringBeforeLast(file.getName(), "."), ".");
        }
        return ext;
    }

    private static String getName(File file) {
        String name = StringUtils.substringBeforeLast(file.getName(), ".");
        if (name.endsWith(DataTransporter.XML) || name.endsWith(DataTransporter.PROPERTIES)) {
            name = StringUtils.substringBeforeLast(file.getName(), ".");
        }
        return name;
    }
}
