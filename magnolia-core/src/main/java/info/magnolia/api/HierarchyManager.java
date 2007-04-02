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

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Workspace;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface HierarchyManager {

    void setStartPage(Node rootNode) throws PathNotFoundException, RepositoryException;

    void init(Node rootNode) throws PathNotFoundException, RepositoryException;

    void init(Node rootNode, AccessManager manager) throws PathNotFoundException, RepositoryException;

    void setAccessManager(AccessManager accessManager);

    AccessManager getAccessManager();

    void setQueryManager(QueryManager queryManager);

    QueryManager getQueryManager();

    Content createPage(String path, String label) throws PathNotFoundException, RepositoryException,
            AccessDeniedException;

    Content createContent(String path, String label, String contentType) throws PathNotFoundException,
            RepositoryException, AccessDeniedException;

    void setMetaData(MetaData md, String template) throws RepositoryException, AccessDeniedException;

    void setMetaData(MetaData md) throws RepositoryException, AccessDeniedException;

    void updateMetaData(MetaData md) throws RepositoryException, AccessDeniedException;

    Content getPage(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    Content getContent(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    Content getContent(String path, boolean create, ItemType type) throws AccessDeniedException,
        RepositoryException;

    Content getContentNode(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    NodeData getNodeData(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    Content getPage(String path, String templateName) throws PathNotFoundException, RepositoryException,
        AccessDeniedException;

    void delete(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    Content getRoot() throws RepositoryException, AccessDeniedException;

    boolean isPage(String path) throws AccessDeniedException;

    boolean isExist(String path);

    boolean isNodeType(String path, String type);

    boolean isNodeType(String path, ItemType type);

    boolean isNodeData(String path) throws AccessDeniedException;

    Content getContentByUUID(String uuid) throws ItemNotFoundException, RepositoryException,
        AccessDeniedException;

    Workspace getWorkspace();

    void moveTo(String source, String destination) throws PathNotFoundException, RepositoryException,
        AccessDeniedException;

    void copyTo(String source, String destination) throws PathNotFoundException, RepositoryException,
            AccessDeniedException;

    void save() throws RepositoryException;

    boolean hasPendingChanges() throws RepositoryException;

    void refresh(boolean keepChanges) throws RepositoryException;
}
