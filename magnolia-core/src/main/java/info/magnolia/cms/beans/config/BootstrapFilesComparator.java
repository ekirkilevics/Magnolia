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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.ie.DataTransporter;

import java.io.File;
import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class BootstrapFilesComparator implements Comparator {

    // remove file with the same name in different dirs
    public int compare(Object file1obj, Object file2obj) {
        String file1 = file1obj instanceof File ? ((File) file1obj).getName() : StringUtils.substringAfterLast((String) file1obj, "/");
        String file2 = file2obj instanceof File ? ((File) file2obj).getName() : StringUtils.substringAfterLast((String) file2obj, "/");

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

    private static String getExtension(String filename) {
        String ext = StringUtils.substringAfterLast(filename, ".");
        if (("." + ext).equals(DataTransporter.GZ) || ("." + ext).equals(DataTransporter.ZIP)) {
            ext = StringUtils.substringAfterLast(StringUtils.substringBeforeLast(filename, "."), ".");
        }
        return ext;        
    }

    private static String getName(String filename) {
        String name = StringUtils.substringBeforeLast(filename, ".");
        if (name.endsWith(DataTransporter.XML) || name.endsWith(DataTransporter.PROPERTIES)) {
            name = StringUtils.substringBeforeLast(filename, ".");
        }
        return name;
    }
}
