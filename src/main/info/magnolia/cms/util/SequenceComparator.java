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

import info.magnolia.cms.core.Content;
import java.util.Comparator;


/**
 * @author Vinzenz Wyser
 * @version 1.1
 */
public class SequenceComparator implements Comparator {

    public int compare(Object o0, Object o1) throws ClassCastException {
        try {
            long pos0 = (((Content) o0).getMetaData().getSequencePosition());
            long pos1 = (((Content) o1).getMetaData().getSequencePosition());
            String s0 = "0";
            String s1 = "0";
            if (pos0 > pos1)
                s0 = "1";
            else if (pos0 < pos1)
                s1 = "1";
            return s0.compareTo(s1);
        }
        catch (Exception e) {
            return 0;
        }
    }
}
