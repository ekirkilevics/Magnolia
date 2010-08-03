/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.beancoder;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.DateUtil;
import info.magnolia.cms.util.NodeDataUtil;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import openwfe.org.jcr.Item;
import openwfe.org.jcr.JcrException;
import openwfe.org.jcr.Node;
import openwfe.org.jcr.Property;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Magnolia wrapper for a node.
 */
public class MgnlNode implements Node {
    private static final Logger log = LoggerFactory.getLogger(MgnlNode.class);
    // TODO private final ?
    Content mnode;

    public MgnlNode(Content mnode) {
        this.mnode = mnode;
    }

    public Iterator getProperties() throws JcrException {
        return new MgnlPropertyIterator(mnode);
    }

    public Iterator getNodes() throws JcrException {
        return new MgnlNodeIterator(mnode);
    }

    // TODO UNUSED ?
    public Property setProperty(String string, Object object) throws JcrException {
        Property property = getProperty(string);
        try {
            final NodeData nodeData = ((NodeData) property.getWrappedInstance());
            if (object instanceof String) {
                nodeData.setValue((String) object);
            } else if (object instanceof Date) {
                nodeData.setValue(DateUtil.getUTCCalendarFromLocalDate((Date) object));
            } else if (object instanceof InputStream) {
                nodeData.setValue((InputStream) object);
            } else if (object instanceof Double) {
                nodeData.setValue(((Double) object).doubleValue());
            } else if (object instanceof Long) {
                nodeData.setValue(((Long) object).longValue());
            } else if (object instanceof Boolean) {
                nodeData.setValue(((Boolean) object).booleanValue());
            } else {
                throw new JcrException("Does not support object of kind:" + object.getClass().getName());
            }
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage(), e);
        }
        return property;
    }

    public String getPath() throws JcrException {
        return mnode.getHandle();
    }

    public Object getWrappedInstance() throws JcrException {
        return mnode;
    }

    public boolean hasProperty(String propertyName) throws JcrException {
        try {
            return mnode.hasNodeData(propertyName);
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public Property getProperty(String propertyName) throws JcrException {
        try {
            return new MgnlProperty(this, NodeDataUtil.getOrCreate(mnode, propertyName));
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    // TODO : delegate to setProperty(String, String, int)
    public Property setProperty(String propertyName, String value) throws JcrException {
        try {
            NodeData nodeData = NodeDataUtil.getOrCreate(mnode, propertyName);
            nodeData.setValue(value);
            return new MgnlProperty(this, nodeData);
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    // TODO : delegate to setProperty(String, String, int)
    public Property setProperty(String propertyName, long value) throws JcrException {
        try {
            NodeData nodeData = NodeDataUtil.getOrCreate(mnode, propertyName);
            nodeData.setValue(value);
            return new MgnlProperty(this, nodeData);
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public boolean hasNode(String relPath) throws JcrException {
        try {
            return mnode.hasContent(relPath);
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public Node getNode(String relPath) throws JcrException {
        try {
            Content c = ContentUtil.getOrCreateContent(mnode, relPath, ItemType.CONTENTNODE);
            return new MgnlNode(c);
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public Node addNode(String newNodeName) throws JcrException {
        try {
            return new MgnlNode(mnode.createContent(newNodeName));
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public String getName() throws JcrException {
        return mnode.getName();
    }

    public Item getParent() throws JcrException {
        try {
            return new MgnlNode(mnode.getParent());
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public boolean isNode() throws JcrException {
        return true;
    }

    public void save() throws JcrException {
        try {
            mnode.save();
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    /**
     * Wraps an iterator of the given node's child nodes.
     */
    class MgnlNodeIterator implements Iterator {

        Content node;

        private Iterator internalIterator;

        public MgnlNodeIterator(Content node) {
            this.node = node;
            this.internalIterator = node.getChildren().iterator();
        }

        public void remove() {
            internalIterator.remove();
        }

        public boolean hasNext() {
            return internalIterator.hasNext();
        }

        public Object next() {
            return new MgnlNode((Content) internalIterator.next());
        }
    }

    /**
     * Wraps an iterator of the given node's properties.
     */
    class MgnlPropertyIterator implements Iterator {

        private Content mnode;

        private Iterator internalIterator;

        public MgnlPropertyIterator(Content mnode) {
            this.mnode = mnode;
            this.internalIterator = mnode.getNodeDataCollection().iterator();
        }

        public void remove() {
            internalIterator.remove();
        }

        public boolean hasNext() {
            return internalIterator.hasNext();
        }

        public Object next() {
            return new MgnlProperty(new MgnlNode(mnode), (NodeData) internalIterator.next());
        }
    }

    /**
     * @see openwfe.org.jcr.Node#setProperty(java.lang.String, java.lang.String, int)
     */
    public Property setProperty(String propertyName, String valueStr, int type) throws JcrException {
        if (type == PropertyType.NAME) {
            log.debug("setProperty(" + propertyName + ", " + valueStr + " with type PropertyType.NAME, will switch to PropertyType.STRING ...");
            type = PropertyType.STRING;
        }

        try {
            final NodeData nodeData = NodeDataUtil.getOrCreate(mnode, propertyName, type);
            final Value value = NodeDataUtil.createValue(valueStr, type);
            nodeData.setValue(value);

            return new MgnlProperty(this, nodeData);
        } catch (RepositoryException e) {
            throw new JcrException(e.getMessage(), e);
        }
    }

    public void remove() throws JcrException {
        try {
            this.mnode.delete();
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage(), e);
        }

    }

    /**
     * @see openwfe.org.jcr.Node#setProperty(java.lang.String, java.io.InputStream)
     */
    public Property setProperty(String propertyName, InputStream value) throws JcrException {
        try {
            NodeData nodeData = NodeDataUtil.getOrCreate(mnode, propertyName);
            nodeData.setValue(value);
            return new MgnlProperty(this, nodeData);
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }
}
