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
package info.magnolia.cms.exchange.simple;

import info.magnolia.cms.core.ContentNode;

import java.io.Serializable;

import org.apache.log4j.Logger;


/**
 * Date: Jun 21, 2004 Time: 2:32:14 PM
 * @author Sameer Charles
 * @version 2.0
 */
public class SerializableContentNode extends SerializableContent implements Serializable {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(SerializableContentNode.class);

    private SerializableMetaData metaData;

    private boolean recurse;

    public SerializableContentNode(ContentNode baseContentNode) {
        this.makeSerializable(baseContentNode);
    }

    public SerializableContentNode(ContentNode baseContentNode, boolean recurse) {
        this.recurse = recurse;
        this.makeSerializable(baseContentNode);
    }

    private void makeSerializable(ContentNode baseContentNode) {
        this.setName(baseContentNode.getName());
        this.metaData = new SerializableMetaData(baseContentNode.getMetaData());
        /* add top level node list */
        this.addNodeDataList(baseContentNode);
        if (this.recurse) {
            this.addContentNodeList(baseContentNode, true);
        }
    }

    public SerializableMetaData getMetaData() {
        return this.metaData;
    }
}
