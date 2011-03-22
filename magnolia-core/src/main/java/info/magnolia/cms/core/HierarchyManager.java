/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.cms.core;

import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;

/**
 * Main class to access content. Is retrieved by {@link MgnlContext#getHierarchyManager(String)}.
 * The {@link DefaultHierarchyManager} wraps a JCR session.
 *
 * @author gjoseph
 * @version $Revision$ ($Author$)
 */
public interface HierarchyManager {

    AccessManager getAccessManager();

    QueryManager getQueryManager();

    Content createContent(String path, String label, String contentType) throws RepositoryException;

    Content getContent(String path) throws RepositoryException;

    Content getContent(String path, boolean create, ItemType type) throws RepositoryException;

    NodeData getNodeData(String path) throws RepositoryException;

    void delete(String path) throws RepositoryException;

    Content getRoot() throws RepositoryException;


    boolean isExist(String path);

    /**
     * Checks for the allowed access rights.
     * @param permissions permission mask
     * @param path path to content to be checked
     * @return true if the current user has access on the provided node path.
     */
    boolean isGranted(String path, long permissions);

    boolean isNodeData(String path) throws AccessDeniedException;

    Content getContentByUUID(String uuid) throws RepositoryException;

    Workspace getWorkspace();

    void moveTo(String source, String destination) throws RepositoryException;

    void copyTo(String source, String destination) throws RepositoryException;

    void save() throws RepositoryException;

    boolean hasPendingChanges() throws RepositoryException;

    void refresh(boolean keepChanges) throws RepositoryException;

    String getName();
}
