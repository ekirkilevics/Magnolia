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
package info.magnolia.cms.gui.dialog;

import java.util.Comparator;
import java.util.Hashtable;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogWebDAVComparator implements Comparator {

    public int compare(Object o, Object o1) throws ClassCastException {
        String s1 = ((String) ((Hashtable) o).get("name")).toLowerCase();
        String s2 = ((String) ((Hashtable) o1).get("name")).toLowerCase();
        return s1.compareTo(s2);
    }
}
