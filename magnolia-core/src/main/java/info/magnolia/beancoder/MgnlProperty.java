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
package info.magnolia.beancoder;

import info.magnolia.cms.core.NodeData;

import java.io.InputStream;

import javax.jcr.RepositoryException;

import openwfe.org.jcr.Item;
import openwfe.org.jcr.JcrException;
import openwfe.org.jcr.Node;
import openwfe.org.jcr.Property;
import openwfe.org.jcr.Value;


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

    public MgnlProperty(Node parent, NodeData data) {
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
        }
        catch (RepositoryException e) {
            throw new JcrException(e.getMessage());
        }
    }

    /**
     * @see openwfe.org.jcr.Property#getStream()
     */
    public InputStream getStream() throws JcrException {
        return data.getStream();
    }
}
