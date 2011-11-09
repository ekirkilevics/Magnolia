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
package info.magnolia.test.mock.jcr;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import javax.jcr.Credentials;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.AbstractSession;
import org.xml.sax.ContentHandler;

/**
 * @version $Id$
 */
public class MockSession extends AbstractSession {

    private MockNode rootNode = null;
    private ValueFactory valueFactory = null;
    private boolean live = true;

    private Workspace workspace;

    public MockSession(MockWorkspace workspace) {
        setWorkspace(workspace);

        rootNode = new MockNode(this);
    }

    public MockSession(String name) {
        this(new MockWorkspace(name));
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
        if (workspace instanceof MockWorkspace) {
            ((MockWorkspace) workspace).setSession(this);
        }
    }

    @Override
    public void addLockToken(String lt) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void checkPermission(String absPath, String actions) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void exportDocumentView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void exportDocumentView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void exportSystemView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void exportSystemView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public AccessControlManager getAccessControlManager() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Object getAttribute(String name) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public String[] getAttributeNames() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Item getItem(final String absPath) throws RepositoryException {

        if (!absPath.startsWith("/"))
            throw new PathNotFoundException("Path must be absolute " + absPath);

        Node current = getRootNode();
        String remainingPath = absPath.substring(1);
        while (remainingPath.length() > 0) {
            int i = remainingPath.indexOf('/');
            String nextSegment;
            if (i == -1) {
                nextSegment = remainingPath;
                remainingPath = "";
            } else {
                nextSegment = remainingPath.substring(0, i);
                remainingPath = remainingPath.substring(i + 1);
            }

            if (current.hasNode(nextSegment))
                current = current.getNode(nextSegment);
            else if (current.hasProperty(nextSegment) && remainingPath.length() == 0)
                return current.getProperty(nextSegment);
            else
                throw new PathNotFoundException(absPath + " This is a fake class.");
        }
        return current;
    }

    @Override
    public String[] getLockTokens() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Node getNode(String absPath) throws PathNotFoundException, RepositoryException {
        if ("/".equals(absPath)) {
            return rootNode;
        }
        return rootNode.getNode(StringUtils.removeStart(absPath, "/"));
    }

    @Override
    public Node getNodeByIdentifier(String id) throws RepositoryException {
        Queue<Node> queue = new LinkedList<Node>();
        queue.add(rootNode);
        while (!queue.isEmpty()) {
            Node node = queue.remove();
            // null safe equals check
            if (StringUtils.equals(id, node.getIdentifier()))
                return node;
            // add children to stack
            NodeIterator iterator = node.getNodes();
            while (iterator.hasNext())
                queue.add(iterator.nextNode());

        }
        throw new ItemNotFoundException("No node found with identifier/uuid [" + id + "]");
    }

    @Override
    public Node getNodeByUUID(String uuid) throws RepositoryException {
        return getNodeByIdentifier(uuid);
    }

    @Override
    public Property getProperty(String absPath) throws RepositoryException {
        Node node = getNode(StringUtils.substringBeforeLast(absPath, "/"));
        return node.getProperty(StringUtils.substringAfterLast(absPath, "/"));
    }

    @Override
    public Repository getRepository() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public RetentionManager getRetentionManager() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Node getRootNode() {
        return rootNode;
    }

    @Override
    public String getUserID() {
        return "admin";
    }

    @Override
    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public boolean hasCapability(String methodName, Object target, Object[] arguments) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean hasPendingChanges() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean hasPermission(String absPath, String actions) {
        return true;
    }

    @Override
    public Session impersonate(Credentials credentials) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean isLive() {
        return live;
    }

    @Override
    public void move(String srcAbsPath, String destAbsPath) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void refresh(boolean keepChanges) {
        // nothing to do
    }

    @Override
    public void removeLockToken(String lt) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void save() {
        // nothing to do
    }

    public void setValueFactory(ValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    @Override
    public String toString() {
        return "MockSession [rootNode=" + rootNode + ", workspace=" + workspace + "]";
    }

    public void setLive(boolean live) {
        this.live = live;
    }
}
