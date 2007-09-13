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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class NodeDataStringComparator extends AbstractContentComparator {
    private final String nodeDataName;

    public NodeDataStringComparator(String nodeDataName) {
        this.nodeDataName = nodeDataName;
    }

    protected int compare(Content c1, Content c2) {
        final String s1 = c1.getNodeData(this.nodeDataName).getString();
        final String s2 = c2.getNodeData(this.nodeDataName).getString();

        return s1.compareTo(s2);
    }

}
