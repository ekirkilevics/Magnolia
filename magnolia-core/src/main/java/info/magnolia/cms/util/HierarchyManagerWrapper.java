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
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class HierarchyManagerWrapper implements HierarchyManager {

    private final HierarchyManager delegate;

    protected HierarchyManagerWrapper(HierarchyManager delegate) {
        this.delegate = delegate;
    }

    public HierarchyManager getDelegate() {
        return delegate;
    }

    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(getClass().getSimpleName());
        buffer.append(" for ");
        buffer.append(getDelegate().toString());
        return buffer.toString();
    }

    // ---- below are only generated delegate methods
    public AccessManager getAccessManager() {
        return getDelegate().getAccessManager();
    }

    public QueryManager getQueryManager() {
        return getDelegate().getQueryManager();
    }

    public Content createContent(String path, String label, String contentType) throws RepositoryException {
        return getDelegate().createContent(path, label, contentType);
    }

    public Content getContent(String path) throws RepositoryException {
        return getDelegate().getContent(path);
    }

    public Content getContent(String path, boolean create, ItemType type) throws RepositoryException {
        return getDelegate().getContent(path, create, type);
    }

    public NodeData getNodeData(String path) throws RepositoryException {
        return getDelegate().getNodeData(path);
    }

    public Content getPage(String path, String templateName) throws RepositoryException {
        return getDelegate().getPage(path, templateName);
    }

    public void delete(String path) throws RepositoryException {
        getDelegate().delete(path);
    }

    public Content getRoot() throws RepositoryException {
        return getDelegate().getRoot();
    }

    public boolean isPage(String path) throws AccessDeniedException {
        return getDelegate().isPage(path);
    }

    public boolean isExist(String path) {
        return getDelegate().isExist(path);
    }

    public boolean isNodeType(String path, String type) {
        return getDelegate().isNodeType(path, type);
    }

    public boolean isNodeType(String path, ItemType type) {
        return getDelegate().isNodeType(path, type);
    }

    public boolean isNodeData(String path) throws AccessDeniedException {
        return getDelegate().isNodeData(path);
    }

    public Content getContentByUUID(String uuid) throws RepositoryException {
        return getDelegate().getContentByUUID(uuid);
    }

    public Workspace getWorkspace() {
        return getDelegate().getWorkspace();
    }

    public void moveTo(String source, String destination) throws RepositoryException {
        getDelegate().moveTo(source, destination);
    }

    public void copyTo(String source, String destination) throws RepositoryException {
        getDelegate().copyTo(source, destination);
    }

    public void save() throws RepositoryException {
        getDelegate().save();
    }

    public boolean hasPendingChanges() throws RepositoryException {
        return getDelegate().hasPendingChanges();
    }

    public void refresh(boolean keepChanges) throws RepositoryException {
        getDelegate().refresh(keepChanges);
    }

    public String getName() {
        return getDelegate().getName();
    }

}