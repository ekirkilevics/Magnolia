/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;
import info.magnolia.module.admincentral.jcr.JCRMetadataUtil;
import info.magnolia.module.admincentral.tree.container.JcrContainer;

/**
 * A column that displays a NodeData value when viewing a content node. Used in the website tree for
 * the 'Title' column.
 *
 * @author dlipp
 * @author tmattsson
 */
public class NodeDataColumn extends TreeColumn<String> implements Serializable {

    private static final long serialVersionUID = 979787074349524725L;

    private boolean editable = false;

    private String nodeDataName;

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getNodeDataName() {
        return nodeDataName;
    }

    public void setNodeDataName(String nodeDataName) {
        this.nodeDataName = nodeDataName;
    }

    @Override
    public Field getEditField(Item item) {
        if (item instanceof Node && editable)
            return new TextField();
        return null;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String getValue(Item item) throws RepositoryException {

        if (item instanceof Node) {
            Node node = (Node) item;

            if (node.hasProperty(nodeDataName))
                return node.getProperty(nodeDataName).getString();
        }
        return "";
    }

    @Override
    public void setValue(JcrContainer jcrContainer, Item item, Object newValue) throws RepositoryException {

        if (item instanceof Node) {
            Node node = (Node) item;
            node.setProperty(nodeDataName, (String) newValue);
            JCRMetadataUtil.updateMetaData(node);
            node.getSession().save();
        }
    }
}
