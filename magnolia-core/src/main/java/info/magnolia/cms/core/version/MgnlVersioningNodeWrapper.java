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
package info.magnolia.cms.core.version;

import info.magnolia.cms.core.Access;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Path;
import info.magnolia.jcr.wrapper.ChildWrappingNodeWrapper;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.MgnlContext.Op;
import info.magnolia.logging.AuditLoggingUtil;

import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper providing support for Magnolia specific versioning ops (by copy).
 *
 * @version $Id$
 */
public class MgnlVersioningNodeWrapper extends ChildWrappingNodeWrapper {

    private static final Logger log = LoggerFactory.getLogger(MgnlVersioningNodeWrapper.class);

    public MgnlVersioningNodeWrapper(Node wrapped) {
        super(wrapped);
    }


    @Override
    public void restore(String versionName, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException,
    LockException, InvalidItemStateException, RepositoryException {
        VersionManager versionMan = VersionManager.getInstance();
        Node raw = deepUnwrap(getClass());
        Version version = versionMan.getVersion(raw, versionName);
        versionMan.restore(raw, version, removeExisting);
    }

    @Override
    public void restore(Version version, boolean removeExisting) throws VersionException, ItemExistsException, InvalidItemStateException,
    UnsupportedRepositoryOperationException, LockException, RepositoryException {
        VersionManager versionMan = VersionManager.getInstance();
        versionMan.restore(deepUnwrap(getClass()), version, removeExisting);
    }

    @Override
    public void restore(Version version, String relPath, boolean removeExisting) throws PathNotFoundException, ItemExistsException, VersionException,
    ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Magnolia doesn't support restore into specified path at the moment");
    }

    @Override
    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException, ItemExistsException,
    UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException("Magnolia doesn't support locating versions by label at the moment");
    }

    /**
     * Remove version history while removing the content.
     */
    @Override
    public void remove() throws RepositoryException {
        wrapped = getWrappedNode();
        final String nodePath = Path.getAbsolutePath(wrapped.getPath());
        log.debug("removing {} from {}", wrapped.getPath(), getSession().getWorkspace().getName());
        Access.tryPermission(getSession(), Path.getAbsolutePath(getPath()), Session.ACTION_REMOVE);
        NodeType nodeType = this.getPrimaryNodeType();
        String workspaceName = getSession().getWorkspace().getName();
        if (!workspaceName.equals("mgnlVersion")) {
            MgnlContext.doInSystemContext(new Op<Void, RepositoryException>() {
                @Override
                public Void exec() throws RepositoryException {
                    try {
                        final String uuid = wrapped.getUUID();
                        HierarchyManager hm = MgnlContext.getHierarchyManager("mgnlVersion");
                        Node versionedNode = hm.getContentByUUID(uuid).getJCRNode();
                        log.debug("Located versioned node {}({})", uuid, nodePath);

                        VersionHistory history = versionedNode.getVersionHistory();

                        log.debug("Removing versioned node {}({})", uuid, nodePath);
                        versionedNode.remove();
                        hm.save();

                        VersionIterator iter = history.getAllVersions();
                        // skip root version. It can't be deleted manually, but will be cleaned up automatically after removing all other versions (see JCR-134)
                        iter.nextVersion();
                        while (iter.hasNext()) {
                            Version version = iter.nextVersion();
                            log.debug("removing version {}", version.getName());
                            history.removeVersion(version.getName());
                        }
                        // at this point history should be deleted automatically (at least on JR)
                    } catch (ItemNotFoundException e) {
                        // doesn't exist in version store, ignore
                    } catch (UnsupportedRepositoryOperationException e) {
                        // not versionable or not referenceable ... either way ignore
                    }
                    return null;
                }
            });
        }
        wrapped.remove();
        AuditLoggingUtil.log(AuditLoggingUtil.ACTION_DELETE, workspaceName, nodeType, nodePath);
    }
}
