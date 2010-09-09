/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.tree.container;

/**
 * @author daniellipp
 * @version $Id$
 *
 */
import java.io.InputStream;
import java.io.Serializable;
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
 * <p>
 * Lazy loading, serializable JCR Node.
 * </p>
 *
 * @author Lance Weber, Alfheim Studios LLC <lance@alfheimstudios.com>
 * @version 1.0.0
 *
 */
public class NodeProxy implements Serializable, Node {
    private static final long serialVersionUID = -4591189218475935752L;

    private transient Node node;

    private String nodepath;

    private JcrSessionProvider provider;

    public NodeProxy(Node node, JcrSessionProvider provider)
            throws RepositoryException {
        this.node = node;
        nodepath = node.getPath();
        this.provider = provider;
    }

    public void accept(ItemVisitor visitor) throws RepositoryException {
        getNode().accept(visitor);
    }

    public void addMixin(String mixinName) throws NoSuchNodeTypeException,
        VersionException, ConstraintViolationException, LockException,
        RepositoryException {

    getNode().addMixin(mixinName);
}

  public Node addNode(String relPath) throws ItemExistsException,
        PathNotFoundException, VersionException,
        ConstraintViolationException, LockException, RepositoryException {
    return getNode().addNode(relPath);
}
  public Node addNode(String relPath, String primaryNodeTypeName)
        throws ItemExistsException, PathNotFoundException,
        NoSuchNodeTypeException, LockException, VersionException,
        ConstraintViolationException, RepositoryException {
    return getNode().addNode(relPath, primaryNodeTypeName);
}
  public boolean canAddMixin(String mixinName)
        throws NoSuchNodeTypeException, RepositoryException {
    return getNode().canAddMixin(mixinName);
}

    public void cancelMerge(Version version) throws VersionException,
            InvalidItemStateException, UnsupportedRepositoryOperationException,
            RepositoryException {
        getNode().cancelMerge(version);
    }

    public Version checkin() throws VersionException,
            UnsupportedRepositoryOperationException, InvalidItemStateException,
            LockException, RepositoryException {
        return getNode().checkin();
    }

    public void checkout() throws UnsupportedRepositoryOperationException,
            LockException, ActivityViolationException, RepositoryException {
        getNode().checkout();
    }

    @SuppressWarnings("deprecation")
    public void doneMerge(Version version) throws VersionException,
            InvalidItemStateException, UnsupportedRepositoryOperationException,
            RepositoryException {
        getNode().doneMerge(version);
    }

    public void followLifecycleTransition(String transition)
            throws UnsupportedRepositoryOperationException,
            InvalidLifecycleTransitionException, RepositoryException {
        getNode().followLifecycleTransition(transition);
    }

    public String[] getAllowedLifecycleTransistions()
            throws UnsupportedRepositoryOperationException, RepositoryException {
        return getNode().getAllowedLifecycleTransistions();
    }

    public Item getAncestor(int depth) throws ItemNotFoundException,
            AccessDeniedException, RepositoryException {
        return getNode().getAncestor(depth);
    }

    @SuppressWarnings("deprecation")
    public Version getBaseVersion()
            throws UnsupportedRepositoryOperationException, RepositoryException {
        return getNode().getBaseVersion();
    }

    public String getCorrespondingNodePath(String workspaceName)
            throws ItemNotFoundException, NoSuchWorkspaceException,
            AccessDeniedException, RepositoryException {
        return getNode().getCorrespondingNodePath(workspaceName);
    }

    public NodeDefinition getDefinition() throws RepositoryException {
        return getNode().getDefinition();
    }

    public int getDepth() throws RepositoryException {
        return getNode().getDepth();
    }

    public String getIdentifier() throws RepositoryException {
        return getNode().getIdentifier();
    }

    public int getIndex() throws RepositoryException {
        return getNode().getIndex();
    }

    @SuppressWarnings("deprecation")
    public Lock getLock() throws UnsupportedRepositoryOperationException,
            LockException, AccessDeniedException, RepositoryException {
        return getNode().getLock();
    }

    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return getNode().getMixinNodeTypes();
    }

    public String getName() throws RepositoryException {
        return getNode().getName();
    }

    protected Node getNode() throws RepositoryException {
        if (node == null) {
           //TODO: FIX node = provider.getSession().getNode(nodepath);
        }
        return node;
    }

    public Node getNode(String relPath) throws PathNotFoundException,
            RepositoryException {
        return getNode().getNode(relPath);
    }

    public NodeIterator getNodes() throws RepositoryException {
        return getNode().getNodes();
    }

    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return getNode().getNodes(namePattern);
    }

    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return getNode().getNodes(nameGlobs);
    }

    public Node getParent() throws ItemNotFoundException,
            AccessDeniedException, RepositoryException {
        return getNode().getParent();
    }

    public String getPath() throws RepositoryException {
        return getNode().getPath();
    }

    public Item getPrimaryItem() throws ItemNotFoundException,
            RepositoryException {
        return getNode().getPrimaryItem();
    }

    public NodeType getPrimaryNodeType() throws RepositoryException {
        return getNode().getPrimaryNodeType();
    }

    public PropertyIterator getProperties() throws RepositoryException {
        return getNode().getProperties();
    }

    public PropertyIterator getProperties(String namePattern)
            throws RepositoryException {
        return getNode().getProperties(namePattern);
    }

    public PropertyIterator getProperties(String[] nameGlobs)
            throws RepositoryException {
        return getNode().getProperties(nameGlobs);
    }

    public Property getProperty(String relPath) throws PathNotFoundException,
            RepositoryException {
        return getNode().getProperty(relPath);
    }

    public PropertyIterator getReferences() throws RepositoryException {
        return getNode().getReferences();
    }

    public PropertyIterator getReferences(String name)
            throws RepositoryException {
        return getNode().getReferences(name);
    }

    public Session getSession() throws RepositoryException {
        return getNode().getSession();
    }

    public NodeIterator getSharedSet() throws RepositoryException {
        return getNode().getSharedSet();
    }

    @SuppressWarnings("deprecation")
    public String getUUID() throws UnsupportedRepositoryOperationException,
            RepositoryException {
        return getNode().getUUID();
    }

    @SuppressWarnings("deprecation")
    public VersionHistory getVersionHistory()
            throws UnsupportedRepositoryOperationException, RepositoryException {
        return getNode().getVersionHistory();
    }

    public PropertyIterator getWeakReferences() throws RepositoryException {
        return getNode().getWeakReferences();
    }

    public PropertyIterator getWeakReferences(String name)
            throws RepositoryException {
        return getNode().getWeakReferences(name);
    }

    public boolean hasNode(String relPath) throws RepositoryException {
        return getNode().hasNode(relPath);
    }

    public boolean hasNodes() throws RepositoryException {
        return getNode().hasNodes();
    }

    public boolean hasProperties() throws RepositoryException {
        return getNode().hasProperties();
    }

    public boolean hasProperty(String relPath) throws RepositoryException {
        return getNode().hasProperty(relPath);
    }

    @SuppressWarnings("deprecation")
    public boolean holdsLock() throws RepositoryException {
        return getNode().holdsLock();
    }

    public boolean isCheckedOut() throws RepositoryException {
        return getNode().isCheckedOut();
    }

    public boolean isLocked() throws RepositoryException {
        return getNode().isLocked();
    }

    public boolean isModified() {
        try {
            return getNode().isModified();
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isNew() {
        try {
            return getNode().isNew();
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isNode() {
        try {
            return getNode().isNode();
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isNodeType(String nodeTypeName) throws RepositoryException {
        return getNode().isNodeType(nodeTypeName);
    }

    public boolean isSame(Item otherItem) throws RepositoryException {
        return getNode().isSame(otherItem);
    }

    @SuppressWarnings("deprecation")
    public Lock lock(boolean isDeep, boolean isSessionScoped)
            throws UnsupportedRepositoryOperationException, LockException,
            AccessDeniedException, InvalidItemStateException,
            RepositoryException {
        return getNode().lock(isDeep, isSessionScoped);
    }

    @SuppressWarnings("deprecation")
    public NodeIterator merge(String srcWorkspace, boolean bestEffort)
            throws NoSuchWorkspaceException, AccessDeniedException,
            MergeException, LockException, InvalidItemStateException,
            RepositoryException {
        return getNode().merge(srcWorkspace, bestEffort);
    }

    public void orderBefore(String srcChildRelPath, String destChildRelPath)
            throws UnsupportedRepositoryOperationException, VersionException,
            ConstraintViolationException, ItemNotFoundException, LockException,
            RepositoryException {
        getNode().orderBefore(srcChildRelPath, destChildRelPath);
    }

    public void refresh(boolean keepChanges) throws InvalidItemStateException,
            RepositoryException {
        getNode().refresh(keepChanges);
    }

    public void remove() throws VersionException, LockException,
            ConstraintViolationException, AccessDeniedException,
            RepositoryException {
        getNode().remove();
    }

    public void removeMixin(String mixinName) throws NoSuchNodeTypeException,
            VersionException, ConstraintViolationException, LockException,
            RepositoryException {
        getNode().removeMixin(mixinName);
    }

    // JCR 2.0 Methods - commented for now...
      public void removeShare() throws VersionException, LockException,
              ConstraintViolationException, RepositoryException {
          getNode().removeShare();
      }


    public void removeSharedSet() throws VersionException, LockException,
              ConstraintViolationException, RepositoryException {
          getNode().removeSharedSet();
      }

    @SuppressWarnings("deprecation")
    public void restore(String versionName, boolean removeExisting)
            throws VersionException, ItemExistsException,
            UnsupportedRepositoryOperationException, LockException,
            InvalidItemStateException, RepositoryException {
        getNode().restore(versionName, removeExisting);
    }


    @SuppressWarnings("deprecation")
    public void restore(Version version, boolean removeExisting)
            throws VersionException, ItemExistsException,
            InvalidItemStateException, UnsupportedRepositoryOperationException,
            LockException, RepositoryException {
        getNode().restore(version, removeExisting);
    }

    @SuppressWarnings("deprecation")
    public void restore(Version version, String relPath, boolean removeExisting)
            throws PathNotFoundException, ItemExistsException,
            VersionException, ConstraintViolationException,
            UnsupportedRepositoryOperationException, LockException,
            InvalidItemStateException, RepositoryException {
        getNode().restore(version, relPath, removeExisting);
    }

    @SuppressWarnings("deprecation")
    public void restoreByLabel(String versionLabel, boolean removeExisting)
            throws VersionException, ItemExistsException,
            UnsupportedRepositoryOperationException, LockException,
            InvalidItemStateException, RepositoryException {
        getNode().restoreByLabel(versionLabel, removeExisting);
    }

    @SuppressWarnings("deprecation")
    public void save() throws AccessDeniedException, ItemExistsException,
            ConstraintViolationException, InvalidItemStateException,
            ReferentialIntegrityException, VersionException, LockException,
            NoSuchNodeTypeException, RepositoryException {
        getNode().save();
    }

    public void setPrimaryType(String nodeTypeName)
            throws NoSuchNodeTypeException, VersionException,
            ConstraintViolationException, LockException, RepositoryException {
        getNode().setPrimaryType(nodeTypeName);
    }

    public Property setProperty(String name, BigDecimal value)
              throws ValueFormatException, VersionException, LockException,
              ConstraintViolationException, RepositoryException {
          return getNode().setProperty(name, value);
      }

    public Property setProperty(String name, Binary value)
              throws ValueFormatException, VersionException, LockException,
              ConstraintViolationException, RepositoryException {
          return getNode().setProperty(name, value);
      }

    public Property setProperty(String name, boolean value)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return getNode().setProperty(name, value);
    }

    public Property setProperty(String name, Calendar value)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return getNode().setProperty(name, value);
    }

    public Property setProperty(String name, double value)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return getNode().setProperty(name, value);
    }

    @SuppressWarnings("deprecation")
    public Property setProperty(String name, InputStream value)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return getNode().setProperty(name, value);
    }

    public Property setProperty(String name, long value)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return getNode().setProperty(name, value);
    }

    public Property setProperty(String name, Node value)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return getNode().setProperty(name, value);
    }

    public Property setProperty(String name, String value)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return getNode().setProperty(name, value);
    }

    public Property setProperty(String name, String value, int type)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return getNode().setProperty(name, value, type);
    }

    public Property setProperty(String name, String[] values)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return getNode().setProperty(name, values);
    }

    public Property setProperty(String name, String[] values, int type)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return getNode().setProperty(name, values, type);
    }

    public Property setProperty(String name, Value value)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return getNode().setProperty(name, value);
    }

    public Property setProperty(String name, Value value, int type)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return getNode().setProperty(name, value, type);
    }

    public Property setProperty(String name, Value[] values)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return getNode().setProperty(name, values);
    }

    public Property setProperty(String name, Value[] values, int type)
            throws ValueFormatException, VersionException, LockException,
            ConstraintViolationException, RepositoryException {
        return getNode().setProperty(name, values, type);
    }

    @SuppressWarnings("deprecation")
    public void unlock() throws UnsupportedRepositoryOperationException,
            LockException, AccessDeniedException, InvalidItemStateException,
            RepositoryException {
        getNode().unlock();
    }

    public void update(String srcWorkspace) throws NoSuchWorkspaceException,
            AccessDeniedException, LockException, InvalidItemStateException,
            RepositoryException {
        getNode().update(srcWorkspace);
    }
}
