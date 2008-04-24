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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;

import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class MockSession implements Session {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MockSession.class);

    public void addLockToken(String lt) {
    }

    public void checkPermission(String absPath, String actions) throws AccessControlException, RepositoryException {
    }

    public void exportDocumentView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse)
        throws PathNotFoundException, SAXException, RepositoryException {
    }

    public void exportDocumentView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse)
        throws IOException, PathNotFoundException, RepositoryException {
    }

    public void exportSystemView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse)
        throws PathNotFoundException, SAXException, RepositoryException {
    }

    public void exportSystemView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse)
        throws IOException, PathNotFoundException, RepositoryException {
    }

    public Object getAttribute(String name) {
        return null;
    }

    public String[] getAttributeNames() {
        return null;
    }

    public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) throws PathNotFoundException,
        ConstraintViolationException, VersionException, LockException, RepositoryException {
        return null;
    }

    public Item getItem(String absPath) throws PathNotFoundException, RepositoryException {
        return null;
    }

    public String[] getLockTokens() {
        return null;
    }

    public String getNamespacePrefix(String uri) throws NamespaceException, RepositoryException {
        return null;
    }

    public String[] getNamespacePrefixes() throws RepositoryException {
        return null;
    }

    public String getNamespaceURI(String prefix) throws NamespaceException, RepositoryException {
        return null;
    }

    public Node getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException {
        return null;
    }

    public Repository getRepository() {
        return null;
    }

    public Node getRootNode() throws RepositoryException {
        return null;
    }

    public String getUserID() {
        return null;
    }

    public ValueFactory getValueFactory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;
    }

    public Workspace getWorkspace() {
        return null;
    }

    public boolean hasPendingChanges() throws RepositoryException {
        return false;
    }

    public Session impersonate(Credentials credentials) throws LoginException, RepositoryException {
        return null;
    }

    public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) throws IOException,
        PathNotFoundException, ItemExistsException, ConstraintViolationException, VersionException,
        InvalidSerializedDataException, LockException, RepositoryException {
    }

    public boolean isLive() {
        return true;
    }

    public boolean itemExists(String absPath) throws RepositoryException {
        return true;
    }

    public void logout() {
    }

    public void move(String srcAbsPath, String destAbsPath) throws ItemExistsException, PathNotFoundException,
        VersionException, ConstraintViolationException, LockException, RepositoryException {
    }

    public void refresh(boolean keepChanges) throws RepositoryException {
    }

    public void removeLockToken(String lt) {
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException,
        InvalidItemStateException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
    }

    public void setNamespacePrefix(String prefix, String uri) throws NamespaceException, RepositoryException {
    }
}
