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

import java.math.BigDecimal;
import java.net.URI;
import java.util.Calendar;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

/**
 * Helper for mapping between vaadin Property and JCR Property objects.
 *
 * @author daniellipp
 * @version $Id$
 */
public class PropertyMapper {

    /**
     * Returns the class associated with the value type of a JCR Property.
     *
     * @param prop
     * @return Class
     * @throws ValueFormatException
     * @throws RepositoryException
     */
    public static Class<?> getType(Property prop) throws ValueFormatException,
            RepositoryException {
        Class<?> retval = null;
        if (prop.getType() == PropertyType.BINARY) {
            retval = Binary.class;
        } else if (prop.getType() == PropertyType.BOOLEAN) {
            retval = Boolean.class;
        } else if (prop.getType() == PropertyType.DATE) {
            retval = Calendar.class;
        } else if (prop.getType() == PropertyType.DECIMAL) {
            retval = BigDecimal.class;
        } else if (prop.getType() == PropertyType.DOUBLE) {
            retval = Double.class;
        } else if (prop.getType() == PropertyType.LONG) {
            retval = Long.class;
        } else if (prop.getType() == PropertyType.NAME) {
            retval = String.class;
        } else if (prop.getType() == PropertyType.PATH) {
            retval = String.class;
        } else if (prop.getType() == PropertyType.REFERENCE) {
            retval = String.class;
        } else if (prop.getType() == PropertyType.STRING) {
            retval = String.class;
        } else if (prop.getType() == PropertyType.URI) {
            retval = URI.class;
        } else if (prop.getType() == PropertyType.WEAKREFERENCE) {
            retval = String.class;
        } else {
            retval = Object.class;
        }
        return retval;
    }

    /**
     * Returns the appropriately typed value from a javax.jcr.Property.
     *
     * @param prop
     *            JCR Property
     * @return Object of appropriate type
     * @throws ValueFormatException
     * @throws RepositoryException
     */
    public static Object getValue(Property prop) throws ValueFormatException,
            RepositoryException {
        Object retval = null;
        if (prop.getType() == PropertyType.BINARY) {
            retval = prop.getBinary();
        } else if (prop.getType() == PropertyType.BOOLEAN) {
            retval = prop.getBoolean();
        } else if (prop.getType() == PropertyType.DATE) {
            retval = prop.getDate();
        } else if (prop.getType() == PropertyType.DECIMAL) {
            retval = prop.getDecimal();
        } else if (prop.getType() == PropertyType.DOUBLE) {
            retval = prop.getDouble();
        } else if (prop.getType() == PropertyType.LONG) {
            retval = prop.getLong();
        } else if (prop.getType() == PropertyType.NAME) {
            retval = prop.getName();
        } else if (prop.getType() == PropertyType.PATH) {
            retval = prop.getPath();
        } else if (prop.getType() == PropertyType.REFERENCE) {
            retval = prop.getNode();
        } else if (prop.getType() == PropertyType.STRING) {
            retval = prop.getString();
        } else if (prop.getType() == PropertyType.URI) {
            retval = prop.getString();
        } else if (prop.getType() == PropertyType.WEAKREFERENCE) {
            retval = prop.getString();
        } else {
            retval = prop.getValue();
        }
        return retval;
    }

    /**
     * Sets a Property value by the type of Object.
     *
     * @param node
     *            - the Node of the Property
     * @param id
     *            - String identifier of the Property to be set
     * @param val
     *            - Object value of the Property to be set
     * @throws ValueFormatException
     * @throws VersionException
     * @throws LockException
     * @throws ConstraintViolationException
     * @throws RepositoryException
     */
    public static void setValue(Node node, String id, Object val)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        if (val instanceof Binary) {
            node.setProperty(id, (Binary) val);
        } else if (val instanceof Boolean) {
            node.setProperty(id, (Boolean) val);
        } else if (val instanceof Calendar) {
            node.setProperty(id, (Calendar) val);
        } else if (val instanceof BigDecimal) {
            node.setProperty(id, (BigDecimal) val);
        } else if (val instanceof Double) {
            node.setProperty(id, (Double) val);
        } else if (val instanceof Long) {
            node.setProperty(id, (Long) val);
        } else if (val instanceof String) {
            node.setProperty(id, (String) val);
        } else if (val instanceof URI) {
            node.setProperty(id, ((URI) val).toString());
        } else if (val instanceof Value) {
            node.setProperty(id, (Value) val);
        } else {
            node.setProperty(id, val.toString());
        }
    }
}
