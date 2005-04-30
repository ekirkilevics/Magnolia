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

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.util.Access;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.0
 */
public class NodeData extends ContentHandler {

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
        this.property = workingNode.getProperty(name);
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
            this.property = workingNode.setProperty(name, "");
        }
        else {
            Access.isGranted(manager, Path.getAbsolutePath(workingNode.getPath(), name), Permission.READ);
            this.property = workingNode.getProperty(name);
        }
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
        this.property = workingNode.setProperty(name, value);
        this.setAccessManager(manager);
    }

    /**
     * <p>
     * constructor | creates a new initialized NodeData
     * </p>
     * @param node <code>Node</code> holding this property
     * @deprecated
     */
    public NodeData(Node node, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(node.getPath()), Permission.READ);
        this.setAccessManager(manager);
    }

    /**
     * <p>
     * constructor | creates a new initialized NodeData
     * </p>
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
            return this.property.getType();
        }
        catch (Exception e) {
            log.warn("Unable to read property type for " + this.property);
            return PropertyType.UNDEFINED;
        }
    }

    /**
     * @return atom name
     */
    public String getName() {
        try {
            return this.property.getName();
        }
        catch (Exception e) {
            log.warn("Unable to read property name for " + this.property);
            return StringUtils.EMPTY;
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
            log.warn("Unable to read content length for " + this.property);
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

    /**
     * <p>
     * get a handle representing path relative to the content repository
     * </p>
     * @return String representing path (handle) of the content
     */
    public String getHandle() {
        try {
            return this.property.getPath();
        }
        catch (RepositoryException e) {
            log.error("Failed to get handle");
            log.error(e.getMessage(), e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * <p>
     * get a handle representing path relative to the content repository with the default extension
     * </p>
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @return String representing path (handle) of the content
     */
    public String getHandleWithDefaultExtension() throws PathNotFoundException, RepositoryException {
        return (this.property.getPath() + "." + Server.getDefaultExtension());
    }

    /**
     * <p>
     * get parent content object
     * </p>
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @return Content representing parent node
     */
    public Content getParent() throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return (new Content(this.property.getParent(), this.accessManager));
    }

    /**
     * <p>
     * get absolute parent object starting from the root node
     * </p>
     * @param digree level at which the requested node exist, relative to the ROOT node
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @return Content representing parent node
     */
    public Content getAncestor(int digree) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (digree > this.getLevel()) {
            throw new PathNotFoundException();
        }
        return (new Content(this.property.getAncestor(digree), this.accessManager));
    }

    /**
     * <p>
     * Convenience method for taglib
     * </p>
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @return Content representing node on level 0
     */
    public Collection getAncestors() throws PathNotFoundException, RepositoryException {
        List allAncestors = new ArrayList();
        int level = this.getLevel();
        while (level != 0) {
            try {
                allAncestors.add(new Content(this.property.getAncestor(--level), this.accessManager));
            }
            catch (AccessDeniedException e) {
                // valid
            }
        }
        return allAncestors;
    }

    /**
     * <p>
     * get node level from the ROOT node : FIXME implement getDepth in javax.jcr
     * </p>
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @return level at which current node exist, relative to the ROOT node
     */
    public int getLevel() throws PathNotFoundException, RepositoryException {
        return this.property.getPath().split("/").length - 1;
    }

    /**
     * <p>
     * Persists all changes to the repository if valiation succeds
     * </p>
     * @throws RepositoryException
     */
    public void save() throws RepositoryException {
        this.property.getSession().save();
    }

    /**
     * <p>
     * checks for the allowed access rights
     * </p>
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
     * <p>
     * Remove this path
     * </p>
     * @throws RepositoryException
     */
    public void delete() throws RepositoryException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.property.getPath()), Permission.REMOVE);
        this.property.remove();
    }

    /**
     * <p>
     * Refreshes current node keeping all changes
     * </p>
     * @see javax.jcr.Node#refresh(boolean)
     * @throws RepositoryException
     */
    public void refresh(boolean keepChanges) throws RepositoryException {
        this.property.refresh(keepChanges);
    }

}
