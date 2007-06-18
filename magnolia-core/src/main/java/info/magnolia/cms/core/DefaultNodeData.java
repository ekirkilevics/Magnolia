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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wrapper class for a jcr property.
 * @author Sameer Charles
 * @version 2.0 $Id$
 */
public class DefaultNodeData extends ContentHandler implements NodeData {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DefaultNodeData.class);

    /**
     * Wrapped javax.jcr.Property.
     */
    protected Property property;

    /**
     * Wrapped javax.jcr.Node for nt:resource type
     */
    protected Node node;

    /**
     * Empty constructor. Should NEVER be used for standard use, test only.
     */
    protected DefaultNodeData() {
        // property is null
    }

    /**
     * Constructor. Create nodeData object to work-on based on existing <code>Property</code> or
     * <code>nt:resource</code>
     * @param workingNode current active <code>Node</code>
     * @param name <code>NodeData</code> name to be retrieved
     * @param manager Access manager to be used for this object
     */
    protected DefaultNodeData(Node workingNode, String name, AccessManager manager)
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
    protected DefaultNodeData(Node workingNode, String name, int type, boolean createNew, AccessManager manager)
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
    protected DefaultNodeData(Node workingNode, String name, Value value, AccessManager manager)
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
    public DefaultNodeData(Node node, AccessManager manager)
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
    public DefaultNodeData(Property property, AccessManager manager)
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
            else {
                throw e;
            }
        }
    }

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

    public String getString(String lineBreak) {
        try {
            return this.getString().replaceAll("\n", lineBreak); //$NON-NLS-1$
        }
        catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    public String getString() {
        try {
            return this.property.getString();
        }
        catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    public long getLong() {
        try {
            return this.property.getLong();
        }
        catch (Exception e) {
            return 0;
        }
    }

    public double getDouble() {
        try {
            return this.property.getDouble();
        }
        catch (Exception e) {
            return 0;
        }
    }

    public Calendar getDate() {
        try {
            return this.property.getDate();
        }
        catch (Exception e) {
            return null;
        }
    }

    public boolean getBoolean() {
        try {
            return this.property.getBoolean();
        }
        catch (Exception e) {
            return false;
        }
    }

    public InputStream getStream() {
        try {
            return this.property.getStream();
        }
        catch (Exception e) {
            return null;
        }
    }

    public Content getReferencedContent() throws RepositoryException, PathNotFoundException, RepositoryException  {
        // node containing this property
        Node node = property.getParent();
        Node refNode = null;

        if (property.getType() == PropertyType.REFERENCE) {
            refNode = property.getNode();
        }

        else if (property.getType() == PropertyType.PATH || property.getType() == PropertyType.STRING) {
            String path = this.getString();
            // is this relative path?
            if (path.startsWith("/")) {
                Node root = node.getSession().getRootNode();
                path = StringUtils.removeStart(path, "/");
                if(root.hasNode(path)){
                    refNode = root.getNode(path);
                }
            }
            else{
                if(node.hasNode(path)){
                    refNode = node.getNode(path);
                }
            }

            // we support uuids as strings
            if (refNode == null && property.getType() == PropertyType.STRING && !StringUtils.contains(path, "/")) {
                try {
                    refNode = node.getSession().getNodeByUUID(path);
                }
                catch (ItemNotFoundException e) {
                    // this is not an uuid
                }
            }
        }

        if(refNode==null){
            throw new ItemNotFoundException("can't find referenced node for value [" + getString() + "]");
        }

        return new DefaultContent(refNode, getAccessManager());
    }

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

    public long getContentLength() {
        try {
            return this.property.getLength();
        }
        catch (RepositoryException re) {
            log.warn("Unable to read content length for " + this.property); //$NON-NLS-1$
            return 0;
        }
    }

    public Property getJCRProperty() {
        return this.property;
    }

    public void setValue(String value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    public void setValue(int value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    public void setValue(long value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    public void setValue(InputStream value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        if (this.node != null) {
            this.property = this.node.setProperty(ItemType.JCR_DATA, value);
        }
        else {
            log.error("This is not a valid Binary type, Binary NodeData must be created with PropertyType.BINARY");
        }
    }

    public void setValue(double value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    public void setValue(boolean value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    public void setValue(Calendar value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    public void setValue(Value value) throws RepositoryException, AccessDeniedException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    public void setAttribute(String name, String value) throws RepositoryException, AccessDeniedException,
        UnsupportedOperationException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        if (null == this.node) {
            throw new UnsupportedOperationException("Attributes are only supported for BINARY type");
        }
        this.node.setProperty(name, value);
    }

    public void setAttribute(String name, Calendar value) throws RepositoryException, AccessDeniedException,
        UnsupportedOperationException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        if (null == this.node) {
            throw new UnsupportedOperationException("Attributes are only supported for BINARY type");
        }
        this.node.setProperty(name, value);
    }

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

    public Collection getAttributeNames() throws RepositoryException {
        Collection names = new ArrayList();
        if (this.node == null) {
            if (log.isDebugEnabled()) {
                log.debug("Attributes are only supported for BINARY type");
            }
            return names;
        }
        PropertyIterator properties = this.node.getProperties();
        while (properties.hasNext()) {
            String name = properties.nextProperty().getName();
            if (!name.equalsIgnoreCase(ItemType.JCR_DATA)) {
                names.add(name);
            }
        }
        return names;
    }

    public boolean isExist() {
        return (this.property != null);
    }

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

    public void save() throws RepositoryException {
        this.property.getSession().save();
    }

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

    public void delete() throws RepositoryException {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.property.getPath()), Permission.REMOVE);
        if (null != this.node) {
            this.node.remove();
        }
        else {
            this.property.remove();
        }
    }

    public void refresh(boolean keepChanges) throws RepositoryException {
        this.property.refresh(keepChanges);
    }

}
