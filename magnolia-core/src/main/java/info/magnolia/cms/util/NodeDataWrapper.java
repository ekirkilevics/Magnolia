/**
 * This file Copyright (c) 2009 Magnolia International
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
package info.magnolia.cms.util;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;


/**
 *
 * @author pbracher
 * @version $Id$
 */
public abstract class NodeDataWrapper implements NodeData {

    private NodeData wrappedNodeData;

    public NodeDataWrapper() {
    }

    public NodeDataWrapper(NodeData wrappedNodeData) {
        this.wrappedNodeData = wrappedNodeData;
    }

    public NodeData getWrappedNodeData() {
        return this.wrappedNodeData;
    }

    public void setWrappedNodeData(NodeData wrappedNodeData) {
        this.wrappedNodeData = wrappedNodeData;
    }

    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(getClass().getSimpleName());
        buffer.append(" for ");
        buffer.append(getWrappedNodeData().toString());
        return buffer.toString();
    }

    // ---- below are only generated delegate methods
    public void delete() throws RepositoryException {
        getWrappedNodeData().delete();
    }

    public String getAttribute(String name) {
        return getWrappedNodeData().getAttribute(name);
    }

    public Collection getAttributeNames() throws RepositoryException {
        return getWrappedNodeData().getAttributeNames();
    }

    public boolean getBoolean() {
        return getWrappedNodeData().getBoolean();
    }

    public long getContentLength() {
        return getWrappedNodeData().getContentLength();
    }

    public Calendar getDate() {
        return getWrappedNodeData().getDate();
    }

    public double getDouble() {
        return getWrappedNodeData().getDouble();
    }

    public String getHandle() {
        return getWrappedNodeData().getHandle();
    }

    public HierarchyManager getHierarchyManager() throws RepositoryException {
        return getWrappedNodeData().getHierarchyManager();
    }

    public Property getJCRProperty() {
        return getWrappedNodeData().getJCRProperty();
    }

    public long getLong() {
        return getWrappedNodeData().getLong();
    }

    public String getName() {
        return getWrappedNodeData().getName();
    }

    public Content getParent() throws AccessDeniedException, ItemNotFoundException, javax.jcr.AccessDeniedException, RepositoryException {
        return getWrappedNodeData().getParent();
    }

    public Content getReferencedContent() throws RepositoryException, PathNotFoundException, RepositoryException {
        return getWrappedNodeData().getReferencedContent();
    }

    public Content getReferencedContent(String repositoryId) throws PathNotFoundException, RepositoryException {
        return getWrappedNodeData().getReferencedContent(repositoryId);
    }

    public InputStream getStream() {
        return getWrappedNodeData().getStream();
    }

    public String getString() {
        return getWrappedNodeData().getString();
    }

    public String getString(String lineBreak) {
        return getWrappedNodeData().getString(lineBreak);
    }

    public int getType() {
        return getWrappedNodeData().getType();
    }

    public Value getValue() {
        return getWrappedNodeData().getValue();
    }

    public Value[] getValues() {
        return getWrappedNodeData().getValues();
    }

    public boolean isExist() {
        return getWrappedNodeData().isExist();
    }

    public boolean isGranted(long permissions) {
        return getWrappedNodeData().isGranted(permissions);
    }

    public int isMultiValue() {
        return getWrappedNodeData().isMultiValue();
    }

    public void refresh(boolean keepChanges) throws RepositoryException {
        getWrappedNodeData().refresh(keepChanges);
    }

    public void save() throws RepositoryException {
        getWrappedNodeData().save();
    }

    public void setAttribute(String name, Calendar value) throws RepositoryException, AccessDeniedException, UnsupportedOperationException {
        getWrappedNodeData().setAttribute(name, value);
    }

    public void setAttribute(String name, String value) throws RepositoryException, AccessDeniedException, UnsupportedOperationException {
        getWrappedNodeData().setAttribute(name, value);
    }

    public void setValue(boolean value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    public void setValue(Calendar value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    public void setValue(double value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    public void setValue(InputStream value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    public void setValue(int value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    public void setValue(long value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    public void setValue(String value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    public void setValue(Value value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    public void setValue(Value[] value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

}