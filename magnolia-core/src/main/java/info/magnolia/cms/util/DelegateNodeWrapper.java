/**
 * This file Copyright (c) 2011 Magnolia International
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

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidLifecycleTransitionException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.MergeException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.ActivityViolationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;

/**
 * Wrapper for JCR node.
 * @author had
 * @version $Id: $
 */
public abstract class DelegateNodeWrapper implements Node {

    private Node wrapped;

    public DelegateNodeWrapper(Node wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void addMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        getWrappedNode().addMixin(mixinName);
    }

    @Override
    public Node addNode(String relPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException,
    RepositoryException {
        return getWrappedNode().addNode(relPath);
    }

    @Override
    public Node addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException,
    VersionException, ConstraintViolationException, RepositoryException {
        return getWrappedNode().addNode(relPath, primaryNodeTypeName);
    }

    @Override
    public boolean canAddMixin(String mixinName) throws NoSuchNodeTypeException, RepositoryException {
        return getWrappedNode().canAddMixin(mixinName);
    }

    @Override
    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        getWrappedNode().cancelMerge(version);
    }

    @Override
    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        return getWrappedNode().checkin();
    }

    @Override
    public void checkout() throws UnsupportedRepositoryOperationException, LockException, ActivityViolationException, RepositoryException {
        getWrappedNode().checkout();
    }

    @Override
    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {

    }

    @Override
    public void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException,
    RepositoryException {
        getWrappedNode().followLifecycleTransition(transition);
    }

    @Override
    public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return getWrappedNode().getAllowedLifecycleTransistions();
    }

    @Override
    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return getWrappedNode().getBaseVersion();
    }

    @Override
    public String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException,
    RepositoryException {
        return getWrappedNode().getCorrespondingNodePath(workspaceName);
    }

    @Override
    public NodeDefinition getDefinition() throws RepositoryException {
        return getWrappedNode().getDefinition();
    }

    @Override
    public String getIdentifier() throws RepositoryException {
        return getWrappedNode().getIdentifier();
    }

    @Override
    public int getIndex() throws RepositoryException {
        return getWrappedNode().getIndex();
    }

    @Override
    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
        return getWrappedNode().getLock();
    }

    @Override
    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return getWrappedNode().getMixinNodeTypes();
    }

    @Override
    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        return getWrappedNode().getNode(relPath);
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return getWrappedNode().getNodes();
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return getWrappedNode().getNodes(namePattern);
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return getWrappedNode().getNodes(nameGlobs);
    }

    @Override
    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        return getWrappedNode().getPrimaryItem();
    }

    @Override
    public NodeType getPrimaryNodeType() throws RepositoryException {
        return getWrappedNode().getPrimaryNodeType();
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        return getWrappedNode().getProperties();
    }

    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        return getWrappedNode().getProperties(namePattern);
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        return getWrappedNode().getProperties(nameGlobs);
    }

    @Override
    public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException {
        return getWrappedNode().getProperty(relPath);
    }

    @Override
    public PropertyIterator getReferences() throws RepositoryException {
        return getWrappedNode().getReferences();
    }

    @Override
    public PropertyIterator getReferences(String name) throws RepositoryException {
        return getWrappedNode().getReferences(name);
    }

    @Override
    public NodeIterator getSharedSet() throws RepositoryException {
        return getWrappedNode().getSharedSet();
    }

    @Override
    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        return getWrappedNode().getUUID();
    }

    @Override
    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return getWrappedNode().getVersionHistory();
    }

    @Override
    public PropertyIterator getWeakReferences() throws RepositoryException {
        return getWrappedNode().getWeakReferences();
    }

    @Override
    public PropertyIterator getWeakReferences(String name) throws RepositoryException {
        return getWrappedNode().getWeakReferences(name);
    }

    @Override
    public boolean hasNode(String relPath) throws RepositoryException {
        return getWrappedNode().hasNode(relPath);
    }

    @Override
    public boolean hasNodes() throws RepositoryException {
        return getWrappedNode().hasNodes();
    }

    @Override
    public boolean hasProperties() throws RepositoryException {
        return getWrappedNode().hasProperties();
    }

    @Override
    public boolean hasProperty(String relPath) throws RepositoryException {
        return getWrappedNode().hasProperty(relPath);
    }

    @Override
    public boolean holdsLock() throws RepositoryException {
        return getWrappedNode().holdsLock();
    }

    @Override
    public boolean isCheckedOut() throws RepositoryException {
        return getWrappedNode().isCheckedOut();
    }

    @Override
    public boolean isLocked() throws RepositoryException {
        return getWrappedNode().isLocked();
    }

    @Override
    public boolean isNodeType(String nodeTypeName) throws RepositoryException {
        return getWrappedNode().isNodeType(nodeTypeName);
    }

    @Override
    public Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException,
    InvalidItemStateException, RepositoryException {
        return getWrappedNode().lock(isDeep, isSessionScoped);
    }

    @Override
    public NodeIterator merge(String srcWorkspace, boolean bestEffort) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException,
    InvalidItemStateException, RepositoryException {
        return getWrappedNode().merge(srcWorkspace, bestEffort);
    }

    @Override
    public void orderBefore(String srcChildRelPath, String destChildRelPath) throws UnsupportedRepositoryOperationException, VersionException,
    ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
        getWrappedNode().orderBefore(srcChildRelPath, destChildRelPath);
    }

    @Override
    public void removeMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException,
    RepositoryException {
        getWrappedNode().removeMixin(mixinName);
    }

    @Override
    public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        getWrappedNode().removeShare();
    }

    @Override
    public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        getWrappedNode().removeSharedSet();
    }

    @Override
    public void restore(String versionName, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException,
    LockException, InvalidItemStateException, RepositoryException {
        getWrappedNode().restore(versionName, removeExisting);
    }

    @Override
    public void restore(Version version, boolean removeExisting) throws VersionException, ItemExistsException, InvalidItemStateException,
    UnsupportedRepositoryOperationException, LockException, RepositoryException {
        getWrappedNode().restore(version, removeExisting);
    }

    @Override
    public void restore(Version version, String relPath, boolean removeExisting) throws PathNotFoundException, ItemExistsException, VersionException,
    ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        getWrappedNode().restore(version, relPath, removeExisting);
    }

    @Override
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException, ItemExistsException,
    UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        getWrappedNode().restoreByLabel(versionLabel, removeExisting);
    }

    @Override
    public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException,
    RepositoryException {
        getWrappedNode().setPrimaryType(nodeTypeName);
    }

    @Override
    public Property setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        return getWrappedNode().setProperty(name, value);
    }

    @Override
    public Property setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        return getWrappedNode().setProperty(name, values);
    }

    @Override
    public Property setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        return getWrappedNode().setProperty(name, values);
    }

    @Override
    public Property setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        return getWrappedNode().setProperty(name, value);
    }

    @Override
    public Property setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        return getWrappedNode().setProperty(name, value);
    }

    @Override
    public Property setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        return getWrappedNode().setProperty(name, value);
    }

    @Override
    public Property setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        return getWrappedNode().setProperty(name, value);
    }

    @Override
    public Property setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        return getWrappedNode().setProperty(name, value);
    }

    @Override
    public Property setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        return getWrappedNode().setProperty(name, value);
    }

    @Override
    public Property setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        return getWrappedNode().setProperty(name, value);
    }

    @Override
    public Property setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        return getWrappedNode().setProperty(name, value);
    }

    @Override
    public Property setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        return getWrappedNode().setProperty(name, value);
    }

    @Override
    public Property setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
    RepositoryException {
        return getWrappedNode().setProperty(name, value, type);
    }

    @Override
    public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException,
    ConstraintViolationException, RepositoryException {
        return null;
    }

    @Override
    public Property setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException,
    ConstraintViolationException, RepositoryException {
        return getWrappedNode().setProperty(name, values, type);
    }

    @Override
    public Property setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException,
    ConstraintViolationException, RepositoryException {
        return getWrappedNode().setProperty(name, value, type);
    }

    @Override
    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        getWrappedNode().unlock();
    }

    @Override
    public void update(String srcWorkspace) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException,
    RepositoryException {
        getWrappedNode().update(srcWorkspace);
    }

    @Override
    public void accept(ItemVisitor visitor) throws RepositoryException {
        getWrappedNode().accept(visitor);
    }

    @Override
    public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return getWrappedNode().getAncestor(depth);
    }

    @Override
    public int getDepth() throws RepositoryException {
        return getWrappedNode().getDepth();
    }

    @Override
    public String getName() throws RepositoryException {
        return getWrappedNode().getName();
    }

    @Override
    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return getWrappedNode().getParent();
    }

    @Override
    public String getPath() throws RepositoryException {
        return getWrappedNode().getPath();
    }

    @Override
    public Session getSession() throws RepositoryException {
        return getWrappedNode().getSession();
    }

    @Override
    public boolean isModified() {
        return getWrappedNode().isModified();
    }

    @Override
    public boolean isNew() {
        return getWrappedNode().isNew();
    }

    @Override
    public boolean isNode() {
        return getWrappedNode().isNode();
    }

    @Override
    public boolean isSame(Item otherItem) throws RepositoryException {
        return getWrappedNode().isSame(otherItem);
    }

    @Override
    public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {
        getWrappedNode().refresh(keepChanges);
    }

    @Override
    public void remove() throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {
        getWrappedNode().remove();
    }

    @Override
    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException,
    ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        getWrappedNode().save();
    }

    public Node getWrappedNode() {
        return this.wrapped;
    }


    public Node deepUnwrap(Class wrapper) {
        if (this.getClass().equals(wrapper)) {
            return getWrappedNode();
        }
        if (getWrappedNode() instanceof DelegateNodeWrapper) {
            // rewrap
            this.wrapped = ((DelegateNodeWrapper) getWrappedNode()).deepUnwrap(wrapper);
            return this;
        }
        // oh well, nothing to unwrap
        return this;
    }

}
