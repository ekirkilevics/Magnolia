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
package info.magnolia.api;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface HierarchyManager {

    void setAccessManager(AccessManager accessManager);

    AccessManager getAccessManager();

    void setQueryManager(QueryManager queryManager);

    QueryManager getQueryManager();

    /**
     * @deprecated use createContent(String path, String label, String contentType)
     * */
    Content createPage(String path, String label) throws RepositoryException;

    Content createContent(String path, String label, String contentType) throws RepositoryException;

    void setMetaData(MetaData md, String template) throws RepositoryException;

    void setMetaData(MetaData md) throws RepositoryException;

    void updateMetaData(MetaData md) throws RepositoryException;

    Content getPage(String path) throws RepositoryException;

    Content getContent(String path) throws RepositoryException;

    Content getContent(String path, boolean create, ItemType type) throws RepositoryException;

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
