/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
import info.magnolia.cms.core.search.QueryManagerImpl;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.PermissionUtil;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.jcr.wrapper.JCRPropertiesFilteringNodeWrapper;
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
 * @version $Id$
 *
 * @deprecated since 4.5. Use Session and its methods instead.
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

    private Session jcrSession;

    public DefaultHierarchyManager(Session jcrSession) {
        this.jcrSession = jcrSession;
    }

    public DefaultHierarchyManager(String userId, Session jcrSession, AccessManager ignoredAccessManager) throws RepositoryException {
        this(userId, jcrSession);
    }

    public DefaultHierarchyManager(String ignoredUserId, Session jcrSession) throws RepositoryException {
        this(jcrSession);
    }

    /**
     * Only used in tests.
     */
    public DefaultHierarchyManager(Session jcrSession, String ignored) {
        this.jcrSession = jcrSession;
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
    @Override
    public AccessManager getAccessManager() {
        // throw an ex because letting this fall through would be too unsecure for code relying on the old behavior
        throw new UnsupportedOperationException("Custom access managers are no longer supported. Use Repository level security checks instead.");
    }

    /**
     * Set query manager for this hierarchy.
     * @param queryManager
     */
    protected void setQueryManager(QueryManager queryManager) {
        throw new UnsupportedOperationException("This Operation is no longer available.");
    }

    @Override
    public QueryManager getQueryManager() {
        try {
            return new QueryManagerImpl(getJcrSession().getWorkspace().getQueryManager(), this);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    private Node getRootNode() throws RepositoryException {
        return jcrSession.getRootNode();
    }

    private String getWorkspaceName() {
        return jcrSession.getWorkspace().getName();
    }

    protected Session getJcrSession() {
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
    @Override
    public Content createContent(String path, String label, String contentType) throws PathNotFoundException,
    RepositoryException, AccessDeniedException {
        Content content = wrapAsContent(this.getRootNode(), this.getNodePath(path, label), contentType);
        setMetaData(content.getMetaData());
        AuditLoggingUtil.log( AuditLoggingUtil.ACTION_CREATE, getWorkspaceName(), content.getItemType(), content.getHandle());
        return content;
    }

    protected Content wrapAsContent(Node rootNode, String path, String contentType) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return new DefaultContent(rootNode, path, contentType);
    }

    private String getNodePath(String parent, String label) {
        if (StringUtils.isEmpty(parent) || (parent.equals("/"))) {
            return label;
        }
        if (!parent.endsWith("/")) {
            parent = parent + "/";
        }
        return getNodePath(parent + label);
    }

    private String getNodePath(String path) {
        if (path != null && path.startsWith("/")) {
            return path.replaceFirst("/", StringUtils.EMPTY);
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
        md.setAuthorId(this.jcrSession.getUserID());
    }

    /**
     * get content object of the requested URI.
     * @param path of the content to be initialized
     * @return Content
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    @Override
    public Content getContent(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (path.equals("/")) {
            return this.getRoot();
        }
        try {
            return wrapAsContent(this.getRootNode(), getNodePath(path));
        } catch (ItemNotFoundException e) {
            this.getJcrSession().refresh(true);
            return wrapAsContent(this.getRootNode(), getNodePath(path));
        }
    }

    public Content wrapAsContent(Node rootNode, String path) throws AccessDeniedException, PathNotFoundException, RepositoryException {
        return new DefaultContent(rootNode, path);
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
    @Override
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
                AuditLoggingUtil.log( AuditLoggingUtil.ACTION_CREATE, getWorkspaceName(), node.getItemType(), node.getHandle());
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
    @Override
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
    @Override
    public void delete(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        ItemType type = null;
        if (this.isNodeData(path)) {
            this.getNodeData(makeRelative(path)).delete();
        }
        else {
            Node aNode = this.getRootNode().getNode(makeRelative(path));
            aNode = NodeUtil.deepUnwrap(aNode, JCRPropertiesFilteringNodeWrapper.class);
            if (aNode.hasProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE)) {
                type = new ItemType(aNode.getProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE).getString());
            }
            type = new ItemType(aNode.getProperty(ItemType.JCR_PRIMARY_TYPE).getString());
            aNode.remove();
        }
        AuditLoggingUtil.log( AuditLoggingUtil.ACTION_DELETE, getWorkspaceName(), type, path);
    }

    private String makeRelative(String path) {
        return StringUtils.stripStart(path, "/");
    }

    /**
     * @return rootNode of the current working repository-workspace
     */
    @Override
    public Content getRoot() throws RepositoryException, AccessDeniedException {
        return (new DefaultContent(this.getRootNode()));
    }


    /**
     * check is either the node or property exists with the specified path and user has access to it. If at least READ permission is not
     * granted or not running in SystemContext, the method will return false even if the node in question exists.
     * @param path
     */
    @Override
    public boolean isExist(String path) {
        if (!PermissionUtil.isGranted(jcrSession, path, Session.ACTION_READ)) {
            return false;
        }
        try {
            this.getJcrSession().refresh(true);
            return this.getJcrSession().itemExists(path);
        } catch (RepositoryException re) {
            // do not create hard dependency on JR API. The path validity check is not exposed via JCR API
            if (re.getCause().getClass().getName().equals("org.apache.jackrabbit.spi.commons.conversion.MalformedPathException")) {
                // do not log invalid path by default
                log.debug("{} is not valid jcr path.", path);
            } else {
                log.error("Exception caught", re);
            }
            return false;
        }
    }

    @Override
    public boolean isGranted(String path, long oldPermissions) {
        return PermissionUtil.isGranted(jcrSession, path, oldPermissions);
    }

    /**
     * checks if the requested resource is an NodeData (Property).
     * @param path of the requested NodeData
     * @return boolean true is the requested content is an NodeData
     */
    @Override
    public boolean isNodeData(String path) throws AccessDeniedException {
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
    @Override
    public Content getContentByUUID(String uuid) throws ItemNotFoundException, RepositoryException,
    AccessDeniedException {
        try {
            return wrapAsContent(this.getJcrSession().getNodeByIdentifier(uuid));
        } catch (ItemNotFoundException e) {
            // retry in case session was not updated
            this.getJcrSession().refresh(true);
            return wrapAsContent(this.getJcrSession().getNodeByIdentifier(uuid));
        }
    }

    protected Content wrapAsContent(Node node) {
        return new DefaultContent(node);
    }

    /**
     * gets currently used workspace for this hierarchy manager.
     */
    @Override
    public Workspace getWorkspace() {
        return getJcrSession().getWorkspace();
    }

    /**
     * move content to the specified location.
     * @param source source node path
     * @param destination node where the node has to be moved
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    @Override
    public void moveTo(String source, String destination) throws PathNotFoundException, RepositoryException,
    AccessDeniedException {
        this.getWorkspace().move(source, destination);
        AuditLoggingUtil.log( AuditLoggingUtil.ACTION_MOVE, getWorkspaceName(), source, destination);
    }

    /**
     * copy content to the specified location.
     * @param source source node path
     * @param destination node where the node has to be copied
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    @Override
    public void copyTo(String source, String destination) throws PathNotFoundException, RepositoryException,
    AccessDeniedException {
        this.getWorkspace().copy(source, destination);
        AuditLoggingUtil.log( AuditLoggingUtil.ACTION_COPY, getWorkspaceName(), source, destination);
    }

    /**
     * Persists all changes to the repository if validation succeeds.
     * @throws RepositoryException
     */
    @Override
    public void save() throws RepositoryException {
        try {
            this.getJcrSession().save();
        }
        catch (RepositoryException re) {
            // TODO dlipp - this might end up with logging the error twice. I'd prefer to either log or rethrow - in that context the rethrow.
            log.error(re.getMessage(), re);
            throw re;
        }
    }

    /**
     * Returns true if the session has pending (unsaved) changes.
     */
    @Override
    public boolean hasPendingChanges() throws RepositoryException {
        return this.getJcrSession().hasPendingChanges();
    }

    /**
     * Refreshes this session.
     * @param keepChanges
     * @throws RepositoryException
     * @see javax.jcr.Session#refresh(boolean)
     */
    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
        this.getJcrSession().refresh(keepChanges);
    }

    @Override
    public String getName() {
        return getWorkspaceName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((jcrSession == null) ? 0 : jcrSession.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultHierarchyManager other = (DefaultHierarchyManager) obj;
        if (jcrSession == null) {
            if (other.jcrSession != null) {
                return false;
            }
        } else if (!SessionUtil.hasSameUnderlyingSession(jcrSession, other.jcrSession)) {
            return false;
        }
        return true;
    }
}
