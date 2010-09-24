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
import javax.jcr.RepositoryException;

import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;


/**
 * Describes a column that contains the label of the item.
 */
public class LabelColumn extends TreeColumn<String> implements Serializable {

    private static final long serialVersionUID = -3025969036157185421L;

    // TODO dlipp: to be decided whether this should be kept (currently configured via Content2Bean)
    // Beeing editable or not is also steered on the TreeTable level... Redundancy???
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
    public Object getValue(Node node) throws RepositoryException {
        return node.getName();
    }

    @Override
    public Field getEditField(Node ignored) {
        return (editable) ? new TextField() : null;
    }

    @Override
    public void setValue(Node node, Object value) throws RepositoryException {
        throw new RepositoryException("Do not yet know how to change Name of a node :-(");
    }

}
