/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core;

import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.core.util.Access;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Path;

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

public class NodeData
{

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(NodeData.class);

    public static final String HTML_LINEBREAK = "<br />";

    private static final String DATA_ELEMENT = "Data";

    private Property property;

    private Node dataFile;

    private AccessManager accessManager;

    /**
     * package private constructor
     */
    NodeData()
    {
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
        AccessDeniedException
    {
        Access.isGranted(manager, Path.getAbsolutePath(workingNode.getPath(), name), Permission.READ);
        this.dataFile = workingNode.getNode(name);
        this.property = this.dataFile.getNode(ItemType.getSystemName(ItemType.JCR_CONTENT)).getProperty(DATA_ELEMENT);
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
        AccessDeniedException
    {
        if (createNew)
        {
            Access.isGranted(manager, Path.getAbsolutePath(workingNode.getPath(), name), Permission.WRITE);
            this.dataFile = workingNode.addNode(name, ItemType.getSystemName(ItemType.NT_NODEDATA));
            Node contentNode = this.dataFile.addNode(ItemType.getSystemName(ItemType.JCR_CONTENT), ItemType
                .getSystemName(ItemType.NT_UNSTRUCTRUED));
            this.property = contentNode.setProperty(DATA_ELEMENT, "");
            if (this.dataFile.canAddMixin(ItemType.getSystemName(ItemType.MIX_Versionable)))
            {
                this.dataFile.addMixin(ItemType.getSystemName(ItemType.MIX_Versionable));
            }
        }
        else
        {
            Access.isGranted(manager, Path.getAbsolutePath(workingNode.getPath(), name), Permission.READ);
            this.dataFile = workingNode.getNode(name);
            this.property = this.dataFile.getNode(ItemType.getSystemName(ItemType.JCR_CONTENT)).getProperty(
                DATA_ELEMENT);
        }
        this.setAccessManager(manager);
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
        AccessDeniedException
    {
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
        AccessDeniedException
    {
        Access.isGranted(manager, Path.getAbsolutePath(workingNode.getPath(), name), Permission.WRITE);
        this.dataFile = workingNode.addNode(name, ItemType.getSystemName(ItemType.NT_NODEDATA));
        Node contentNode = this.dataFile.addNode(ItemType.getSystemName(ItemType.JCR_CONTENT), ItemType
            .getSystemName(ItemType.NT_UNSTRUCTRUED));
        this.property = contentNode.setProperty(DATA_ELEMENT, value);
        if (this.dataFile.canAddMixin(ItemType.getSystemName(ItemType.MIX_Versionable)))
        {
            this.dataFile.addMixin(ItemType.getSystemName(ItemType.MIX_Versionable));
        }
        this.setAccessManager(manager);
    }

    /**
     * <p>
     * constructor | creates a new initialized NodeData
     * </p>
     * @param dataFile <code>Node</code> holding this property
     */
    public NodeData(Node dataFile, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException
    {
        Access.isGranted(manager, Path.getAbsolutePath(dataFile.getPath()), Permission.READ);
        this.dataFile = dataFile;
        this.property = this.dataFile.getNode(ItemType.getSystemName(ItemType.JCR_CONTENT)).getProperty(DATA_ELEMENT);
        this.setAccessManager(manager);
    }

    public void setAccessManager(AccessManager manager)
    {
        this.accessManager = manager;
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
    public Value getValue()
    {
        try
        {
            return this.property.getValue();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * <p>
     * Returns the <code>String</code> representation of the value: <br>
     * decodes like breaks with the specified regular expresion
     * </p>
     * @param lineBreak , regular expession
     * @return String
     */
    public String getString(String lineBreak)
    {
        try
        {
            return this.getString().replaceAll("\n", lineBreak);
        }
        catch (Exception e)
        {
            return "";
        }
    }

    /**
     * <p>
     * Returns the <code>String</code> representation of the value:
     * </p>
     * @return String
     */
    public String getString()
    {
        try
        {
            return this.property.getString();
        }
        catch (Exception e)
        {
            return "";
        }
    }

    /**
     * <p>
     * Returns the <code>long</code> representation of the value:
     * </p>
     * @return long
     */
    public long getLong()
    {
        try
        {
            return this.property.getLong();
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    /**
     * <p>
     * Returns the <code>double</code> representation of the value:
     * </p>
     * @return double
     */
    public double getDouble()
    {
        try
        {
            return this.property.getDouble();
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    /**
     * <p>
     * Returns the <code>Calendar</code> representation of the value:
     * </p>
     * @return Calendar
     */
    public Calendar getDate()
    {
        try
        {
            return this.property.getDate();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * <p>
     * Returns the <code>boolean</code> representation of the value:
     * </p>
     * @return boolean
     */
    public boolean getBoolean()
    {
        try
        {
            return this.property.getBoolean();
        }
        catch (Exception e)
        {
            return false;
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
    public int getType()
    {
        try
        {
            return this.property.getValue().getType();
        }
        catch (Exception e)
        {
            return PropertyType.UNDEFINED;
        }
    }

    /**
     * @return atom name
     */
    public String getName()
    {
        try
        {
            return this.dataFile.getName();
        }
        catch (Exception e)
        {
            return "";
        }
    }

    /**
     * <p>
     * returns size in bytes
     * </p>
     * @return content length
     */
    public long getContentLength()
    {
        try
        {
            return this.property.getLength();
        }
        catch (RepositoryException re)
        {
            return 0;
        }
    }

    /**
     * <p>
     * Access to property at the JCR level. <br>
     * <b>available only to be available, should not be used in normal circunstances! </b>
     * </p>
     * @return Property
     */
    public Property getJCRProperty()
    {
        return this.property;
    }

    /**
     * <p>
     * set value of type <code>String</code>
     * </p>
     * @throws RepositoryException
     * @param value , string to be set
     */
    public void setValue(String value) throws RepositoryException, AccessDeniedException
    {
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
    public void setValue(int value) throws RepositoryException, AccessDeniedException
    {
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
    public void setValue(long value) throws RepositoryException, AccessDeniedException
    {
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
    public void setValue(InputStream value) throws RepositoryException, AccessDeniedException
    {
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
    public void setValue(double value) throws RepositoryException, AccessDeniedException
    {
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
    public void setValue(boolean value) throws RepositoryException, AccessDeniedException
    {
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
    public void setValue(Calendar value) throws RepositoryException, AccessDeniedException
    {
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
    public void setValue(Value value) throws RepositoryException, AccessDeniedException
    {
        Access.isGranted(this.accessManager, Path.getAbsolutePath(this.getHandle()), Permission.SET);
        this.property.setValue(value);
    }

    /**
     * <p>
     * checks if the atom exists in the repository
     * </p>
     * @return boolean
     */
    public boolean isExist()
    {
        return (this.property != null);
    }

    /**
     * <p>
     * path as specified by JCR
     * </p>
     * @return NodeData path relative to the repository
     */
    public String getHandle()
    {
        try
        {
            return this.dataFile.getPath();
        }
        catch (RepositoryException e)
        {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * <p>
     * checks for the allowed access rights
     * </p>
     * @param permissions as defined in javax.jcr.Permission
     * @return true is the current user has specified access on this node.
     */
    public boolean isGranted(long permissions)
    {
        try
        {
            Access.isGranted(this.accessManager, Path.getAbsolutePath(this.dataFile.getPath()), permissions);
            return true;
        }
        catch (RepositoryException re)
        {
            log.error(re.getMessage(), re);
        }
        return false;
    }

}
