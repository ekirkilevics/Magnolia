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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.WorkspaceAccessUtil;
import info.magnolia.logging.AuditLoggingUtil;

import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Collection;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default JCR-based implementation of {@link HierarchyManager}.
 *
 * @author Sameer Charles
 * $Id:HierarchyManager.java 2719 2006-04-27 14:38:44Z scharles $
 * @deprecated since 5.0. Use Session and its methods instead.
 */
@Deprecated
public class DefaultHierarchyManager implements HierarchyManager, Serializable {

    private static final long serialVersionUID = 223L;

    /**
     * instead of defining each field transient, we explicitly says what needs to be
     * serialized.
     * */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("userId", String.class),
        new ObjectStreamField("repositoryName", String.class),
        new ObjectStreamField("workspaceName", String.class),
    };

    private static final Logger log = LoggerFactory.getLogger(DefaultHierarchyManager.class);

    private Node rootNode;

    private Workspace workspace;

    private Session jcrSession;

    private QueryManager queryManager;

    /**
     * All serializable properties.
     * */
    private String userId;

    private String repositoryName;

    private String workspaceName;

    protected DefaultHierarchyManager() {}

    public DefaultHierarchyManager(String userId,
            Session jcrSession,
            AccessManager aManager)
    throws RepositoryException {
        this.userId = userId;
        this.jcrSession = jcrSession;
        this.rootNode = jcrSession.getRootNode();
        this.workspace = jcrSession.getWorkspace();
        this.workspaceName = this.workspace.getName();
        this.repositoryName = ContentRepository.getParentRepositoryName(this.workspaceName);
    }

    /**
     * Reinitialize itself with the partial deserialized data.
     * */
    private void reInitialize() {
        WorkspaceAccessUtil util = WorkspaceAccessUtil.getInstance();
        try {
            this.jcrSession = util.createRepositorySession(util.getDefaultCredentials(), this.repositoryName, this.workspaceName);
            this.queryManager = util.createQueryManager(this.jcrSession, this);
            this.rootNode = this.jcrSession.getRootNode();
            this.workspace = this.jcrSession.getWorkspace();
        } catch (RepositoryException re) {
            log.error("Failed to load HierarchyManager from persistent storage", re);
        }
    }

    /**
     * Set access manager for this hierarchy.
     * @param accessManager
     */
    protected void setAccessManager(AccessManager accessManager) {
        // throw an ex because letting this fall through would be too unsecure for code relying on the old behavior
        throw new UnsupportedOperationException("Custom access managers are no longer supported. Use Repository level security checks instead.");
    }

    /**
     * Get access manager.
     * @return accessmanager attached to this hierarchy
     */
    public AccessManager getAccessManager() {
        // throw an ex because letting this fall through would be too unsecure for code relying on the old behavior
        throw new UnsupportedOperationException("Custom access managers are no longer supported. Use Repository level security checks instead.");
    }

    /**
     * Set query manager for this hierarchy.
     * @param queryManager
     */
    protected void setQueryManager(QueryManager queryManager) {
        this.queryManager = queryManager;
    }

    public QueryManager getQueryManager() {
        if (null == this.queryManager) {
            WorkspaceAccessUtil util = WorkspaceAccessUtil.getInstance();
            try {
                this.queryManager = util.createQueryManager(this.jcrSession, this);
            } catch (RepositoryException e) {
                reInitialize();
            }
        }
        return this.queryManager;
    }

    private Node getRootNode() {
        if (null == this.rootNode) {
            reInitialize();
        }
        return this.rootNode;
    }

    protected Session getJcrSession() {
        if (null == this.jcrSession) {
            reInitialize();
        }
        return this.jcrSession;
    }

    protected void setJcrSession(Session jcrSession) {
        this.jcrSession = jcrSession;
    }

    /**
     * Creates contentNode of type <b>contentType</b>. contentType must be defined in item type definition of Magnolia
     * as well as JCR implementation.
     * @param path absolute (primary) path to this <code>Node</code>
     * @param label page name
     * @param contentType , JCR node type as configured
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    public Content createContent(String path, String label, String contentType) throws PathNotFoundException,
    RepositoryException, AccessDeniedException {
        Content content = new DefaultContent(this.getRootNode(), this.getNodePath(path, label), contentType, this);
        setMetaData(content.getMetaData());
        AuditLoggingUtil.log( AuditLoggingUtil.ACTION_CREATE, workspaceName, content.getItemType(), content.getHandle());
        return content;
    }

    private String getNodePath(String parent, String label) {
        if (StringUtils.isEmpty(parent) || (parent.equals("/"))) { //$NON-NLS-1$
            return label;
        }
        if (!parent.endsWith("/")) {
            parent = parent + "/";
        }
        return getNodePath(parent + label);
    }

    private String getNodePath(String path) {
        if (path != null && path.startsWith("/")) { //$NON-NLS-1$
            return path.replaceFirst("/", StringUtils.EMPTY); //$NON-NLS-1$
        }
        return path;
    }

    /**
     * Helper method to set page properties, create page calls this method. you could call this method anytime to create
     * working page properties.
     */
    protected void setMetaData(MetaData md) throws RepositoryException, AccessDeniedException {
        md.setCreationDate();
        md.setModificationDate();
        md.setAuthorId(this.userId);
        md.setTitle(StringUtils.EMPTY);
    }

    /**
     * get content object of the requested URI.
     * @param path of the content to be initialized
     * @return Content
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public Content getContent(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (path.equals("/")) { //$NON-NLS-1$
            return this.getRoot();
        }
        try {
            return (new DefaultContent(this.getRootNode(), getNodePath(path), this));
        } catch (ItemNotFoundException e) {
            this.getJcrSession().refresh(true);
            return (new DefaultContent(this.getRootNode(), getNodePath(path), this));
        }
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
                AuditLoggingUtil.log( AuditLoggingUtil.ACTION_CREATE, workspaceName, node.getItemType(), node.getHandle());
            }
            else {
                throw e;
            }
        }
        return node;
    }


    /**
     * get NodeData object of the requested URI.
     * @param path of the atom to be initialized
     * @return NodeData
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public NodeData getNodeData(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        final String nodePath = StringUtils.substringBeforeLast(path, "/");
        final String nodeDataName = StringUtils.substringAfterLast(path, "/");
        return getContent(nodePath).getNodeData(nodeDataName);
    }

    /**
     * returns the first page with a given template name that is found in tree that starts from the page given py the
     * path (including this page).
     * @param path handle of the page from where the search should start
     * @param templateName template name to search for
     * @return first Content hierarchy node that has the specified template name assigned
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     *
     * @deprecated since 4.0 - only used by taglibs - should go/move.
     */
    @Deprecated
    public Content getPage(String path, String templateName) throws PathNotFoundException, RepositoryException,
    AccessDeniedException {
        Content page = getContent(path);
        if (page.getTemplate().equals(templateName)) {
            return page;
        }
        Content pageToBeFound = null;
        try {
            if (page.hasChildren()) {
                Collection<Content> children = page.getChildren(ItemType.CONTENT.getSystemName());
                for (Content child : children) {
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
            log.error("Failed to get - " + path + " : " + e.getMessage(), e);
        }
        return pageToBeFound;
    }

    /**
     * removes specified path, it can be either node or property.
     * @param path to be removed
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @throws AccessDeniedException
     */
    public void delete(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Access.tryPermission(jcrSession, path, Session.ACTION_REMOVE);
        ItemType type = null;
        if (this.isNodeData(path)) {
            this.getNodeData(makeRelative(path)).delete();
        }
        else {
            Node aNode = this.getRootNode().getNode(makeRelative(path));
            if (aNode.hasProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE)) {
                type = new ItemType(aNode.getProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE).getString());
            }
            type = new ItemType(aNode.getProperty(ItemType.JCR_PRIMARY_TYPE).getString());
            aNode.remove();
        }
        AuditLoggingUtil.log( AuditLoggingUtil.ACTION_DELETE, workspaceName, type, path);
    }

    private String makeRelative(String path) {
        return StringUtils.stripStart(path, "/"); //$NON-NLS-1$
    }

    /**
     * @return rootNode of the current working repository-workspace
     */
    public Content getRoot() throws RepositoryException, AccessDeniedException {
        return (new DefaultContent(this.getRootNode(), this));
    }


    /**
     * check is either the node or property exists with the specified path and user has access to it. If at least READ permission is not
     * granted or not running in SystemContext, the method will return false even if the node in question exists.
     * @param path
     */
    public boolean isExist(String path) {
        if (!Access.isGranted(jcrSession, path, Session.ACTION_READ)) {
            return false;
        }
        try {
            this.getJcrSession().refresh(true);
            return this.getJcrSession().itemExists(path);
        }
        catch (RepositoryException re) {
            log.error("Exception caught", re);
            return false;
        }
    }

    public boolean isGranted(String path, long oldPermissions) {
        String permissions = "";
        //TODO: review && convert all the permissions properly
        if ((oldPermissions & Permission.ALL) == Permission.ALL) {
            permissions = Session.ACTION_ADD_NODE + "," + Session.ACTION_READ + "," + Session.ACTION_REMOVE + "," + Session.ACTION_SET_PROPERTY;
        } else if ((oldPermissions & Permission.WRITE) == Permission.WRITE) {
            permissions = Session.ACTION_ADD_NODE;
        }
        else if ((oldPermissions & Permission.READ) == Permission.READ) {
            permissions = Session.ACTION_READ;
        }

        return (Access.isGranted(jcrSession, path, permissions));
    }

    /**
     * checks if the requested resource is an NodeData (Property).
     * @param path of the requested NodeData
     * @return boolean true is the requested content is an NodeData
     */
    public boolean isNodeData(String path) throws AccessDeniedException {
        Access.tryPermission(jcrSession, path, Session.ACTION_READ);
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
        try {
            return new DefaultContent(this.getJcrSession().getNodeByUUID(uuid), this);
        } catch (ItemNotFoundException e) {
            // retry in case session was not updated
            this.getJcrSession().refresh(true);
            return new DefaultContent(this.getJcrSession().getNodeByUUID(uuid), this);
        }
    }

    /**
     * gets currently used workspace for this hierarchy manager.
     */
    public Workspace getWorkspace() {
        if (null == this.workspace) {
            reInitialize();
        }
        return this.workspace;
    }

    /**
     * move content to the specified location.
     * @param source source node path
     * @param destination node where the node has to be moved
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void moveTo(String source, String destination) throws PathNotFoundException, RepositoryException,
    AccessDeniedException {
        Access.tryPermission(jcrSession, source, Session.ACTION_REMOVE);
        Access.tryPermission(jcrSession, destination, Session.ACTION_ADD_NODE);
        this.getWorkspace().move(source, destination);
        AuditLoggingUtil.log( AuditLoggingUtil.ACTION_MOVE, workspaceName, source, destination);
    }

    /**
     * copy content to the specified location.
     * @param source source node path
     * @param destination node where the node has to be copied
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void copyTo(String source, String destination) throws PathNotFoundException, RepositoryException,
    AccessDeniedException {
        Access.tryPermission(jcrSession, source, Session.ACTION_READ);
        Access.tryPermission(jcrSession, destination, Session.ACTION_ADD_NODE);
        this.getWorkspace().copy(source, destination);
        AuditLoggingUtil.log( AuditLoggingUtil.ACTION_COPY, workspaceName, source, destination);
    }

    /**
     * Persists all changes to the repository if validation succeeds.
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
     * Refreshes this session.
     * @param keepChanges
     * @throws RepositoryException
     * @see javax.jcr.Session#refresh(boolean)
     */
    public void refresh(boolean keepChanges) throws RepositoryException {
        this.getJcrSession().refresh(keepChanges);
    }

    public String getName() {
        return this.workspaceName;
    }

}
