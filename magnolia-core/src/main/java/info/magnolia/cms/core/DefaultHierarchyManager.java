/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.WorkspaceAccessUtil;
import info.magnolia.cms.beans.config.ContentRepository;

import java.util.Collection;
import java.util.Iterator;
import java.io.ObjectStreamField;
import java.io.Serializable;

import javax.jcr.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * $Id:HierarchyManager.java 2719 2006-04-27 14:38:44Z scharles $
 */
public class DefaultHierarchyManager implements HierarchyManager, Serializable {

    private static final long serialVersionUID = 222L;

    /**
     * instead of defining each field transient, we explicitly says what needs to be
     * serialized
     * */
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("userId", String.class),
            new ObjectStreamField("repositoryName", String.class),
            new ObjectStreamField("workspaceName", String.class),
            new ObjectStreamField("accessManager", AccessManager.class)
    };

    private static Logger log = LoggerFactory.getLogger(DefaultHierarchyManager.class);

    private Node rootNode;

    private Workspace workspace;

    private Session jcrSession;

    private QueryManager queryManager;

    /**
     * All serializable properties
     * */
    private String userId;

    private String repositoryName;

    private String workspaceName;

    private AccessManager accessManager;

    public DefaultHierarchyManager() {}

    public DefaultHierarchyManager(String userId,
                                   Session jcrSession,
                                   AccessManager aManager,
                                   QueryManager qManager)
            throws RepositoryException {
        this.userId = userId;
        this.jcrSession = jcrSession;
        this.rootNode = jcrSession.getRootNode();
        this.workspace = jcrSession.getWorkspace();
        this.workspaceName = this.workspace.getName();
        this.repositoryName = ContentRepository.getParentRepositoryName(this.workspaceName);
        this.accessManager = aManager;
        this.queryManager = qManager;

    }

    /**
     * Reinitialize itself with the partial deserialized data
     * */
    private void reInitialize() {
        WorkspaceAccessUtil util = WorkspaceAccessUtil.getInstance();
        try {
            this.jcrSession =
                    util.createRepositorySession(util.getDefaultCredentials(), this.repositoryName, this.workspaceName);
            this.queryManager = util.createQueryManager(this.jcrSession, this.accessManager);
            this.rootNode = this.jcrSession.getRootNode();
            this.workspace = this.jcrSession.getWorkspace();
        } catch (RepositoryException re) {
            log.error("Failed to load HierarchyManager from persistent storage", re);
        }
    }

    /**
     * Set access manager for this hierarchy
     * @param accessManager
     */
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    /**
     * Get access manager
     * @return accessmanager attached to this hierarchy
     */
    public AccessManager getAccessManager() {
        return this.accessManager;
    }

    /**
     * Set query manager for this hierarchy
     * @param queryManager
     */
    public void setQueryManager(QueryManager queryManager) {
        this.queryManager = queryManager;
    }

    public QueryManager getQueryManager() {
        if (null == this.queryManager) {
            reInitialize();
        }
        return this.queryManager;
    }

    private Node getRootNode() {
        if (null == this.rootNode) {
            reInitialize();
        }
        return this.rootNode;
    }

    private Session getJcrSession() {
        if (null == this.jcrSession) {
            reInitialize();
        }
        return this.jcrSession;
    }

    /**
     * creates a new content page
     * @param path parent handle under which new page has to be created
     * @param label page name to be created
     * @return Content newly created hierarchy node
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     *
     * @deprecated use createContent(String path, String label, String contentType)
     */
    public Content createPage(String path, String label) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        log.warn("Deprecated: Use createContent(String path, String label, String contentType)");
        Content newPage = (new DefaultContent(
            this.getRootNode(),
            this.getNodePath(path, label),
            ItemType.CONTENT.getSystemName(),
            this.accessManager));
        this.setMetaData(newPage.getMetaData());
        return newPage;
    }

    /**
     * creates contentNode of type <b>contentType </b> contentType must be defined in item type definition of magnolia
     * as well as JCR implementation
     * @param path absolute (primary) path to this <code>Node</code>
     * @param label page name
     * @param contentType , JCR node type as configured
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    public Content createContent(String path, String label, String contentType) throws PathNotFoundException,
        RepositoryException, AccessDeniedException {
        Content content = new DefaultContent(this.getRootNode(), this.getNodePath(path, label), contentType, this.accessManager);
        setMetaData(content.getMetaData());
        return content;
    }

    private String getNodePath(String path, String label) {
        if (StringUtils.isEmpty(path) || (path.equals("/"))) { //$NON-NLS-1$
            return label;
        }
        return getNodePath(path + "/" + label); //$NON-NLS-1$
    }

    private String getNodePath(String path) {
        if (path != null && path.startsWith("/")) { //$NON-NLS-1$
            return path.replaceFirst("/", StringUtils.EMPTY); //$NON-NLS-1$
        }
        return path;
    }

    /**
     * Helper method to set page properties, create page calls this method. you could call this method anytime to create
     * working page properties
     */
    public void setMetaData(MetaData md, String template) throws RepositoryException, AccessDeniedException {
        md.setTemplate(template);
        setMetaData(md);
    }

    /**
     * Helper method to set page properties, create page calls this method. you could call this method anytime to create
     * working page properties
     */
    public void setMetaData(MetaData md) throws RepositoryException, AccessDeniedException {
        md.setCreationDate();
        md.setModificationDate();
        md.setAuthorId(this.userId);
        md.setTitle(StringUtils.EMPTY);
    }

    /**
     * Helper method to set page properties, get page calls this method. you could call this method anytime to update
     * working page properties
     */
    public void updateMetaData(MetaData md) throws RepositoryException, AccessDeniedException {
        md.setModificationDate();
        md.setAuthorId(this.userId);
    }

    /**
     * returns the page specified by path in the parameter
     * @param path handle of the page to be initialized
     * @return Content hierarchy node
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @deprecated use getContent(String path) instead
     */
    public Content getPage(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.getContent(path);
    }

    /**
     * get content object of the requested URI
     * @param path of the content to be initialized
     * @return Content
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public Content getContent(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (path.equals("/")) { //$NON-NLS-1$
            return this.getRoot();
        }
        return (new DefaultContent(this.getRootNode(), getNodePath(path), this.accessManager));
    }

    /**
     * Like getContent() but creates the node if not yet existing. Attention save is not called!
     * @param path the path of the node
     * @param create true if the node should get created
     * @param type the node type of the created node
     * @return the node
     * @throws AccessDeniedException
     * @throws RepositoryException
     */
    public Content getContent(String path, boolean create, ItemType type) throws AccessDeniedException,
        RepositoryException {
        Content node;
        try {
            node = getContent(path);
        }
        catch (PathNotFoundException e) {
            if (create) {
                node = this.createContent(StringUtils.substringBeforeLast(path, "/"), StringUtils.substringAfterLast(
                    path,
                    "/"), type.toString());
            }
            else {
                throw e;
            }
        }
        return node;
    }

    /**
     * get content node object of the requested URI
     * @param path of the content (container / containerlist) to be initialized
     * @return ContentNode
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @deprecated use getContent(String path) instead
     */
    public Content getContentNode(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return new DefaultContent(this.getRootNode(), getNodePath(path), this.accessManager);
    }

    /**
     * get NodeData object of the requested URI
     * @param path of the atom to be initialized
     * @return NodeData
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public NodeData getNodeData(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        String nodePath = getNodePath(path);
        if (StringUtils.isEmpty(nodePath)) {
            return null;
        }

        return new DefaultNodeData(this.getRootNode(), nodePath, this.accessManager);
    }

    /**
     * returns the first page with a given template name that is found in tree that starts from the page given py the
     * path (including this page)
     * @param path handle of the page from where the search should start
     * @param templateName template name to search for
     * @return first Content hierarchy node that has the specified template name assigned
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public Content getPage(String path, String templateName) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Content page = getContent(path);
        if (page.getTemplate().equals(templateName)) {
            return page;
        }
        Content pageToBeFound = null;
        try {
            if (page.hasChildren()) {
                Collection children = page.getChildren(ItemType.CONTENT.getSystemName());
                Iterator iterator = children.iterator();
                while (iterator.hasNext()) {
                    Content child = (Content) iterator.next();
                    if (child.getTemplate().equals(templateName)) {
                        return child;
                    }
                    if (child.hasChildren()) {
                        pageToBeFound = getPage(child.getHandle(), templateName);
                    }
                    if (pageToBeFound != null) {
                        return pageToBeFound;
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to get - " + path); //$NON-NLS-1$
            log.error(e.getMessage(), e);
        }
        return pageToBeFound;
    }

    /**
     * removes specified path, it can be either node or property
     * @param path to be removed
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @throws AccessDeniedException
     */
    public void delete(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (this.isNodeData(path)) {
            this.getNodeData(makeRelative(path)).delete();
        }
        else {
            this.getRootNode().getNode(makeRelative(path)).remove();
        }

    }

    private String makeRelative(String path) {
        return StringUtils.stripStart(path, "/"); //$NON-NLS-1$
    }

    /**
     * @return rootNode of the current working repository-workspace
     */
    public Content getRoot() throws RepositoryException, AccessDeniedException {
        return (new DefaultContent(this.getRootNode(), this.accessManager));
    }

    /**
     * Checks if the requested resource is a page (hierarchy Node).
     * @param path of the requested content
     * @return boolean true is the requested content is a Hierarchy Node todo remove this method, instead use
     * (getContent(PATH) is NodeType)
     */
    public boolean isPage(String path) throws AccessDeniedException {
        Access.isGranted(this.accessManager, path, Permission.READ);

        String nodePath = getNodePath(path);
        if (StringUtils.isEmpty(nodePath)) {
            return false;
        }

        try {
            Node n = this.getRootNode().getNode(nodePath);
            return (n.isNodeType(ItemType.CONTENT.getSystemName()));
        }
        catch (RepositoryException re) {
            // ignore, not existing?
        }
        return false;
    }

    /**
     * check is either the node or property exists with the specified path
     * @param path
     */
    public boolean isExist(String path) {
        try {
            Access.isGranted(this.accessManager, path, Permission.READ);
        }
        catch (AccessDeniedException e) {
            log.error(e.getMessage());
            return false;
        }
        boolean isExist = false;
        try {
            isExist = this.getJcrSession().itemExists(path);
        }
        catch (RepositoryException re) {
            log.error("Exception caught", re);
        }
        return isExist;
    }

    /**
     * Evaluate primary node type of the node at the given path.
     */
    public boolean isNodeType(String path, String type) {
        try {
            Node n = this.getRootNode().getNode(getNodePath(path));
            return n.isNodeType(type);
        }
        catch (RepositoryException re) {
            log.error(re.getMessage());
            log.debug(re.getMessage(), re);
        }
        return false;
    }

    /**
     * Evaluate primary node type of the node at the given path.
     */
    public boolean isNodeType(String path, ItemType type) {
        return isNodeType(path, type.getSystemName());
    }

    /**
     * checks if the requested resource is an NodeData (Property)
     * @param path of the requested NodeData
     * @return boolean true is the requested content is an NodeData
     */
    public boolean isNodeData(String path) throws AccessDeniedException {
        Access.isGranted(this.accessManager, path, Permission.READ);
        boolean result = false;
        String nodePath = getNodePath(path);
        if (StringUtils.isEmpty(nodePath)) {
            return false;
        }
        try {
            result = this.getRootNode().hasProperty(nodePath);
            if (!result) {
                // check if its a nt:resource
                result = this.getRootNode().hasProperty(nodePath + "/" + ItemType.JCR_DATA);
            }
        }
        catch (RepositoryException e) {
            // ignore, no property
        }
        return result;
    }

    /**
     * This method can be used to retrieve Content which has UUID assigned to it, in other words only those nodes which
     * has mixin type mix:referenceable.
     * @param uuid
     */
    public Content getContentByUUID(String uuid) throws ItemNotFoundException, RepositoryException,
        AccessDeniedException {
        return new DefaultContent(this.getJcrSession().getNodeByUUID(uuid), this.accessManager);
    }

    /**
     * gets currently used workspace for this hierarchy manager
     */
    public Workspace getWorkspace() {
        if (null == this.workspace) {
            reInitialize();
        }
        return this.workspace;
    }

    /**
     * move content to the specified location
     * @param source source node path
     * @param destination node where the node has to be moved
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void moveTo(String source, String destination) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Access.isGranted(this.accessManager, source, Permission.REMOVE);
        Access.isGranted(this.accessManager, destination, Permission.WRITE);
        this.getWorkspace().move(source, destination);
    }

    /**
     * copy content to the specified location
     * @param source source node path
     * @param destination node where the node has to be copied
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void copyTo(String source, String destination) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Access.isGranted(this.accessManager, source, Permission.READ);
        Access.isGranted(this.accessManager, destination, Permission.WRITE);
        this.getWorkspace().copy(source, destination);
    }

    /**
     * Persists all changes to the repository if validation succeds
     * @throws RepositoryException
     */
    public void save() throws RepositoryException {
        try {
            this.getJcrSession().save();
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
            throw re;
        }
    }

    /**
     * Returns true if the session has pending (unsaved) changes.
     */
    public boolean hasPendingChanges() throws RepositoryException {
        return this.getJcrSession().hasPendingChanges();
    }

    /**
     * Refreshes this session
     * @param keepChanges
     * @throws RepositoryException
     * @see javax.jcr.Session#refresh(boolean)
     */
    public void refresh(boolean keepChanges) throws RepositoryException {
        this.getJcrSession().refresh(keepChanges);
    }

}
