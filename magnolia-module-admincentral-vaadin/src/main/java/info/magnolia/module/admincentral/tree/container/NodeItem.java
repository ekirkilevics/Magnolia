/**
 * This file Copyright (c) 2010 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admincentral.tree.container;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import com.vaadin.data.Item;
import com.vaadin.data.Property;


/**
 * Wrapper around JCR Node for usage in a Vaadin Container.
 *
 * @author daniellipp
 * @version $Id$
 */

public class NodeItem extends NodeProxy implements Item {

    private static final long serialVersionUID = -6540682899828148456L;

    public NodeItem(Node node, JcrSessionProvider provider)
            throws RepositoryException {
        super(node, provider);
    }

    public boolean addItemProperty(Object id, Property property)
            throws UnsupportedOperationException {
        assertIdIsString(id);
        try {
            PropertyMapper
                    .setValue(getNode(), (String) id, property.getValue());
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    protected void assertIdIsString(Object id) {
        if (!(id instanceof String)) {
            throw new UnsupportedOperationException(
                    "JCR requires all property id's to be String");
        }
    }

    /**
     *
     * @return absolute path as vaadin item id
     * @throws RepositoryException
     */
    public String getItemId() throws RepositoryException {
        return getPath();
    }

    public Property getItemProperty(Object id) {
        assertIdIsString(id);
        // TODO dlipp: check whether we have to provide the TreeDefinition to the NodeItem as
        // well...
        return new NodeProperty(this, null, (String) id);
    }

    public Collection<String> getItemPropertyIds() {
        ArrayList<String> idlist = new ArrayList<String>();
        try {
            PropertyIterator iter = getProperties();
            while (iter.hasNext()) {
                javax.jcr.Property jcrprop = iter.nextProperty();
                idlist.add(jcrprop.getName());
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return idlist;
    }

    public boolean removeItemProperty(Object id)
            throws UnsupportedOperationException {
        assertIdIsString(id);
        try {
            getNode().getProperty((String) id).remove();
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}