/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
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
 * @author gjoseph
 * @version $Revision$ ($Author$)
 */
public interface HierarchyManager {

    void setAccessManager(AccessManager accessManager);

    AccessManager getAccessManager();

    void setQueryManager(QueryManager queryManager);

    QueryManager getQueryManager();

    /**
     * @deprecated use createContent(String path, String label, String contentType)
     */
    Content createPage(String path, String label) throws RepositoryException;

    Content createContent(String path, String label, String contentType) throws RepositoryException;

    /**
     * @deprecated since 3.6 - not used
     */
    void setMetaData(MetaData md, String template) throws RepositoryException;

    /**
     * @deprecated since 3.6 - only used in implementation, should not be exposed in interface
     */
    void setMetaData(MetaData md) throws RepositoryException;

    /**
     * @deprecated since 3.6 - not used
     */
    void updateMetaData(MetaData md) throws RepositoryException;

    /**
     * @deprecated use getContent(String path) instead
     */
    Content getPage(String path) throws RepositoryException;

    Content getContent(String path) throws RepositoryException;

    Content getContent(String path, boolean create, ItemType type) throws RepositoryException;

    /**
     * @deprecated use getContent(String path) instead
     */
    Content getContentNode(String path) throws RepositoryException;

    NodeData getNodeData(String path) throws RepositoryException;

    Content getPage(String path, String templateName) throws RepositoryException;

    void delete(String path) throws RepositoryException;

    Content getRoot() throws RepositoryException;

    boolean isPage(String path) throws AccessDeniedException;

    boolean isExist(String path);

    boolean isNodeType(String path, String type);

    boolean isNodeType(String path, ItemType type);

    boolean isNodeData(String path) throws AccessDeniedException;

    Content getContentByUUID(String uuid) throws RepositoryException;

    Workspace getWorkspace();

    void moveTo(String source, String destination) throws RepositoryException;

    void copyTo(String source, String destination) throws RepositoryException;

    void save() throws RepositoryException;

    boolean hasPendingChanges() throws RepositoryException;

    void refresh(boolean keepChanges) throws RepositoryException;
}
