/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */




package info.magnolia.cms.exchange.simple;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.beans.config.ItemType;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.log4j.Logger;


/**
 * Date: Jun 21, 2004
 * Time: 11:42:04 AM
 *
 * @author Sameer Charles
 * @version 2.0
 */





public class SerializableContent implements Serializable {



    private static Logger log = Logger.getLogger(SerializableContent.class);


    /* NodeData collection */
    protected ArrayList nodeDataCollection = new ArrayList();


    /* sub ContentNode collection */
    protected ArrayList contentNodeCollection = new ArrayList();


    /* sub Content collection */
    protected ArrayList contentCollection = new ArrayList();


    protected SerializableMetaData metaData;


    private String name;

    private Content baseContent;

    private boolean recurse;



    public SerializableContent() {

    }


    public SerializableContent(Content content) {
        this.baseContent = content;
        this.makeSerializable();
        this.baseContent = null;
    }



    public SerializableContent(Content content, boolean recurse) {
        this.baseContent = content;
        this.recurse = recurse;
        this.makeSerializable();
        this.baseContent = null;
    }



    private void makeSerializable() {
        this.setName(this.baseContent.getName());
        this.metaData = new SerializableMetaData(this.baseContent.getMetaData());
        this.addNodeDataList(this.baseContent);
        this.addContentNodeList(this.baseContent, true);
        if (this.recurse)
            this.addContentList(this.baseContent, true);
    }



    public ArrayList getContentNodeCollection() {
        return this.contentNodeCollection;
    }



    public ArrayList getContentCollection() {
        return this.contentCollection;
    }



    public ArrayList  getNodeDataCollection() {
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
        Collection children = content.getChildren(ItemType.MAGNOLIA_NODE_DATA);
        if (children == null)
            return;
        Iterator childIterator = children.iterator();
        while (childIterator.hasNext()) {
            try {
                this.nodeDataCollection.add(
                        new SerializableNodeData((NodeData)childIterator.next()));
            } catch (SerializationException se) {
                log.error(se.getMessage(), se);
            }
        }
    }



    protected void addContentNodeList(Content content, boolean recurse) {
        Collection children =
                content.getChildren(ItemType.MAGNOLIA_CONTENT_NODE);
        if (children == null)
            return;
        Iterator childIterator = children.iterator();
        while (childIterator.hasNext()) {
            this.contentNodeCollection.add(
                    new SerializableContentNode((ContentNode)childIterator.next(), recurse));
        }
    }



    protected void addContentList(Content content, boolean recurse) {
        Collection children =
                content.getChildren(ItemType.MAGNOLIA_PAGE);
        if (children == null)
            return;
        Iterator childIterator = children.iterator();
        while (childIterator.hasNext()) {
            this.contentCollection.add(
                    new SerializableContent((Content) childIterator.next(), recurse));
        }
    }



}
