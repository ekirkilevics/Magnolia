/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.test.mock;

import info.magnolia.cms.core.Content;
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
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MockJCRNode implements Node {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MockJCRNode.class);

    private final MockContent mockContent;

    public MockJCRNode(MockContent mockContent) {
        this.mockContent = mockContent;
    }

    @Override
    public void addMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public Node addNode(String relPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public Node addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        return mockContent.createContent(relPath, primaryNodeTypeName).getJCRNode();
    }

    @Override
    public boolean canAddMixin(String mixinName) throws NoSuchNodeTypeException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public NodeDefinition getDefinition() throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public int getIndex() throws RepositoryException {
        return mockContent.getIndex();
    }

    @Override
    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return mockContent.getMixinNodeTypes();
    }

    @Override
    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        return mockContent.getContent(relPath).getJCRNode();
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return new MockJCRNodeIterator(mockContent.getChildren(new Content.ContentFilter() {
            @Override
            public boolean accept(Content content) {
                return true;
            }
        }));
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return new MockJCRNodeIterator(mockContent.getChildren(namePattern));
    }

    @Override
    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public NodeType getPrimaryNodeType() throws RepositoryException {
        return mockContent.getNodeType();
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        return new MockJCRPropertyIterator(mockContent.getNodeDataCollection());
    }

    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        return new MockJCRPropertyIterator(mockContent.getNodeDataCollection(namePattern));
    }

    @Override
    public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException {
        return mockContent.getNodeData(relPath).getJCRProperty();
    }

    @Override
    public PropertyIterator getReferences() throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        return mockContent.getUUID();
    }

    @Override
    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public boolean hasNode(String relPath) throws RepositoryException {
        return mockContent.hasContent(relPath);
    }

    @Override
    public boolean hasNodes() throws RepositoryException {
        return mockContent.hasChildren();
    }

    @Override
    public boolean hasProperties() throws RepositoryException {
        return mockContent.getNodeDataCollection().size() > 0;
    }

    @Override
    public boolean hasProperty(String relPath) throws RepositoryException {
        return mockContent.hasNodeData(relPath);
    }

    @Override
    public boolean holdsLock() throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public boolean isCheckedOut() throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public boolean isLocked() throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public boolean isNodeType(String nodeTypeName) throws RepositoryException {
        return mockContent.isNodeType(nodeTypeName);
    }

    @Override
    public Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public NodeIterator merge(String srcWorkspace, boolean bestEffort) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public void orderBefore(String srcChildRelPath, String destChildRelPath) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
        mockContent.orderBefore(srcChildRelPath, destChildRelPath);
    }

    @Override
    public void removeMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public void restore(String versionName, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public void restore(Version version, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public void restore(Version version, String relPath, boolean removeExisting) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public Property setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, value).getJCRProperty();
    }

    @Override
    public Property setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, values).getJCRProperty();    }

    @Override
    public Property setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public Property setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, value).getJCRProperty();
    }

    @Override
    public Property setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, value).getJCRProperty();
    }

    @Override
    public Property setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, value).getJCRProperty();
    }

    @Override
    public Property setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, value).getJCRProperty();
    }

    @Override
    public Property setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, value).getJCRProperty();
    }

    @Override
    public Property setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, value).getJCRProperty();
    }

    @Override
    public Property setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public Property setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return setProperty(name, value);
    }

    @Override
    public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return setProperty(name, values);
    }

    @Override
    public Property setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public Property setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public void update(String srcWorkspaceName) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public void accept(ItemVisitor visitor) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return mockContent.getAncestor(depth).getJCRNode();
    }

    @Override
    public int getDepth() throws RepositoryException {
        return mockContent.getLevel();
    }

    @Override
    public String getName() throws RepositoryException {
        return mockContent.getName();
    }

    @Override
    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return mockContent.getParent().getJCRNode();
    }

    @Override
    public String getPath() throws RepositoryException {
        return mockContent.getHandle();
    }

    @Override
    public Session getSession() throws RepositoryException {
        return mockContent.getHierarchyManager().getWorkspace().getSession();
    }

    @Override
    public boolean isModified() {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public boolean isNew() {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public boolean isNode() {
        return true;
    }

    @Override
    public boolean isSame(Item otherItem) throws RepositoryException {
        return false;
    }

    @Override
    public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {
        mockContent.refresh(keepChanges);
    }

    @Override
    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        mockContent.delete();
    }

    @Override
    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        mockContent.save();
    }

    @Override
    public Property setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public Property setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public String getIdentifier() throws RepositoryException {
        return this.mockContent.getUUID();
    }

    @Override
    public PropertyIterator getReferences(String name) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public PropertyIterator getWeakReferences() throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public PropertyIterator getWeakReferences(String name) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
    }

    @Override
    public NodeIterator getSharedSet() throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }

    @Override
    public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    @Override
    public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    @Override
    public void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {
    }

    @Override
    public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a mock class.");
    }
}
