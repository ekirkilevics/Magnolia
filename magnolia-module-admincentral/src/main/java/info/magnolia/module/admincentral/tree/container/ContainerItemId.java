/**
 * This file Copyright (c) 2011 Magnolia International
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

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Id of an item kept in JcrContainer. Can be used for a node or a property.
 *
 * @author tmattsson
 */
public class ContainerItemId {

    private final String nodeIdentifier;
    private final String propertyName;

    public ContainerItemId(String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        this.propertyName = null;
    }

    public ContainerItemId(Item item) throws RepositoryException {
        if (item instanceof Node) {
            Node node = (Node) item;
            this.nodeIdentifier = node.getIdentifier();
            this.propertyName = null;
        } else if (item instanceof Property) {
            Property property = (Property) item;
            this.nodeIdentifier = property.getParent().getIdentifier();
            this.propertyName = property.getName();
        } else {
            throw new IllegalStateException("Unsupported item type: " + item);
        }
    }

    public ContainerItemId(Node node, String propertyName) throws RepositoryException {
        this.nodeIdentifier = node.getIdentifier();
        this.propertyName = propertyName;
    }

    public String getNodeIdentifier() {
        return nodeIdentifier;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean isNode() {
        return propertyName == null;
    }

    public boolean isProperty() {
        return propertyName != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContainerItemId that = (ContainerItemId) o;

        if (nodeIdentifier != null ? !nodeIdentifier.equals(that.nodeIdentifier) : that.nodeIdentifier != null)
            return false;
        if (propertyName != null ? !propertyName.equals(that.propertyName) : that.propertyName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = propertyName != null ? propertyName.hashCode() : 0;
        result = 31 * result + (nodeIdentifier != null ? nodeIdentifier.hashCode() : 0);
        return result;
    }
}
