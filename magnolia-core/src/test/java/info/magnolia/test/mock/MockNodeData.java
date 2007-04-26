/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.test.mock;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.NodeDataUtil;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.RepositoryException;


/**
 * @author philipp
 * @version $Id$
 */
public class MockNodeData extends NodeData {

    private String name;

    private Object value;

    private Content parent;

    public MockNodeData(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getString() {
        return value !=null ? value.toString() : "";
    }

    public int getType() {
        return NodeDataUtil.getJCRPropertyType(value);
    }

    public Content getParent() {
        return this.parent;
    }

    public void setParent(Content parent) {
        this.parent = parent;
    }

    public boolean getBoolean() {
        return ((Boolean) value).booleanValue();
    }

    public Calendar getDate() {
        return (Calendar) value;
    }

    public double getDouble() {
        return ((Double) value).doubleValue();
    }

    public long getLong() {
        if (value instanceof Integer) {
            return ((Integer)value).longValue();
        }
        return ((Long) value).longValue();
    }

    public InputStream getStream() {
        return (InputStream) value;
    }

    public Content getReferencedContent() throws RepositoryException {
        if (value instanceof Content) {
            return (Content) value;
        }
        throw new RepositoryException("Value is not Content, a real NodeData/javax.jcr.Property will not allow this either");
    }

    public String getHandle() {
        return this.getParent().getHandle() + "/" + this.getName();
    }

    public boolean isExist() {
        return value != null;
    }

    public void setValue(boolean value) throws RepositoryException, AccessDeniedException {
        this.value = Boolean.valueOf(value);
    }

    public void setValue(Calendar value) throws RepositoryException, AccessDeniedException {
        this.value = value;
    }

    public void setValue(double value) throws RepositoryException, AccessDeniedException {
        this.value = new Double(value);
    }

    public void setValue(InputStream value) throws RepositoryException, AccessDeniedException {
        this.value = value;
    }

    public void setValue(int value) throws RepositoryException, AccessDeniedException {
        this.value = new Integer(value);
    }

    public void setValue(long value) throws RepositoryException, AccessDeniedException {
        this.value = new Long(value);
    }

    public void setValue(String value) throws RepositoryException, AccessDeniedException {
        this.value = value;
    }

    public String toString() {
        return this.getHandle() + ": " + this.getString();
    }

}