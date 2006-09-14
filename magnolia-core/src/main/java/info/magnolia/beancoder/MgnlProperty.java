package info.magnolia.beancoder;

import info.magnolia.cms.core.NodeData;
import openwfe.org.jcr.*;

import javax.jcr.RepositoryException;

/**
 * Magnolia wrapper for the property
 */
public class MgnlProperty implements Property {

    NodeData data;
    Node parent;

    public String getPath() throws JcrException {
        return data.getHandle();
    }

    public Object getWrappedInstance() throws JcrException {
        return data;
    }

    public MgnlProperty(Node parent,NodeData data) {
        this.data = data;
        this.parent = parent;
    }

    public Value getValue() throws JcrException {
        return new MgnlValue(data.getValue());
    }

    public String getString() throws JcrException {
        return data.getString();
    }

    public long getLong() throws JcrException {
        return data.getLong();
    }

    public String getName() throws JcrException {
        return data.getName();
    }

    public Item getParent() throws JcrException {
        return parent;
    }

    public boolean isNode() throws JcrException {
        return false;
    }

    public void save() throws JcrException {
        try {
            data.save();
        } catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }
}
