/**
 * This file Copyright (c) 2008 Magnolia International
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


/**
 * @author philipp
 * @version $Id$
 *
 */
public class MockJCRNode implements Node {
    MockContent mockContent;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MockJCRNode.class);

    public MockJCRNode(MockContent mockContent) {
        this.mockContent = mockContent;
    }

    public void addMixin(String mixinName) throws NoSuchNodeTypeException, VersionException,
        ConstraintViolationException, LockException, RepositoryException {
    }

    public Node addNode(String relPath) throws ItemExistsException, PathNotFoundException, VersionException,
        ConstraintViolationException, LockException, RepositoryException {
        return null;
    }

    public Node addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException,
        NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public boolean canAddMixin(String mixinName) throws NoSuchNodeTypeException, RepositoryException {
        return false;
    }

    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException,
        UnsupportedRepositoryOperationException, RepositoryException {
    }

    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException,
        InvalidItemStateException, LockException, RepositoryException {
        return null;
    }

    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
    }

    public void doneMerge(Version version) throws VersionException, InvalidItemStateException,
        UnsupportedRepositoryOperationException, RepositoryException {
    }

    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;
    }

    public String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException,
        NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        return null;
    }

    public NodeDefinition getDefinition() throws RepositoryException {
        return null;
    }

    public int getIndex() throws RepositoryException {
        return 0;
    }

    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException,
        RepositoryException {
        return null;
    }

    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return null;
    }

    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        return null;
    }

    public NodeIterator getNodes() throws RepositoryException {
        return null;
    }

    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return null;
    }

    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        return null;
    }

    public NodeType getPrimaryNodeType() throws RepositoryException {
        return null;
    }

    public PropertyIterator getProperties() throws RepositoryException {
        return null;
    }

    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        return null;
    }

    public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException {
        return null;
    }

    public PropertyIterator getReferences() throws RepositoryException {
        return null;
    }

    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;
    }

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;
    }

    public boolean hasNode(String relPath) throws RepositoryException {
        return false;
    }

    public boolean hasNodes() throws RepositoryException {
        return false;
    }

    public boolean hasProperties() throws RepositoryException {
        return false;
    }

    public boolean hasProperty(String relPath) throws RepositoryException {
        return false;
    }

    public boolean holdsLock() throws RepositoryException {
        return false;
    }

    public boolean isCheckedOut() throws RepositoryException {
        return false;
    }

    public boolean isLocked() throws RepositoryException {
        return false;
    }

    public boolean isNodeType(String nodeTypeName) throws RepositoryException {
        return false;
    }

    public Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException,
        LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        return null;
    }

    public NodeIterator merge(String srcWorkspace, boolean bestEffort) throws NoSuchWorkspaceException,
        AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        return null;
    }

    public void orderBefore(String srcChildRelPath, String destChildRelPath)
        throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException,
        ItemNotFoundException, LockException, RepositoryException {
    }

    public void removeMixin(String mixinName) throws NoSuchNodeTypeException, VersionException,
        ConstraintViolationException, LockException, RepositoryException {
    }

    public void restore(String versionName, boolean removeExisting) throws VersionException, ItemExistsException,
        UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
    }

    public void restore(Version version, boolean removeExisting) throws VersionException, ItemExistsException,
        UnsupportedRepositoryOperationException, LockException, RepositoryException {
    }

    public void restore(Version version, String relPath, boolean removeExisting) throws PathNotFoundException,
        ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException,
        LockException, InvalidItemStateException, RepositoryException {
    }

    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException,
        ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException,
        RepositoryException {
    }

    public Property setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException,
        ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String name, Value[] values) throws ValueFormatException, VersionException,
        LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String name, String[] values) throws ValueFormatException, VersionException,
        LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String name, String value) throws ValueFormatException, VersionException,
        LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String name, InputStream value) throws ValueFormatException, VersionException,
        LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String name, boolean value) throws ValueFormatException, VersionException,
        LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String name, double value) throws ValueFormatException, VersionException,
        LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String name, long value) throws ValueFormatException, VersionException, LockException,
        ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String name, Calendar value) throws ValueFormatException, VersionException,
        LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException,
        ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String name, Value value, int type) throws ValueFormatException, VersionException,
        LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException,
        LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException,
        LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public Property setProperty(String name, String value, int type) throws ValueFormatException, VersionException,
        LockException, ConstraintViolationException, RepositoryException {
        return null;
    }

    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException,
        InvalidItemStateException, RepositoryException {
    }

    public void update(String srcWorkspaceName) throws NoSuchWorkspaceException, AccessDeniedException, LockException,
        InvalidItemStateException, RepositoryException {
    }

    public void accept(ItemVisitor visitor) throws RepositoryException {
    }

    public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return null;
    }

    public int getDepth() throws RepositoryException {
        return 0;
    }

    public String getName() throws RepositoryException {
        return null;
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return null;
    }

    public String getPath() throws RepositoryException {
        return null;
    }

    public Session getSession() throws RepositoryException {
        // TODO get that session from the hierarchy manager
        return new MockSession();
    }

    public boolean isModified() {
        return false;
    }

    public boolean isNew() {
        return false;
    }

    public boolean isNode() {
        return false;
    }

    public boolean isSame(Item otherItem) throws RepositoryException {
        return false;
    }

    public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException {
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException,
        InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException,
        NoSuchNodeTypeException, RepositoryException {
    }
}
