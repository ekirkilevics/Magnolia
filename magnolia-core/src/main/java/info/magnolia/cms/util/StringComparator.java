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
 * @author Sameer Charles
 * @version 1.1
 */
public class StringComparator implements Comparator {

    /**
     * Logger.
     */

    private String nodeDataName;

    public StringComparator(String nodeDataName) {
        this.nodeDataName = nodeDataName;
    }

    public int compare(Object o, Object o1) throws ClassCastException {
        String uri1;
        String uri2;

        uri1 = ((Content) o).getNodeData(this.nodeDataName).getString();
        uri2 = ((Content) o1).getNodeData(this.nodeDataName).getString();

        return uri1.compareTo(uri2);
    }
}
