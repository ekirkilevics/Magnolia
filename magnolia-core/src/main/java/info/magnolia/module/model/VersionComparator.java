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
package info.magnolia.module.model;

import java.util.Comparator;

/**
 * A simple Comparator that compares Version instances and ignores their classifier.
 * Does not handle nulls.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class VersionComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        final Version v1 = (Version) o1;
        final Version v2 = (Version) o2;
        if (v1.isStrictlyAfter(v2)) {
            return 1;
        } else if (v1.isEquivalent(v2)) {
            return 0;
        } else {
            return -1;
        }
    }
}
