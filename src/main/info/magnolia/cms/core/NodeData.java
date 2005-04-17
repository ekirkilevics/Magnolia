/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core;

import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.core.util.Access;
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

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.0
 */
public class NodeData extends ContentHandler {

    public static final String HTML_LINEBREAK = "<br />";

    private static final String DATA_ELEMENT = "Data";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(NodeData.class);

    private Property property;

    /**
     * package private constructor
     */
    NodeData() {
        this.property = null;
    }

    /**
     * <p>
     * constructor | create atom object to work-on based on existing <code>NodeData</code>
     * <p>
     * @param workingNode current active <code>Node</code>
     * @param name <code>NodeData</code> name to be retrieved
     */
    public NodeData(Node workingNode, String name, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(workingNode.getPath(), name), Permission.READ);
        this.node = workingNode.getNode(name);
        this.property = this.node.getNode(ItemType.getSystemName(ItemType.JCR_CONTENT)).getProperty(DATA_ELEMENT);
        this.setAccessManager(manager);
    }

    /**
     * <p>
     * constructor | creates a new initialized NodeData of default type <b>String </b>
     * </p>
     * @param workingNode current active <code>Node</code>
     * @param name <code>NodeData</code> name to be created
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public NodeData(Node workingNode, String name, boolean createNew, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        this.setAccessManager(manager);
        if (createNew) {
            Access.isGranted(manager, Path.getAbsolutePath(workingNode.getPath(), name), Permission.WRITE);
            this.node = workingNode.addNode(name, ItemType.getSystemName(ItemType.NT_NODEDATA));
            Node contentNode = this.node.addNode(ItemType.getSystemName(ItemType.JCR_CONTENT), ItemType
                .getSystemName(ItemType.NT_UNSTRUCTRUED));
            this.property = contentNode.setProperty(DATA_ELEMENT, "");
            this.addMixin(ItemType.getSystemName(ItemType.MIX_VERSIONABLE));
        }
        else {
            Access.isGranted(manager, Path.getAbsolutePath(workingNode.getPath(), name), Permission.READ);
            this.node = workingNode.getNode(name);
            this.property = this.node.getNode(ItemType.getSystemName(ItemType.JCR_CONTENT)).getProperty(DATA_ELEMENT);
        }
    }

    /**
     * <p>
     * constructor | creates a new initialized NodeData
     * </p>
     * @param workingNode current active <code>Node</code>
     * @param name <code>NodeData</code> name to be created
     * @param value Value to be set
     * @param type PropertyType
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @deprecated as of magnolia 2.0
     */
    public NodeData(Node workingNode, String name, Value value, int type, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        this(workingNode, name, value, manager);
    }

    /**
     * <p>
     * constructor | creates a new initialized NodeData
     * </p>
     * @param workingNode current active <code>Node</code>
     * @param name <code>NodeData</code> name to be created
     * @param value Value to be set
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public NodeData(Node workingNode, String name, Value value, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(workingNode.getPath(), name), Permission.WRITE);
        this.node = workingNode.addNode(name, ItemType.getSystemName(ItemType.NT_NODEDATA));
        Node contentNode = this.node.addNode(ItemType.getSystemName(ItemType.JCR_CONTENT), ItemType
            .getSystemName(ItemType.NT_UNSTRUCTRUED));
        this.property = contentNode.setProperty(DATA_ELEMENT, value);
        this.setAccessManager(manager);
        this.addMixin(ItemType.getSystemName(ItemType.MIX_VERSIONABLE));
    }

    /**
     * <p>
     * constructor | creates a new initialized NodeData
     * </p>
     * @param node <code>Node</code> holding this property
     */
    public NodeData(Node node, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(node.getPath()), Permission.READ);
        this.node = node;
        this.property = this.node.getNode(ItemType.getSystemName(ItemType.JCR_CONTENT)).getProperty(DATA_ELEMENT);
        this.setAccessManager(manager);
    }

    /**
     * <p>
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
     * </p>
     * @return Value
     */
    public Value getValue() {
        try {
            return this.property.getValue();
        }
        catch (Exception e) {
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
            return this.getString().replaceAll("\n", lineBreak);
        }
        catch (Exception e) {
            return "";
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
            return "";
        }
    }

    /**
     * <p>
     * Returns the <code>long</code> representation of the value:
     * </p>
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
     * <p>
     * Returns the <code>double</code> representation of the value:
     * </p>
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
     * <p>
     * Returns the <code>Calendar</code> representation of the value:
     * </p>
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
     * <p>
     * Returns the <code>boolean</code> representation of the value:
     * </p>
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
     * <p>
     * Returns the <code>InputStream</code> representation of the value:
     * </p>
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
     * <p>
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
     * </p>
     * @return PropertyType
     */
    public int getType() {
        try {
            return this.property.getValue().getType();
        }
        catch (Exception e) {
            return PropertyType.UNDEFINED;
        }
    }

    /**
     * @return atom name
     */
    public String getName() {
        try {
            return this.node.getName();
        }
        catch (Exception e) {
            return "";
        }
    }

    /**
     * <p>
     * returns size in bytes
     * </p>
     * @return content length
     */
    public long getContentLength() {
        try {
            return this.property.getLength();
        }
        catch (RepositoryException re) {
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
     * <p>
     * set value of type <code>String</code>
     * </p>
     * @throws RepositoryException
     * @param value , string to be set
     */
    public void setValue(String value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * <p>
     * set value of type <code>int</code>
     * </p>
     * @throws RepositoryException
     * @param value , int value to be set
     */
    public void setValue(int value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * <p>
     * set value of type <code>long</code>
     * </p>
     * @throws RepositoryException
     * @param value , long value to be set
     */
    public void setValue(long value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * <p>
     * set value of type <code>InputStream</code>
     * </p>
     * @throws RepositoryException
     * @param value , InputStream to be set
     */
    public void setValue(InputStream value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * <p>
     * set value of type <code>double</code>
     * </p>
     * @throws RepositoryException
     * @param value , double value to be set
     */
    public void setValue(double value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * <p>
     * set value of type <code>boolean</code>
     * </p>
     * @throws RepositoryException
     * @param value , boolean value to be set
     */
    public void setValue(boolean value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * <p>
     * set value of type <code>Calendar</code>
     * </p>
     * @throws RepositoryException
     * @param value , Calendar value to be set
     */
    public void setValue(Calendar value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * <p>
     * set value of type <code>Value</code>
     * </p>
     * @throws RepositoryException
     * @param value
     */
    public void setValue(Value value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * <p>
     * checks if the atom exists in the repository
     * </p>
     * @return boolean
     */
    public boolean isExist() {
        return (this.property != null);
    }
}
