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
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;
import info.magnolia.module.admincentral.jcr.JCRMetadataUtil;

/**
 * Describes a column that contains the label of the item.
 *
 * @author dlipp
 * @author tmattsson
 */
public class LabelColumn extends TreeColumn<String> implements Serializable {

    private static final long serialVersionUID = -3025969036157185421L;

    private boolean editable = false;

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String getValue(Item item) throws RepositoryException {
        return item.getName();
    }

    @Override
    public Field getEditField(Item item) {
        return (editable) ? new TextField() : null;
    }

    @Override
    public void setValue(Item item, Object newValue) throws RepositoryException {

        if (item instanceof Node) {
            Node node = (Node) item;

            String newPath = (node.getParent().getDepth() > 0 ? node.getParent().getPath() : "") + "/" + newValue;

            node.getSession().move(node.getPath(), newPath);

            JCRMetadataUtil.updateMetaData(node);

            node.getSession().save();

        } else if (item instanceof Property) {
            Property property = (Property) item;
            Node node = property.getParent();

            node.setProperty((String) newValue, property.getValue());
            property.remove();

            JCRMetadataUtil.updateMetaData(node);
            node.getSession().save();

            // TODO since the name of the property is part of the itemId in the container it needs to be removed and re-added

            // Will that have any consequences on for instance focus and selection and so on?
        }
    }
}
