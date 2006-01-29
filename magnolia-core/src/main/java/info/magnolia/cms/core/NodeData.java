/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wrapper class for a jcr property.
 * @author Sameer Charles
 * @version 2.0
 */
public class NodeData extends ContentHandler {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(NodeData.class);

    /**
     * Wrapped javax.jcr.Property.
     */
    private Property property;

    /**
     * Wrapped javax.jcr.Node for nt:resource type
     */
    private Node node;

    /**
     * Empty constructor. Should NEVER be used for standard use, test only.
     */
    protected NodeData() {
        // property is null
    }

    /**
     * Constructor. Create nodeData object to work-on based on existing <code>Property</code> or
     * <code>nt:resource</code>
     * @param workingNode current active <code>Node</code>
     * @param name <code>NodeData</code> name to be retrieved
     * @param manager Access manager to be used for this object
     */
    protected NodeData(Node workingNode, String name, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(workingNode.getPath(), name), Permission.READ);
        this.init(workingNode, name);
        this.setAccessManager(manager);
    }

    /**
     * Constructor. Creates a new initialized NodeData of given type
     * @param workingNode current active <code>Node</code>
     * @param name <code>NodeData</code> name to be created
     * @param type
     * @param createNew if true create a new Item
     * @param manager Access manager to be used for this object
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    protected NodeData(Node workingNode, String name, int type, boolean createNew, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        if (createNew) {
            Access.isGranted(manager, Path.getAbsolutePath(workingNode.getPath(), name), Permission.WRITE);
            this.init(workingNode, name, type, null);
        }
        else {
            Access.isGranted(manager, Path.getAbsolutePath(workingNode.getPath(), name), Permission.READ);
            this.init(workingNode, name);
        }
        this.setAccessManager(manager);
    }

    /**
     * Constructor. Creates a new initialized NodeData
     * @param workingNode current active <code>Node</code>
     * @param name <code>NodeData</code> name to be created
     * @param value Value to be set
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    protected NodeData(Node workingNode, String name, Value value, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(workingNode.getPath(), name), Permission.WRITE);
        this.init(workingNode, name, value.getType(), value);
        this.setAccessManager(manager);
    }

    /**
     * Constructor. Creates a new initialized NodeData
     * @param node <code>Node</code> of type nt:resource
     */
    public NodeData(Node node, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(node.getPath()), Permission.READ);
        this.node = node;
        this.property = this.node.getProperty(ItemType.JCR_DATA);
        this.setAccessManager(manager);
    }

    /**
     * Constructor. Creates a new initialized NodeData
     * @param property
     */
    public NodeData(Property property, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        this.property = property;
        Access.isGranted(manager, Path.getAbsolutePath(this.property.getPath()), Permission.READ);
        this.setAccessManager(manager);
    }

    /**
     * create a new nt:resource node
     * @param workingNode
     * @param name
     * @param type
     * @param value
     */
    private void init(Node workingNode, String name, int type, Value value) throws PathNotFoundException,
        RepositoryException, AccessDeniedException {
        if (PropertyType.BINARY == type) {
            this.node = workingNode.addNode(name, ItemType.NT_RESOURCE);
            if (null != value) {
                this.property = this.node.setProperty(ItemType.JCR_DATA, value, value.getType());
            }
        }
        else {
            if (null == value) {
                this.property = workingNode.setProperty(name, StringUtils.EMPTY);
            }
            else {
                this.property = workingNode.setProperty(name, value, value.getType());
            }
        }
    }

    /**
     * initialize this object based on existing property or nt:resource node
     * @param workingNode
     * @param name
     * @throws RepositoryException
     */
    private void init(Node workingNode, String name) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        try {
            this.property = workingNode.getProperty(name);
        }
        catch (PathNotFoundException e) {
            if (workingNode.hasNode(name)) {
                // this node data should wrap nt:resource
                this.node = workingNode.getNode(name);
                this.property = this.node.getProperty(ItemType.JCR_DATA);
            }
        }
    }

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
    public Value getValue() {
        try {
            return this.property.getValue();
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Returns the <code>String</code> representation of the value: decodes like breaks with the specified regular
     * expression.
     * @param lineBreak , regular expession
     * @return String
     */
    public String getString(String lineBreak) {
        try {
            return this.getString().replaceAll("\n", lineBreak); //$NON-NLS-1$
        }
        catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Returns the <code>String</code> representation of the value.
     * @return String
     */
    public String getString() {
        try {
            return this.property.getString();
        }
        catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Returns the <code>long</code> representation of the value:
     * @return long
     */
    public long getLong() {
        try {
            return this.property.getLong();
        }
        catch (Exception e) {
            return 0;
        }
    }

    /**
     * Returns the <code>double</code> representation of the value:
     * @return double
     */
    public double getDouble() {
        try {
            return this.property.getDouble();
        }
        catch (Exception e) {
            return 0;
        }
    }

    /**
     * Returns the <code>Calendar</code> representation of the value:
     * @return Calendar
     */
    public Calendar getDate() {
        try {
            return this.property.getDate();
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the <code>boolean</code> representation of the value.
     * @return boolean
     */
    public boolean getBoolean() {
        try {
            return this.property.getBoolean();
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the <code>InputStream</code> representation of the value:
     * @return boolean
     */
    public InputStream getStream() {
        try {
            return this.property.getStream();
        }
        catch (Exception e) {
            return null;
        }
    }

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
    public int getType() {
        if (this.property != null) {
            try {
                return this.property.getType();
            }
            catch (Exception e) {
                log.warn("Unable to read property type for " + this.property); //$NON-NLS-1$
            }
        }
        return PropertyType.UNDEFINED;
    }

    /**
     * @return atom name
     */
    public String getName() {
        try {
            // check if its a nt:resource
            if (null != this.node) {
                return this.node.getName();
            }
            return this.property.getName();
        }
        catch (Exception e) {
            log.warn("Unable to read property name for " + this.property); //$NON-NLS-1$
            return StringUtils.EMPTY;
        }
    }

    /**
     * returns size in bytes
     * @return content length
     */
    public long getContentLength() {
        try {
            return this.property.getLength();
        }
        catch (RepositoryException re) {
            log.warn("Unable to read content length for " + this.property); //$NON-NLS-1$
            return 0;
        }
    }

    /**
     * Access to property at the JCR level. Available only to be available, should not be used in normal circumstances!
     * @return Property
     */
    public Property getJCRProperty() {
        return this.property;
    }

    /**
     * set value of type <code>String</code>
     * @throws RepositoryException
     * @param value , string to be set
     */
    public void setValue(String value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * set value of type <code>int</code>
     * @throws RepositoryException
     * @param value , int value to be set
     */
    public void setValue(int value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * set value of type <code>long</code>
     * @throws RepositoryException
     * @param value , long value to be set
     */
    public void setValue(long value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * set value of type <code>InputStream</code>
     * @throws RepositoryException
     * @param value , InputStream to be set
     */
    public void setValue(InputStream value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        if (this.property == null && this.node != null) {
            this.property = this.node.setProperty(ItemType.JCR_DATA, value);
        }
        else {
            this.property.setValue(value);
        }
    }

    /**
     * set value of type <code>double</code>
     * @throws RepositoryException
     * @param value , double value to be set
     */
    public void setValue(double value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * set value of type <code>boolean</code>
     * @throws RepositoryException
     * @param value , boolean value to be set
     */
    public void setValue(boolean value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * set value of type <code>Calendar</code>
     * @throws RepositoryException
     * @param value , Calendar value to be set
     */
    public void setValue(Calendar value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * set value of type <code>Value</code>
     * @throws RepositoryException
     * @param value
     */
    public void setValue(Value value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * set attribute, available only if NodeData is of type <code>Binary</code>
     * @param name
     * @param value
     * @throws RepositoryException
     * @throws AccessDeniedException
     * @throws UnsupportedOperationException if its not a Binary type
     */
    public void setAttribute(String name, String value) throws RepositoryException, AccessDeniedException,
        UnsupportedOperationException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        if (null == this.node) {
            throw new UnsupportedOperationException("Attributes are only supported for BINARY type");
        }
        this.node.setProperty(name, value);
    }

    /**
     * set attribute, available only if NodeData is of type <code>Binary</code>
     * @param name
     * @param value
     * @throws RepositoryException
     * @throws AccessDeniedException
     * @throws UnsupportedOperationException if its not a Binary type
     */
    public void setAttribute(String name, Calendar value) throws RepositoryException, AccessDeniedException,
        UnsupportedOperationException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        if (null == this.node) {
            throw new UnsupportedOperationException("Attributes are only supported for BINARY type");
        }
        this.node.setProperty(name, value);
    }

    /**
     * get attribute, available only if NodeData is of type <code>Binary</code>
     * @param name
     * @return string value
     */
    public String getAttribute(String name) {
        if (null == this.node) {
            return "";
        }
        try {
            return this.node.getProperty(name).getString();
        }
        catch (RepositoryException re) {
            if (log.isDebugEnabled()) {
                log.debug("Attribute [ " + name + " ] not set");
            }
            return "";
        }
    }

    /**
     * checks if the atom exists in the repository
     * @return boolean
     */
    public boolean isExist() {
        return (this.property != null);
    }

    /**
     * get a handle representing path relative to the content repository
     * @return String representing path (handle) of the content
     */
    public String getHandle() {
        try {
            if (null != this.node) {
                return this.node.getPath();
            }
            return this.property.getPath();
        }
        catch (RepositoryException e) {
            log.error("Failed to get handle"); //$NON-NLS-1$
            log.error(e.getMessage(), e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * Persists all changes to the repository if valiation succeds
     * @throws RepositoryException
     */
    public void save() throws RepositoryException {
        this.property.getSession().save();
    }

    /**
     * checks for the allowed access rights
     * @param permissions as defined in javax.jcr.Permission
     * @return true is the current user has specified access on this node.
     */
    public boolean isGranted(long permissions) {
        try {
            Access.isGranted(this.accessManager, Path.getAbsolutePath(property.getPath()), permissions);
            return true;
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return false;
    }

    /**
     * Remove this path
     * @throws RepositoryException
     */
    public void delete() throws RepositoryException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.property.getPath()), Permission.REMOVE);
        if (null != this.node) {
            this.node.remove();
        }
        else {
            this.property.remove();
        }
    }

    /**
     * Refreshes current node keeping all changes
     * @see javax.jcr.Node#refresh(boolean)
     * @throws RepositoryException
     */
    public void refresh(boolean keepChanges) throws RepositoryException {
        this.property.refresh(keepChanges);
    }

}
