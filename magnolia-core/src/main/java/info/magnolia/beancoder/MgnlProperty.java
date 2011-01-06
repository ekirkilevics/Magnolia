/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.cms.core.NodeData;

import java.io.InputStream;

import javax.jcr.RepositoryException;

import openwfe.org.jcr.Item;
import openwfe.org.jcr.JcrException;
import openwfe.org.jcr.Node;
import openwfe.org.jcr.Property;
import openwfe.org.jcr.Value;


/**
 * Magnolia wrapper for the property.
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
