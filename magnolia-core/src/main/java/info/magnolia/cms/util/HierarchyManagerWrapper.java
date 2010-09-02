/**
 * This file Copyright (c) 2009-2010 Magnolia International
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
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;

/**
 * A {@link HierarchyManager} wrapping an other hierarchy manager. Subclassed to manipulate the hierarchy.
 * @version $Revision: $ ($Author: $)
 */
public abstract class HierarchyManagerWrapper implements HierarchyManager {

    private final HierarchyManager wrappedHM;

    protected HierarchyManagerWrapper(HierarchyManager wrappedHM) {
        this.wrappedHM = wrappedHM;
    }

    public HierarchyManager getWrappedHierarchyManager() {
        return wrappedHM;
    }

    /**
     * @deprecated since 4.3 use getWrappedHierarchyManager() instead
     */
    @Deprecated
    public HierarchyManager getDelegate() {
        return wrappedHM;
    }

    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(getClass().getSimpleName());
        buffer.append(" for ");
        buffer.append(getWrappedHierarchyManager().toString());
        return buffer.toString();
    }


    /**
     * Override this method to have hierarchy manager wrap every piece of content it returns.
     * @param content unwrapped content
     * @return wrapped content (or content passed in if not overriden)
     */
    protected Content wrap(Content content) {
        return content;
    }

    /**
     * Override this method to have hierarchy manager wrap every piece of node data it returns.
     * @param nodeData unwrapped node data
     * @return wrapped node data (or node data passed in if not overriden)
     */
    protected NodeData wrap(NodeData nodeData) {
        return nodeData;
    }

    /**
     * Override this method to alter all paths passed into the various hierarchy manager methods.
     * @param path unaltered path
     * @return wrapped path (or the one passed in if not overridden)
     */
    protected String transformPath(String path) {
        return path;
    }

    // ---- below are only generated wrappedHM methods
    public AccessManager getAccessManager() {
        return getWrappedHierarchyManager().getAccessManager();
    }

    public QueryManager getQueryManager() {
        return getWrappedHierarchyManager().getQueryManager();
    }

    public Content createContent(String path, String label, String contentType) throws RepositoryException {
        path = transformPath(path);
        return wrap(getWrappedHierarchyManager().createContent(path, label, contentType));
    }

    public Content getContent(String path) throws RepositoryException {
        path = transformPath(path);
        return wrap(getWrappedHierarchyManager().getContent(path));
    }

    public Content getContent(String path, boolean create, ItemType type) throws RepositoryException {
        path = transformPath(path);
        return wrap(getWrappedHierarchyManager().getContent(path, create, type));
    }

    public NodeData getNodeData(String path) throws RepositoryException {
        path = transformPath(path);
        return wrap(getWrappedHierarchyManager().getNodeData(path));
    }

    /**
     * @deprecated since 4.0 - use getContent().isNodeType() instead. (not used currently)
      */
     @Deprecated
    public Content getPage(String path, String templateName) throws RepositoryException {
        path = transformPath(path);
        return wrap(getWrappedHierarchyManager().getPage(path, templateName));
    }

    public void delete(String path) throws RepositoryException {
        path = transformPath(path);
        getWrappedHierarchyManager().delete(path);
    }

    public Content getRoot() throws RepositoryException {
        return wrap(getWrappedHierarchyManager().getRoot());
    }

    /**
     * @deprecated since 4.0 - use getContent().isNodeType() instead.
      */
     @Deprecated
    public boolean isPage(String path) throws AccessDeniedException {
        path = transformPath(path);
        return getWrappedHierarchyManager().isPage(path);
    }

    public boolean isExist(String path) {
        path = transformPath(path);
        return getWrappedHierarchyManager().isExist(path);
    }

    public boolean isGranted(String path, long permissions) {
        path = transformPath(path);
        return getWrappedHierarchyManager().isGranted(path, permissions);
    }

    /**
     * @deprecated since 4.0 - use getContent().isNodeType() instead.
      */
     @Deprecated
    public boolean isNodeType(String path, String type) {
        path = transformPath(path);
        return getWrappedHierarchyManager().isNodeType(path, type);
    }

    /**
    * @deprecated since 4.0 - use getContent().isNodeType() instead. (not used currently)
     */
    @Deprecated
    public boolean isNodeType(String path, ItemType type) {
        path = transformPath(path);
        return getWrappedHierarchyManager().isNodeType(path, type);
    }

    public boolean isNodeData(String path) throws AccessDeniedException {
        path = transformPath(path);
        return getWrappedHierarchyManager().isNodeData(path);
    }

    public Content getContentByUUID(String uuid) throws RepositoryException {
        return wrap(getWrappedHierarchyManager().getContentByUUID(uuid));
    }

    public Workspace getWorkspace() {
        return getWrappedHierarchyManager().getWorkspace();
    }

    public void moveTo(String source, String destination) throws RepositoryException {
        source = transformPath(source);
        destination = transformPath(destination);
        getWrappedHierarchyManager().moveTo(source, destination);
    }

    public void copyTo(String source, String destination) throws RepositoryException {
        source = transformPath(source);
        destination = transformPath(destination);
        getWrappedHierarchyManager().copyTo(source, destination);
    }

    public void save() throws RepositoryException {
        getWrappedHierarchyManager().save();
    }

    public boolean hasPendingChanges() throws RepositoryException {
        return getWrappedHierarchyManager().hasPendingChanges();
    }

    public void refresh(boolean keepChanges) throws RepositoryException {
        getWrappedHierarchyManager().refresh(keepChanges);
    }

    public String getName() {
        return getWrappedHierarchyManager().getName();
    }
}
