/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessDeniedException;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Value;
import javax.jcr.RepositoryException;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import java.util.Calendar;
import java.util.Collection;
import java.io.InputStream;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface NodeData extends Cloneable {

    /**
     * Returns the <code>value</code> of this <code>NodeData</code>. One of type:
     * <ul>
     * <li><code>PropertyType.STRING</code></li>
     * <li><code>PropertyType.DATE</code></li>
     * <li><code>PropertyType.SOFTLINK</code></li>
     * <li><code>PropertyType.BINARY</code></li>
     * <li><code>PropertyType.DOUBLE</code></li>
     * <li><code>PropertyType.LONG</code></li>
     * <li><code>PropertyType.BOOLEAN</code></li>
     * </ul>
     * @return Value
     */
    Value getValue();

    /**
     * For multi-value properties
     * @return Value[]
     */
    Value[] getValues();

    /**
     * Returns the <code>String</code> representation of the value: decodes like breaks with the specified regular
     * expression.
     * @param lineBreak , regular expression
     * @return String
     */
    String getString(String lineBreak);

    /**
     * Returns the <code>String</code> representation of the value.
     * @return String
     */
    String getString();

    /**
     * Returns the <code>long</code> representation of the value:
     * @return long
     */
    long getLong();

    /**
     * Returns the <code>double</code> representation of the value:
     * @return double
     */
    double getDouble();

    /**
     * Returns the <code>Calendar</code> representation of the value:
     * @return Calendar
     */
    Calendar getDate();

    /**
     * Returns the <code>boolean</code> representation of the value.
     * @return boolean
     */
    boolean getBoolean();

    /**
     * Returns the <code>InputStream</code> representation of the value:
     * @return boolean
     */
    InputStream getStream();

    /**
     * Returns the Content that this NodeData references (if its type is PropertyType.REFERENCE). If it is of type PATH
     * or STRING it tries to resolve the node by using the path. The path can be relative or absolute. If the property
     * type is STRING, it tries finally to get the node by using the value as an uuid.
     * @throws javax.jcr.RepositoryException
     */
    Content getReferencedContent() throws RepositoryException, PathNotFoundException, RepositoryException;

    /**
     * Same as {@link #getReferencedContent()} but achieves the referenced node from a different workspace
     */
    Content getReferencedContent(String repositoryId) throws PathNotFoundException, RepositoryException;

    /**
     * Returns the <code>type</code> of this <code>NodeData</code>. One of:
     * <ul>
     * <li><code>PropertyType.STRING</code></li>
     * <li><code>PropertyType.DATE</code></li>
     * <li><code>PropertyType.SOFTLINK</code></li>
     * <li><code>PropertyType.BINARY</code></li>
     * <li><code>PropertyType.DOUBLE</code></li>
     * <li><code>PropertyType.LONG</code></li>
     * <li><code>PropertyType.BOOLEAN</code></li>
     * </ul>
     * @return PropertyType
     */
    int getType();

    /**
     * @return atom name
     */
    String getName();

    /**
     * returns size in bytes
     * @return content length
     */
    long getContentLength();

    /**
     * Access to property at the JCR level. Available only to be available, should not be used in normal circumstances!
     * @return Property
     */
    Property getJCRProperty();

    /**
     * set value of type <code>String</code>
     * @param value , string to be set
     * @throws javax.jcr.RepositoryException
     */
    void setValue(String value) throws RepositoryException, AccessDeniedException;

    /**
     * set value of type <code>int</code>
     * @param value , int value to be set
     * @throws javax.jcr.RepositoryException
     */
    void setValue(int value) throws RepositoryException, AccessDeniedException;

    /**
     * set value of type <code>long</code>
     * @param value , long value to be set
     * @throws javax.jcr.RepositoryException
     */
    void setValue(long value) throws RepositoryException, AccessDeniedException;

    /**
     * set value of type <code>InputStream</code>
     * @param value , InputStream to be set
     * @throws javax.jcr.RepositoryException
     */
    void setValue(InputStream value) throws RepositoryException, AccessDeniedException;

    /**
     * set value of type <code>double</code>
     * @param value , double value to be set
     * @throws javax.jcr.RepositoryException
     */
    void setValue(double value) throws RepositoryException, AccessDeniedException;

    /**
     * set value of type <code>boolean</code>
     * @param value , boolean value to be set
     * @throws javax.jcr.RepositoryException
     */
    void setValue(boolean value) throws RepositoryException, AccessDeniedException;

    /**
     * set value of type <code>Calendar</code>
     * @param value , Calendar value to be set
     * @throws javax.jcr.RepositoryException
     */
    void setValue(Calendar value) throws RepositoryException, AccessDeniedException;

    /**
     * set value of type <code>Value</code>
     * @param value
     * @throws javax.jcr.RepositoryException
     */
    void setValue(Value value) throws RepositoryException, AccessDeniedException;

    /**
     * set value of type <code>Value[]</code>
     * @param value[]
     * @throws javax.jcr.RepositoryException
     */
    void setValue(Value[] value) throws RepositoryException, AccessDeniedException;

    /**
     * set attribute, available only if NodeData is of type <code>Binary</code>
     * @param name
     * @param value
     * @throws javax.jcr.RepositoryException
     * @throws info.magnolia.cms.security.AccessDeniedException
     * @throws UnsupportedOperationException if its not a Binary type
     */
    void setAttribute(String name, String value) throws RepositoryException, AccessDeniedException,
        UnsupportedOperationException;

    /**
     * set attribute, available only if NodeData is of type <code>Binary</code>
     * @param name
     * @param value
     * @throws javax.jcr.RepositoryException
     * @throws info.magnolia.cms.security.AccessDeniedException
     * @throws UnsupportedOperationException if its not a Binary type
     */
    void setAttribute(String name, Calendar value) throws RepositoryException, AccessDeniedException,
        UnsupportedOperationException;

    /**
     * get attribute, available only if NodeData is of type <code>Binary</code>
     * @param name
     * @return string value
     */
    String getAttribute(String name);

    /**
     * get all attribute names
     * @return collection of attribute names
     * @throws javax.jcr.RepositoryException
     */
    Collection getAttributeNames() throws RepositoryException;

    /**
     * checks if the atom exists in the repository
     * @return boolean
     */
    boolean isExist();

    /**
     * get a handle representing path relative to the content repository
     * @return String representing path (handle) of the content
     */
    String getHandle();

    /**
     * Persists all changes to the repository if validation succeeds
     * @throws javax.jcr.RepositoryException
     */
    void save() throws RepositoryException;

    /**
     * checks for the allowed access rights
     * @param permissions as defined in javax.jcr.Permission
     * @return true is the current user has specified access on this node.
     */
    boolean isGranted(long permissions);

    /**
     * Remove this path
     * @throws javax.jcr.RepositoryException
     */
    void delete() throws RepositoryException;

    /**
     * Refreshes current node keeping all changes
     * @throws javax.jcr.RepositoryException
     * @see javax.jcr.Node#refresh(boolean)
     */
    void refresh(boolean keepChanges) throws RepositoryException;

    /**
     * for multi-value controls
     * @return
     */
    int isMultiValue();

    /**
     * returns Parent node
     * @return
     * @throws RepositoryException
     * @throws javax.jcr.AccessDeniedException
     * @throws ItemNotFoundException
     * @throws AccessDeniedException
     */
    Content getParent() throws AccessDeniedException, ItemNotFoundException, javax.jcr.AccessDeniedException, RepositoryException;

    HierarchyManager getHierarchyManager();

}
