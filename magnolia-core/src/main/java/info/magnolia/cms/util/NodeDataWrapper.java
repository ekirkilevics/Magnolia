/**
 * This file Copyright (c) 2009-2012 Magnolia International
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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;


/**
 * Wraps a {@link NodeData} to which it delegates. Used to manipulate node datas.
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

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(" for ");
        builder.append(getWrappedNodeData().toString());
        return builder.toString();
    }

    /**
     * Default implementation of content wrapping for cases where NodeData needs to return content.
     * 
     * @param content
     * @return
     */
    protected Content wrap(Content content) {
        return content;
    }

    // ---- below are only generated delegate methods
    @Override
    public void delete() throws RepositoryException {
        getWrappedNodeData().delete();
    }

    @Override
    public String getAttribute(String name) {
        return getWrappedNodeData().getAttribute(name);
    }

    @Override
    public Collection<String> getAttributeNames() throws RepositoryException {
        return getWrappedNodeData().getAttributeNames();
    }

    @Override
    public boolean getBoolean() {
        return getWrappedNodeData().getBoolean();
    }

    @Override
    public long getContentLength() {
        return getWrappedNodeData().getContentLength();
    }

    @Override
    public Calendar getDate() {
        return getWrappedNodeData().getDate();
    }

    @Override
    public double getDouble() {
        return getWrappedNodeData().getDouble();
    }

    @Override
    public String getHandle() {
        return getWrappedNodeData().getHandle();
    }

    @Override
    public HierarchyManager getHierarchyManager() {
        return getWrappedNodeData().getHierarchyManager();
    }

    @Override
    public Property getJCRProperty() throws PathNotFoundException {
        return getWrappedNodeData().getJCRProperty();
    }

    @Override
    public long getLong() {
        return getWrappedNodeData().getLong();
    }

    @Override
    public String getName() {
        return getWrappedNodeData().getName();
    }

    @Override
    public Content getParent() throws AccessDeniedException, ItemNotFoundException, javax.jcr.AccessDeniedException, RepositoryException {
        return wrap(getWrappedNodeData().getParent());
    }

    @Override
    public Content getReferencedContent() throws RepositoryException, PathNotFoundException, RepositoryException {
        return wrap(getWrappedNodeData().getReferencedContent());
    }

    @Override
    public Content getReferencedContent(String repositoryId) throws PathNotFoundException, RepositoryException {
        return wrap(getWrappedNodeData().getReferencedContent(repositoryId));
    }

    @Override
    public InputStream getStream() {
        return getWrappedNodeData().getStream();
    }

    @Override
    public String getString() {
        return getWrappedNodeData().getString();
    }

    @Override
    public String getString(String lineBreak) {
        return getWrappedNodeData().getString(lineBreak);
    }

    @Override
    public int getType() {
        return getWrappedNodeData().getType();
    }

    @Override
    public Value getValue() {
        return getWrappedNodeData().getValue();
    }

    @Override
    public Value[] getValues() {
        return getWrappedNodeData().getValues();
    }

    @Override
    public boolean isExist() {
        return getWrappedNodeData().isExist();
    }

    @Override
    public boolean isGranted(long permissions) {
        return getWrappedNodeData().isGranted(permissions);
    }

    @Override
    public int isMultiValue() {
        return getWrappedNodeData().isMultiValue();
    }

    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
        getWrappedNodeData().refresh(keepChanges);
    }

    @Override
    public void save() throws RepositoryException {
        getWrappedNodeData().save();
    }

    @Override
    public void setAttribute(String name, Calendar value) throws RepositoryException, AccessDeniedException, UnsupportedOperationException {
        getWrappedNodeData().setAttribute(name, value);
    }

    @Override
    public void setAttribute(String name, String value) throws RepositoryException, AccessDeniedException, UnsupportedOperationException {
        getWrappedNodeData().setAttribute(name, value);
    }

    @Override
    public void setValue(boolean value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    @Override
    public void setValue(Calendar value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    @Override
    public void setValue(double value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    @Override
    public void setValue(InputStream value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    @Override
    public void setValue(int value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    @Override
    public void setValue(long value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    @Override
    public void setValue(String value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    @Override
    public void setValue(Content value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    @Override
    public void setValue(Value value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

    @Override
    public void setValue(Value[] value) throws RepositoryException, AccessDeniedException {
        getWrappedNodeData().setValue(value);
    }

}
