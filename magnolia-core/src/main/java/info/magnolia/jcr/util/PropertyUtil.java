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
package info.magnolia.jcr.util;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * Property-related utility methods.
 *
 * @version $Id$
 */
public class PropertyUtil {

    public static void renameProperty(Property property, String newName) throws RepositoryException {
        Node node = property.getNode();
        node.setProperty(newName, property.getValue());
        property.remove();
    }

    /**
     * Allows setting a Node's property from an object.
     */
    public static void setProperty(Node node, String propertyName, Object propertyValue) throws RepositoryException {
        if (node == null) {
            throw new IllegalArgumentException("Cannot set a property on a null-node!");
        }
        if (propertyName == null) {
            throw new IllegalArgumentException("Cannot set a property without a provided name");
        }

        // let's find out what type of value we got
        if (propertyValue instanceof Value) {
            node.setProperty(propertyName, (Value) propertyValue);
        } else if (propertyValue instanceof Node) {
            node.setProperty(propertyName, (Node) propertyValue);
        } else if (propertyValue instanceof Binary) {
            node.setProperty(propertyName, (Binary) propertyValue);
        } else if (propertyValue instanceof Calendar) {
            node.setProperty(propertyName, (Calendar) propertyValue);
        } else if (propertyValue instanceof BigDecimal) {
            node.setProperty(propertyName, (BigDecimal) propertyValue);
        } else if (propertyValue instanceof String) {
            node.setProperty(propertyName, (String) propertyValue);
        } else if (propertyValue instanceof Long) {
            node.setProperty(propertyName, ((Long) propertyValue).longValue());
        } else if (propertyValue instanceof Double) {
            node.setProperty(propertyName, ((Double) propertyValue).doubleValue());
        } else if (propertyValue instanceof Boolean) {
            node.setProperty(propertyName, ((Boolean) propertyValue).booleanValue());
        } else
            throw new IllegalArgumentException("Cannot set property to a value of type " + propertyValue.getClass());
    }
}
