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
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;


/**
 * @author pbracher
 * @version $Id$
 *
 */
public class MockWorkspace implements Workspace {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MockWorkspace.class);

    private String name;

    private Session session;

    private ObservationManager observationManager;

    public MockWorkspace(String name, Session session) {
        super();
        this.name = name;
        this.session = session;
        this.observationManager = new MockObservationManager();
    }

    public void clone(String srcWorkspace, String srcAbsPath, String destAbsPath, boolean removeExisting)
        throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException,
        PathNotFoundException, ItemExistsException, LockException, RepositoryException {
    }

    public void copy(String srcAbsPath, String destAbsPath) throws ConstraintViolationException, VersionException,
        AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
    }

    public void copy(String srcWorkspace, String srcAbsPath, String destAbsPath) throws NoSuchWorkspaceException,
        ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException,
        ItemExistsException, LockException, RepositoryException {
    }

    public String[] getAccessibleWorkspaceNames() throws RepositoryException {
        return null;
    }

    public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) throws PathNotFoundException,
        ConstraintViolationException, VersionException, LockException, AccessDeniedException, RepositoryException {
        return null;
    }

    public String getName() {
        return name;
    }

    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
        return null;
    }

    public NodeTypeManager getNodeTypeManager() throws RepositoryException {
        return null;
    }

    public QueryManager getQueryManager() throws RepositoryException {
        return null;
    }

    public Session getSession() {
        return session;
    }

    public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) throws IOException,
        PathNotFoundException, ItemExistsException, ConstraintViolationException, InvalidSerializedDataException,
        LockException, AccessDeniedException, RepositoryException {
    }

    public void move(String srcAbsPath, String destAbsPath) throws ConstraintViolationException, VersionException,
        AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
    }

    public void restore(Version[] versions, boolean removeExisting) throws ItemExistsException,
        UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException,
        RepositoryException {
    }


    public ObservationManager getObservationManager() {
        return this.observationManager;
    }


    public void setObservationManager(ObservationManager observationManager) {
        this.observationManager = observationManager;
    }
}
