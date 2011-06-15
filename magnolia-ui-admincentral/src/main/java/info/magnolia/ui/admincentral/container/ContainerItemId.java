/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.container;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Id of an item kept in JcrContainer. Can be used for a node or a property.
 *
 * @author tmattsson
 */
public class ContainerItemId {

    private final Object nodeIdentifier;
    private final String propertyName;

    public ContainerItemId(Item item) throws RepositoryException {
        if (item.isNode()) {
            Node node = (Node) item;
            this.nodeIdentifier = uuid2int(node.getIdentifier());
            this.propertyName = null;
        } else {
            Property property = (Property) item;
            this.nodeIdentifier = uuid2int(property.getParent().getIdentifier());
            this.propertyName = property.getName();
        }
    }

    public String getNodeIdentifier() {
        return int2uuid(nodeIdentifier);
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContainerItemId that = (ContainerItemId) o;

        if (nodeIdentifier != null ? !getNodeIdentifier().equals((that.getNodeIdentifier())) : that.nodeIdentifier != null) {
            return false;
        }
        if (propertyName != null ? !propertyName.equals(that.propertyName) : that.propertyName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = propertyName != null ? propertyName.hashCode() : 0;
        result = 31 * result + (nodeIdentifier != null ? getNodeIdentifier().hashCode() : 0);
        return result;
    }

    protected final Object uuid2int(String uuidStr) {
        if (StringUtils.isBlank(uuidStr) || uuidStr.length() != 36) {
            return uuidStr;
        }
        char[] uuidChar = uuidStr.toCharArray();
        // ex: 159bc523-fa30-40e6-965d-4ab91a4bdd9a
        int[] uuidInt = new int[8];
        int idx = 0;
        for (int i = 0; i < 36; i++) {
            char ch = uuidChar[i];
            if ('-' == ch) {
                continue;
            }
            uuidInt[idx++] = Integer.parseInt("" + ch + uuidChar[++i] + uuidChar[++i] + uuidChar[++i], 16);
        }
        return uuidInt;
    }

    protected final String int2uuid(Object uuidIntArg) {
        if (uuidIntArg instanceof String) {
            return (String) uuidIntArg;
        }
        int[] uuidInt = (int[]) uuidIntArg;
        StringBuilder sb = new StringBuilder();
        for (int i : uuidInt) {
            // for those not in love with formatter: convert number to hex and left pad with zero to the length of 4 digits/chars
            sb.append(String.format("%04x", i));
            int len = sb.length();
            if (len == 8 || len == 13 || len == 18 || len == 23) {
                sb.append("-");
            }
        }
        return sb.toString();
    }
}
