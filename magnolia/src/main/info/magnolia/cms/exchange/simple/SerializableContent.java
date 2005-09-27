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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;


/**
 * Date: Jun 21, 2004 Time: 11:42:04 AM
 * @author Sameer Charles
 * @version 2.0
 */
public class SerializableContent implements Serializable {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(SerializableContent.class);

    /**
     * NodeData collection.
     */
    protected ArrayList nodeDataCollection = new ArrayList();

    /**
     * Sub ContentNode collection.
     */
    protected ArrayList contentNodeCollection = new ArrayList();

    /**
     * Sub Content collection.
     */
    protected ArrayList contentCollection = new ArrayList();

    protected SerializableMetaData metaData;

    private String name;

    private boolean recurse;

    /**
     * If true, all subnodes of type CONTENTNODE are included
     */
    private boolean includeContentNodes;

    public SerializableContent() {
        this.includeContentNodes = true;
    }

    public SerializableContent(Content content) {
        this();
        this.makeSerializable(content);
    }

    /**
     * Serialize a node of type CONTENT
     * @param content the node to serialize
     * @param recurse true if also subnodes of type CONTENT gets serialized)
     * @param includeContentNodes false if no subnodes of type CONTENTNODE should get serialized. If recurse is true,
     * this is irelevant
     */
    public SerializableContent(Content content, boolean recurse, boolean includeContentNodes) {
        this();
        this.recurse = recurse;
        this.includeContentNodes = includeContentNodes;
        this.makeSerializable(content);
    }

    private void makeSerializable(Content content) {
        this.setName(content.getName());
        this.metaData = new SerializableMetaData(content.getMetaData());
        this.addNodeDataList(content);
        if (this.recurse || this.includeContentNodes) {
            this.addContentNodeList(content, true);
        }
        if (this.recurse) {
            this.addContentList(content, true, true);
        }
    }

    public ArrayList getContentNodeCollection() {
        return this.contentNodeCollection;
    }

    public ArrayList getContentCollection() {
        return this.contentCollection;
    }

    public ArrayList getNodeDataCollection() {
        return this.nodeDataCollection;
    }

    public SerializableMetaData getMetaData() {
        return this.metaData;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getName() {
        return this.name;
    }

    protected void addNodeDataList(Content content) {
        Collection children = content.getNodeDataCollection();
        if (children == null) {
            return;
        }
        Iterator childIterator = children.iterator();
        while (childIterator.hasNext()) {
            try {
                this.nodeDataCollection.add(new SerializableNodeData((NodeData) childIterator.next()));
            }
            catch (SerializationException se) {
                log.error(se.getMessage(), se);
            }
        }
    }

    protected void addContentNodeList(Content content, boolean recurse) {
        Collection children = content.getChildren(ItemType.CONTENTNODE);
        if (children == null) {
            return;
        }
        Iterator childIterator = children.iterator();
        while (childIterator.hasNext()) {
            this.contentNodeCollection.add(new SerializableContentNode((Content) childIterator.next(), recurse));
        }
    }

    protected void addContentList(Content content, boolean recurse, boolean includeContentNodes) {
        Collection children = content.getChildren(ItemType.CONTENT);
        if (children == null) {
            return;
        }
        Iterator childIterator = children.iterator();
        while (childIterator.hasNext()) {
            this.contentCollection.add(new SerializableContent(
                (Content) childIterator.next(),
                recurse,
                includeContentNodes));
        }
    }
}
