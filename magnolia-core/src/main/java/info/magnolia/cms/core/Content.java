/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import info.magnolia.cms.core.version.ContentVersion;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.util.Rule;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;


/**
 * Represents a piece of content (node) which has nodedatas (properties) containing the values and
 * which can have sub contents. This is is very similar to the JCR {@link Node} interface.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface Content extends Cloneable {

    /**
     * Gets the Content node of the current node with the specified name.
     * @param name of the node acting as <code>Content</code>
     * @return Content
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights
     * to complete the operation
     */
    Content getContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Creates a Content node under the current node with the specified name. The default node type
     * {@link ItemType#CONTENT} will be use as the contents primary type.
     * @param name of the node to be created as <code>Content</code>
     * @return newly created <code>Content</code>
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights
     * to complete the operation
     */
    Content createContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Creates a Content node under the current node with the specified name.
     * @param name of the node to be created as <code>Content</code>
     * @param contentType JCR node type as configured
     * @return newly created <code>Content</code>
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights
     * to complete the operation
     */
    Content createContent(String name, String contentType) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Creates a Content node under the current node with the specified name.
     * @param name of the node to be created as <code>Content</code>
     * @param contentType ItemType
     * @return newly created <code>Content</code>
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights
     * to complete the operation
     */
    Content createContent(String name, ItemType contentType) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Returns the template name which is assigned to this content.
     */
    String getTemplate();

    /**
     * @return String, title
     */
    String getTitle();

    /**
     * Returns the meta data of the current node.
     * @return MetaData meta information of the content <code>Node</code>
     */
    MetaData getMetaData();

    /**
     * Returns a {@link NodeData} object. If the node data does not exist (respectively if it has no
     * value) an empty representation is returned whose {@link NodeData#isExist()} will return
     * false.
     * @return NodeData requested <code>NodeData</code> object
     */
    NodeData getNodeData(String name);

    /**
     * get node name.
     * @return String name of the current <code>Node</code>
     */
    String getName();

    /**
     * Creates a node data of type STRING with an empty String as default value.
     * @deprecated since 4.3, as JCR only supports set or remove operations for properties we
     * recommend to use {@link #setNodeData(String, Object)} instead.
     */
    NodeData createNodeData(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Creates a node data of type with an default value set. If the no default value can be set
     * (for BINARY, REFERENCE type) the returned node data will be empty and per definition not yet
     * exist.
     * <ul>
     * <li> STRING: empty string
     * <li> BOOLEAN: false
     * <li> DATE: now
     * <li> LONG/DOUBLE: 0
     * </ul>
     * @deprecated since 4.3, as JCR only supports set or remove operations for properties we
     * recommend to use {@link #setNodeData(String, Object)} instead.
     */
    NodeData createNodeData(String name, int type) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Creates a node data setting the value.
     * @throws AccessDeniedException if the current session does not have sufficient access rights
     * to complete the operation
     * @deprecated since 4.3, as JCR only supports set or remove operations for properties we
     * recommend to use {@link #setNodeData(String, Value)} instead.
     */
    NodeData createNodeData(String name, Value value) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Create a multi value node data.
     * @throws AccessDeniedException if the current session does not have sufficient access rights
     * to complete the operation
     * @deprecated since 4.3, as JCR only supports set or remove operations for properties we
     * recommend to use {@link #setNodeData(String, Value[])} instead.
     */
    NodeData createNodeData(String name, Value[] value) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Creates a property and set its value immediately, according to the type of the passed
     * instance, hiding the complexity of using JCR's ValueFactory and providing a sensible default
     * behavior.
     * @deprecated since 4.3, as JCR only supports set or remove operations for properties we
     * recommend to use {@link #setNodeData(String, Object)} instead.
     */
    NodeData createNodeData(String name, Object obj) throws RepositoryException;

    /**
     * Sets the node data. If the node data does not yet exist the node data is created. Setting
     * null is not allowed
     */
    NodeData setNodeData(String name, Value value) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Sets the node data. If the node data does not yet exist the node data is created. Setting
     * null is not allowed.
     */
    NodeData setNodeData(String name, Value[] value) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Sets the node data. If the node data does not yet exist the node data is created. Setting
     * null is not allowed.
     */
    NodeData setNodeData(String name, String value) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Sets the node data. If the node data does not yet exist the node data is created. Setting
     * null is not allowed.
     */
    NodeData setNodeData(String name, long value) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Sets the node data. If the node data does not yet exist the node data is created. Setting
     * null is not allowed.
     */
    NodeData setNodeData(String name, InputStream value) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Sets the node data. If the node data does not yet exist the node data is created. Setting
     * null will remove the node data.
     */
    NodeData setNodeData(String name, double value) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Sets the node data. If the node data does not yet exist the node data is created. Setting
     * null will remove the node data.
     */
    NodeData setNodeData(String name, boolean value) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Sets the node data. If the node data does not yet exist the node data is created. Setting
     * null will remove the node data.
     */
    NodeData setNodeData(String name, Calendar value) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Sets the node data. If the node data does not yet exist the node data is created. Setting
     * null will remove the node data.
     */
    NodeData setNodeData(String name, Content value) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Sets the node data. If the node data does not yet exist the node data is created. Setting
     * null will remove the node data.<br>
     * The type of the node data will be determined by the type of the passed value
     */
    NodeData setNodeData(String name, Object value) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Delete NodeData with the specified name.
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     */
    void deleteNodeData(String name) throws PathNotFoundException, RepositoryException;

    /**
     * You could call this method anytime to update working page properties - Modification date &
     * Author ID.
     * @throws AccessDeniedException if the current session does not have sufficient access rights
     * to complete the operation
     * @throws RepositoryException if an error occurs
     */
    void updateMetaData() throws RepositoryException, AccessDeniedException;

    /**
     * Gets a Collection containing all child nodes of the same NodeType as "this" object.
     * @return Collection of content objects
     */
    Collection<Content> getChildren();

    /**
     * Get a collection containing child nodes which satisfies the given filter.
     * @param filter
     * @return Collection of content objects or empty collection when no children are found.
     */
    Collection<Content> getChildren(ContentFilter filter);

    /**
     * Get a collection containing child nodes which satisfies the given filter. The returned
     * collection is ordered according to the passed in criteria.
     * @param filter filter for the child nodes
     * @param orderCriteria ordering for the selected child nodes; if <tt>null</tt> than no
     * particular order of the child nodes
     * @return Collection of content objects or empty collection when no children are found.
     */
    Collection<Content> getChildren(ContentFilter filter, Comparator<Content> orderCriteria);

    /**
     * Get collection of specified content type and its subtypes.
     * @param contentType JCR node type as configured
     * @return Collection of content nodes
     */
    Collection<Content> getChildren(String contentType);

    /**
     * Get collection of specified content type.
     * @param contentType ItemType
     * @return Collection of content nodes
     */
    Collection<Content> getChildren(ItemType contentType);

    /**
     * Get collection of specified content type.
     * @param contentType JCR node type as configured
     * @param namePattern
     * @return Collection of content nodes
     */
    Collection<Content> getChildren(String contentType, String namePattern);

    /**
     * Returns the first child with the given name, any node type.
     * @param namePattern child node name
     * @return first found node with the given name or <code>null</code> if not found
     * @deprecated since 4.3, either use {@link #getContent(String)} or {@link #getChildren(String)}
     */
    Content getChildByName(String namePattern);

    /**
     * Gets all properties bind in NodeData object excluding JCR system properties.
     */
    Collection<NodeData> getNodeDataCollection();

    /**
     * Gets all node datas matching the given pattern. If no pattern is given (null),
     * gets all node datas.
     */
    Collection<NodeData> getNodeDataCollection(String namePattern);

    /**
     * @return Boolean, if sub node(s) exists
     */
    boolean hasChildren();

    /**
     * @param contentType JCR node type as configured
     * @return Boolean, if sub <code>collectionType</code> exists
     */
    boolean hasChildren(String contentType);

    /**
     * @param name
     * @throws RepositoryException if an error occurs
     */
    boolean hasContent(String name) throws RepositoryException;

    /**
     * @param name
     * @throws RepositoryException if an error occurs
     */
    boolean hasNodeData(String name) throws RepositoryException;

    /**
     * get a handle representing path relative to the content repository.
     * @return String representing path (handle) of the content
     */
    String getHandle();

    /**
     * get parent content object.
     * @return Content representing parent node
     * @throws PathNotFoundException
     * @throws AccessDeniedException if the current session does not have sufficient access rights
     * to complete the operation
     * @throws RepositoryException if an error occurs
     */
    Content getParent() throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * get absolute parent object starting from the root node.
     * @param level level at which the requested node exist, relative to the ROOT node
     * @return Content representing parent node
     * @throws AccessDeniedException if the current session does not have sufficient access rights
     * to complete the operation
     * @throws RepositoryException if an error occurs
     */
    Content getAncestor(int level) throws PathNotFoundException, RepositoryException, AccessDeniedException;

    /**
     * Convenience method for taglib.
     * @return Content representing node on level 0
     * @throws RepositoryException if an error occurs
     */
    Collection<Content> getAncestors() throws PathNotFoundException, RepositoryException;

    /**
     * get node level from the ROOT node.
     * @return level at which current node exist, relative to the ROOT node
     * @throws PathNotFoundException
     * @throws RepositoryException if an error occurs
     */
    int getLevel() throws PathNotFoundException, RepositoryException;

    /**
     * move current node to the specified location above the named <code>beforename</code>.
     * @param srcName where current node has to be moved
     * @param beforeName name of the node before the current node has to be placed
     * @throws RepositoryException if an error occurs
     */
    void orderBefore(String srcName, String beforeName) throws RepositoryException;

    /**
     * This method returns the index of this node within the ordered set of its same-name sibling
     * nodes. This index is the one used to address same-name siblings using the square-bracket
     * notation, e.g., /a[3]/b[4]. Note that the index always starts at 1 (not 0), for compatibility
     * with XPath. As a result, for nodes that do not have same-name-siblings, this method will
     * always return 1.
     * @return The index of this node within the ordered set of its same-name sibling nodes.
     * @throws RepositoryException if an error occurs
     */
    int getIndex() throws RepositoryException;

    /**
     * utility method to get Node object used to create current content object.
     * @return Node
     */
    Node getJCRNode();

    /**
     * evaluate primary node type of the associated Node of this object.
     * @param type
     */
    boolean isNodeType(String type);

    /**
     * returns primary node type definition of the associated Node of this object.
     * @throws RepositoryException if an error occurs
     */
    NodeType getNodeType() throws RepositoryException;

    /**
     * returns primary node type name of the associated Node of this object.
     * @throws RepositoryException if an error occurs
     */
    String getNodeTypeName() throws RepositoryException;

    /**
     * Get the magnolia ItemType.
     * @return the type
     * @throws RepositoryException
     */
    ItemType getItemType() throws RepositoryException;

    /**
     * Restores this node to the state defined by the version with the specified versionName.
     * @param versionName
     * @param removeExisting
     * @throws VersionException if the specified <code>versionName</code> does not exist in this
     * node's version history
     * @throws RepositoryException if an error occurs
     */
    void restore(String versionName, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * Restores this node to the state defined by the specified version.
     * @param version
     * @param removeExisting
     * @throws VersionException if the specified <code>version</code> is not part of this node's
     * version history
     * @throws RepositoryException if an error occurs
     */
    void restore(Version version, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * Restores the specified version to relPath, relative to this node.
     * @param version
     * @param relPath
     * @param removeExisting
     * @throws VersionException if the specified <code>version</code> is not part of this node's
     * version history
     * @throws RepositoryException if an error occurs
     */
    void restore(Version version, String relPath, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * Restores this node to the state recorded in the version specified by versionLabel.
     * @param versionLabel
     * @param removeExisting
     * @throws VersionException if the specified <code>versionLabel</code> does not exist in this
     * node's version history
     * @throws RepositoryException if an error occurs
     */
    void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * add version leaving the node checked out.
     * @throws UnsupportedRepositoryOperationException
     * @throws RepositoryException if an error occurs
     */
    Version addVersion() throws UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * add version leaving the node checked out.
     * @param rule to be used to collect content
     * @throws UnsupportedRepositoryOperationException
     * @throws RepositoryException if an error occurs
     * @see info.magnolia.cms.util.Rule
     */
    Version addVersion(Rule rule) throws UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * Returns <code>true</code> if this <code>Item</code> has been saved but has subsequently been
     * modified through the current session and therefore the state of this item as recorded in the
     * session differs from the state of this item as saved. Within a transaction,
     * <code>isModified</code> on an <code>Item</code> may return <code>false</code> (because the
     * <code>Item</code> has been saved since the modification) even if the modification in question
     * is not in persistent storage (because the transaction has not yet been committed).
     * <p/>
     * Note that in level 1 (that is, read-only) implementations, this method will always return
     * <code>false</code>.
     * @return <code>true</code> if this item is modified; <code>false</code> otherwise.
     */
    boolean isModified();

    /**
     * @return version history
     * @throws RepositoryException if an error occurs
     */
    VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * @return Version iterator retreived from version history
     * @throws RepositoryException if an error occurs
     */
    VersionIterator getAllVersions() throws UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * get the current base version of this node.
     * @return base ContentVersion
     * @throws UnsupportedRepositoryOperationException
     * @throws RepositoryException
     */
    ContentVersion getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * get content view over the jcr version object.
     * @param version
     * @return version object wrapped in ContentVersion
     * @see info.magnolia.cms.core.version.ContentVersion
     */
    ContentVersion getVersionedContent(Version version) throws RepositoryException;

    /**
     * get content view over the jcr version object.
     * @param versionName
     * @return version object wrapped in ContentVersion
     * @see info.magnolia.cms.core.version.ContentVersion
     */
    ContentVersion getVersionedContent(String versionName) throws RepositoryException;

    /**
     * removes all versions of this node and associated version graph.
     * @throws AccessDeniedException If not allowed to do write operations on this node
     * @throws RepositoryException if unable to remove versions from version store
     */
    void removeVersionHistory() throws AccessDeniedException, RepositoryException;

    /**
     * Persists all changes to the repository if validation succeeds.
     * @throws RepositoryException if an error occurs
     */
    void save() throws RepositoryException;

    /**
     * checks for the allowed access rights.
     * @param permissions as defined in javax.jcr.Permission
     * @return true is the current user has specified access on this node.
     */
    boolean isGranted(long permissions);

    /**
     * Remove this path.
     * @throws RepositoryException if an error occurs
     */
    void delete() throws RepositoryException;

    /**
     * Remove specified path.
     * @throws RepositoryException if an error occurs
     */
    void delete(String path) throws RepositoryException;

    /**
     * checks if the requested resource is an NodeData (Property).
     * @param path of the requested NodeData
     * @return boolean true is the requested content is an NodeData
     * @throws AccessDeniedException
     * @throws AccessDeniedException if the current session does not have sufficient access rights
     * to complete the operation
     * @throws RepositoryException if an error occurs
     */
    boolean isNodeData(String path) throws AccessDeniedException, RepositoryException;

    /**
     * If keepChanges is false, this method discards all pending changes recorded in this session.
     * @throws RepositoryException if an error occurs
     * @see javax.jcr.Node#refresh(boolean)
     */
    void refresh(boolean keepChanges) throws RepositoryException;

    /**
     * UUID of the node referenced by this object.
     * @return uuid
     */
    String getUUID();

    /**
     * add specified mixin type if allowed.
     * @param type mixin type to be added
     * @throws RepositoryException if an error occurs
     */
    void addMixin(String type) throws RepositoryException;

    /**
     * Removes the specified mixin node type from this node. Also removes mixinName from this node's
     * jcr:mixinTypes property. <strong>The mixin node type removal takes effect on save</strong>.
     * @param type , mixin type to be removed
     * @throws RepositoryException if an error occurs
     */
    void removeMixin(String type) throws RepositoryException;

    /**
     * Returns an array of NodeType objects representing the mixin node types assigned to this node.
     * This includes only those mixin types explicitly assigned to this node, and therefore listed
     * in the property jcr:mixinTypes. It does not include mixin types inherited through the addition
     * of supertypes to the primary type hierarchy.
     * @return an array of mixin NodeType objects.
     * @throws RepositoryException if an error occurs
     */
    NodeType[] getMixinNodeTypes() throws RepositoryException;

    /**
     * places a lock on this object.
     * @param isDeep if true this lock will apply to this node and all its descendants; if false, it
     * applies only to this node.
     * @param isSessionScoped if true, this lock expires with the current session; if false it
     * expires when explicitly or automatically unlocked for some other reason.
     * @return A Lock object containing a lock token.
     * @throws LockException if this node is already locked or <code>isDeep</code> is true and a
     * descendant node of this node already holds a lock.
     * @throws RepositoryException if an error occurs
     * @see javax.jcr.Node#lock(boolean,boolean)
     */
    Lock lock(boolean isDeep, boolean isSessionScoped) throws LockException, RepositoryException;

    /**
     * places a lock on this object.
     * @param isDeep if true this lock will apply to this node and all its descendants; if false, it
     * applies only to this node.
     * @param isSessionScoped if true, this lock expires with the current session; if false it
     * expires when explicitly or automatically unlocked for some other reason.
     * @param yieldFor number of milliseconds for which this method will try to get a lock
     * @return A Lock object containing a lock token.
     * @throws LockException if this node is already locked or <code>isDeep</code> is true and a
     * descendant node of this node already holds a lock.
     * @throws RepositoryException if an error occurs
     * @see javax.jcr.Node#lock(boolean,boolean)
     */
    Lock lock(boolean isDeep, boolean isSessionScoped, long yieldFor) throws LockException, RepositoryException;

    /**
     * Returns the Lock object that applies to this node. This may be either a lock on this node
     * itself or a deep lock on a node above this node.
     * @throws LockException If no lock applies to this node, a LockException is thrown.
     * @throws RepositoryException if an error occurs
     */
    Lock getLock() throws LockException, RepositoryException;

    /**
     * Removes the lock on this node. Also removes the properties jcr:lockOwner and jcr:lockIsDeep
     * from this node. These changes are persisted automatically; <b>there is no need to call
     * save</b>.
     * @throws LockException if either does not currently hold a lock, or holds a lock for which
     * this Session does not have the correct lock token
     * @throws RepositoryException if an error occurs
     */
    void unlock() throws LockException, RepositoryException;

    /**
     * Returns true if this node holds a lock; otherwise returns false. To hold a lock means that
     * this node has actually had a lock placed on it specifically, as opposed to just having a lock
     * apply to it due to a deep lock held by a node above.
     * @return a boolean
     * @throws RepositoryException if an error occurs
     */
    boolean holdsLock() throws RepositoryException;

    /**
     * Returns true if this node is locked either as a result of a lock held by this node or by a
     * deep lock on a node above this node; otherwise returns false.
     * @return a boolean
     * @throws RepositoryException if an error occurs
     */
    boolean isLocked() throws RepositoryException;

    /**
     * get workspace to which this node attached to.
     * @throws RepositoryException if unable to get this node session
     */
    Workspace getWorkspace() throws RepositoryException;

    /**
     * @return the underlying AccessManager
     * @deprecated since 4.0 - use getHierarchyManager instead
     */
    AccessManager getAccessManager();

    HierarchyManager getHierarchyManager();

    /**
     * checks if this node has a sub node with name MetaData.
     * @return true if MetaData exists
     */
    boolean hasMetaData();

    /**
     * Implement this interface to be used as node filter by getChildren().
     */
    public interface ContentFilter {

        /**
         * Test if this content should be included in a resultant collection.
         * @param content
         * @return if true this will be a part of collection
         */
        public boolean accept(Content content);

    }
}
