package info.magnolia.beancoder;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.DateUtil;
import info.magnolia.cms.util.NodeDataUtil;
import openwfe.org.jcr.Item;
import openwfe.org.jcr.JcrException;
import openwfe.org.jcr.Node;
import openwfe.org.jcr.Property;

import javax.jcr.RepositoryException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

/**
 * Magnolia wrapper for a node.
 */
public class MgnlNode implements Node {
    Content mnode;

    public MgnlNode(Content mnode)  {
        this.mnode = mnode;
    }

    public Iterator getProperties() throws JcrException {
        return new MgnlPropertyIterator(mnode);
    }

    public Iterator getNodes() throws JcrException {
        return new MgnlNodeIterator(mnode);
    }

    public Property setProperty(String string, Object object) throws JcrException {
        Property property = getProperty(string);
        try {
            final NodeData nodeData = ((NodeData) property.getWrappedInstance());
            if (object instanceof String)
                nodeData.setValue((String) object);
            else if (object instanceof Date)
                nodeData.setValue(DateUtil.getUTCCalendarFromLocalDate((Date) object));
            else if (object instanceof InputStream)
                nodeData.setValue((InputStream)object);
            else if (object instanceof Double)
                nodeData.setValue(((Double)object).doubleValue());
            else if (object instanceof Long)
                nodeData.setValue(((Long)object).longValue());
            else if (object instanceof Boolean)
                nodeData.setValue(((Boolean)object).booleanValue());
            else
                throw new JcrException("Does not support object of kind:"+object.getClass().getName());
        } catch (RepositoryException e) {
            throw new JcrException(e.getMessage(),e);
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
        } catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public Property getProperty(String propertyName) throws JcrException {
        try {
            return new MgnlProperty(this, NodeDataUtil.getOrCreate(mnode,propertyName));
        } catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public Property setProperty(String propertyName, String value) throws JcrException {
        try {
            NodeData nodeData = NodeDataUtil.getOrCreate(mnode,propertyName);
            nodeData.setValue(value);
            return new MgnlProperty(this,nodeData);
        } catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public Property setProperty(String propertyName, long value) throws JcrException {
        try {
            NodeData nodeData = NodeDataUtil.getOrCreate(mnode,propertyName);
            nodeData.setValue(value);
            return new MgnlProperty(this,nodeData);
        } catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public boolean hasNode(String relPath) throws JcrException {
        try {
            return mnode.hasContent(relPath);
        } catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public Node getNode(String relPath) throws JcrException {
        try {
            Content c = ContentUtil.getOrCreateContent(mnode,relPath, ItemType.CONTENTNODE);
            return new MgnlNode(c);
        } catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public Node addNode(String newNodeName) throws JcrException {
        try {
            return new MgnlNode(mnode.createContent(newNodeName));
        } catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public String getName() throws JcrException {
        return mnode.getName();
    }

    public Item getParent() throws JcrException {
        try {
            return new MgnlNode(mnode.getParent());
        } catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    public boolean isNode() throws JcrException {
        return true;
    }

    public void save() throws JcrException {
        try {
            mnode.save();
        } catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

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
            return new MgnlNode((Content)internalIterator.next());
        }
    }

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
            return new MgnlProperty(new MgnlNode(mnode),(NodeData)internalIterator.next());
        }
    }
}
