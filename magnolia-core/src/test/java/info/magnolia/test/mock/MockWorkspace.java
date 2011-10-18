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

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.test.mock.jcr.MockQueryManager;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;


/**
 * @version $Id$
 * @deprecated since 4.5 - use {@link info.magnolia.test.mock.jcr.MockWorkspace} instead.
 */

public class MockWorkspace implements Workspace {

    private static Logger log = LoggerFactory.getLogger(MockWorkspace.class);

    private String name;

    private final Session session;

    private ObservationManager observationManager;

    private final HierarchyManager hm;

    public MockWorkspace(String name, Session session) {
        this(name, session, null);
    }

    public MockWorkspace(String name, Session session, HierarchyManager hm) {
        super();
        this.name = name;
        this.session = session;
        this.observationManager = new MockObservationManager();
        this.hm = hm;
    }

    @Override
    public void clone(String srcWorkspace, String srcAbsPath, String destAbsPath, boolean removeExisting)
    throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException,
    PathNotFoundException, ItemExistsException, LockException, RepositoryException {
    }

    @Override
    public void copy(String srcAbsPath, String destAbsPath) throws ConstraintViolationException, VersionException,
    AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
    }

    @Override
    public void copy(String srcWorkspace, String srcAbsPath, String destAbsPath) throws NoSuchWorkspaceException,
    ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException,
    ItemExistsException, LockException, RepositoryException {
    }

    @Override
    public String[] getAccessibleWorkspaceNames() throws RepositoryException {
        return null;
    }

    @Override
    public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) throws PathNotFoundException,
    ConstraintViolationException, VersionException, LockException, AccessDeniedException, RepositoryException {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
        return null;
    }

    @Override
    public NodeTypeManager getNodeTypeManager() throws RepositoryException {
        return null;
    }

    @Override
    public QueryManager getQueryManager() throws RepositoryException {
        return new MockQueryManager(this.session);
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) throws IOException,
    PathNotFoundException, ItemExistsException, ConstraintViolationException, InvalidSerializedDataException,
    LockException, AccessDeniedException, RepositoryException {
    }

    @Override
    public void move(String srcAbsPath, String destAbsPath) throws ConstraintViolationException, VersionException,
    AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
    }

    @Override
    public void restore(Version[] versions, boolean removeExisting) throws ItemExistsException,
    UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException,
    RepositoryException {
    }


    @Override
    public ObservationManager getObservationManager() {
        return this.observationManager;
    }


    public void setObservationManager(ObservationManager observationManager) {
        this.observationManager = observationManager;
    }

    @Override
    public LockManager getLockManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public VersionManager getVersionManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void createWorkspace(String name) throws AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
    }

    @Override
    public void createWorkspace(String name, String srcWorkspace) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {
    }

    @Override
    public void deleteWorkspace(String name) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {
    }
}
