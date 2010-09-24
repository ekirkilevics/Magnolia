/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.tree;

import java.io.Serializable;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;


/**
 * A column that displays a NodeData value when viewing a content node. Used in the website tree for
 * the 'Title' column.
 */
public class NodeDataColumn extends TreeColumn<String> implements Serializable {

    public static final String PROPERTY_NAME = "title";

    private static final long serialVersionUID = 979787074349524725L;

    private boolean editable = false;

    private String nodeDataName;

    @Override
    public Field getEditField(Node unusedt) {
        // TODO dlipp: check whether this editable flag makes sense. One has to define editing on
        // the TreeTable level already...
        return (editable) ? new TextField() : null;
    }

    public String getNodeDataName() {
        return nodeDataName;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public Object getValue(Node node) throws RepositoryException {
        Property title = node.getProperty(PROPERTY_NAME);
        return title.getString();
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setNodeDataName(String nodeDataName) {
        this.nodeDataName = nodeDataName;
    }

    @Override
    public void setValue(Node node, Object newValue) throws RepositoryException {
        Property title = node.getProperty(PROPERTY_NAME);
        title.setValue((String) newValue);
    }
}
