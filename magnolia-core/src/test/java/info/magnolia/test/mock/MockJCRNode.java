/**
 * This file Copyright (c) 2008-2010 Magnolia International
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

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
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

    private MockContent mockContent;

    public MockJCRNode(MockContent mockContent) {
        this.mockContent = mockContent;
    }

    public void addMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public Node addNode(String relPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public Node addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        return mockContent.createContent(relPath, primaryNodeTypeName).getJCRNode();
    }

    public boolean canAddMixin(String mixinName) throws NoSuchNodeTypeException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public NodeDefinition getDefinition() throws RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public int getIndex() throws RepositoryException {
        return mockContent.getIndex();
    }

    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return mockContent.getMixinNodeTypes();
    }

    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        return mockContent.getContent(relPath).getJCRNode();
    }

    public NodeIterator getNodes() throws RepositoryException {
        return new MockJCRNodeIterator(mockContent.getChildren());
    }

    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return new MockJCRNodeIterator(mockContent.getChildren(namePattern));
    }

    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public NodeType getPrimaryNodeType() throws RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public PropertyIterator getProperties() throws RepositoryException {
        return new MockJCRPropertyIterator(mockContent.getNodeDataCollection());
    }

    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        return new MockJCRPropertyIterator(mockContent.getNodeDataCollection(namePattern));
    }

    public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException {
        return mockContent.getNodeData(relPath).getJCRProperty();
    }

    public PropertyIterator getReferences() throws RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        return mockContent.getUUID();
    }

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public boolean hasNode(String relPath) throws RepositoryException {
        return mockContent.hasContent(relPath);
    }

    public boolean hasNodes() throws RepositoryException {
        return mockContent.hasChildren();
    }

    public boolean hasProperties() throws RepositoryException {
        return mockContent.getNodeDataCollection().size() > 0;
    }

    public boolean hasProperty(String relPath) throws RepositoryException {
        return mockContent.hasNodeData(relPath);
    }

    public boolean holdsLock() throws RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public boolean isCheckedOut() throws RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");    
    }

    public boolean isLocked() throws RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public boolean isNodeType(String nodeTypeName) throws RepositoryException {
        return mockContent.isNodeType(nodeTypeName);
    }

    public Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class."); 
    }

    public NodeIterator merge(String srcWorkspace, boolean bestEffort) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public void orderBefore(String srcChildRelPath, String destChildRelPath) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
        mockContent.orderBefore(srcChildRelPath, destChildRelPath);
    }

    public void removeMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public void restore(String versionName, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public void restore(Version version, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public void restore(Version version, String relPath, boolean removeExisting) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public Property setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, value).getJCRProperty();
    }

    public Property setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, values).getJCRProperty();    }

    public Property setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public Property setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, value).getJCRProperty();
    }

    public Property setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, value).getJCRProperty();
    }

    public Property setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, value).getJCRProperty();
    }

    public Property setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, value).getJCRProperty();
    }

    public Property setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, value).getJCRProperty();
    }

    public Property setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return mockContent.setNodeData(name, value).getJCRProperty();
    }

    public Property setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public Property setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return setProperty(name, value);
    }

    public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return setProperty(name, values);
    }

    public Property setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public Property setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public void update(String srcWorkspaceName) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public void accept(ItemVisitor visitor) throws RepositoryException {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return mockContent.getAncestor(depth).getJCRNode();
    }

    public int getDepth() throws RepositoryException {
        return mockContent.getLevel();
    }

    public String getName() throws RepositoryException {
        return mockContent.getName();
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return mockContent.getParent().getJCRNode();
    }

    public String getPath() throws RepositoryException {
        return mockContent.getHandle();
    }

    public Session getSession() throws RepositoryException {
        return mockContent.getHierarchyManager().getWorkspace().getSession();
    }

    public boolean isModified() {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public boolean isNew() {
        throw new UnsupportedOperationException("Not implmeneted. This is a mock class.");
    }

    public boolean isNode() {
        return true;
    }

    public boolean isSame(Item otherItem) throws RepositoryException {
        return false;
    }

    public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {
        mockContent.refresh(keepChanges);
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        mockContent.delete();
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        mockContent.save();
    }
}
