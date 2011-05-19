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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Credentials;
import javax.jcr.Item;
import javax.jcr.Node;
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
import org.xml.sax.ContentHandler;

/**
 * @version $Id$
 */
public class MockSession implements Session {

    public static final String JCR_ROOT_NAME = "jcr:root";

    final private Map<String, MockNode> nodesCache = new LinkedHashMap<String, MockNode>();

    private MockNode rootNode = null;

    final private Workspace workspace;

    public MockSession(MockWorkspace workspace) {
        this.workspace = workspace;
        workspace.setSession(this);

        rootNode = new MockNode(JCR_ROOT_NAME);
        rootNode.setSession(this);
    }

    public MockSession(String name) {
        this(new MockWorkspace(name));
    }

    @Override
    public void addLockToken(String lt) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    protected void cacheContent(MockNode node) throws RepositoryException{
        nodesCache.put(node.getPath(), node);
    }

    @Override
    public void checkPermission(String absPath, String actions)  {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void exportDocumentView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void exportDocumentView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse)  {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void exportSystemView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse)  {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void exportSystemView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse)  {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public AccessControlManager getAccessControlManager()  {
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
    public Item getItem(String absPath) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public String[] getLockTokens() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public String getNamespacePrefix(String uri) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public String[] getNamespacePrefixes()  {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public String getNamespaceURI(String prefix) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Node getNode(String absPath) throws PathNotFoundException, RepositoryException {
        MockNode c = nodesCache.get(absPath);
        if (c == null) {
            if ("/".equals(absPath)) {
                return rootNode;
            }
            c = (MockNode) rootNode.getNode(StringUtils.removeStart(absPath, "/"));
            cacheContent(c);
        }
        return c;
    }

    @Override
    public Node getNodeByIdentifier(String id) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Node getNodeByUUID(String uuid)  {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Property getProperty(String absPath) throws PathNotFoundException, RepositoryException {
        Node node = getNode(StringUtils.substringBeforeLast(absPath, "/"));
        return node.getProperty(StringUtils.substringAfterLast(absPath, "/"));
    }

    @Override
    public Repository getRepository() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public RetentionManager getRetentionManager()  {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Node getRootNode() {
        return rootNode;
    }

    @Override
    public String getUserID() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public ValueFactory getValueFactory()  {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public boolean hasCapability(String methodName, Object target, Object[] arguments)  {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean hasPendingChanges() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean hasPermission(String absPath, String actions) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Session impersonate(Credentials credentials)  {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void importXML(String parentAbsPath, InputStream in, int uuidBehavior){
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean isLive() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean itemExists(String absPath) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void move(String srcAbsPath, String destAbsPath) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean nodeExists(String absPath) throws RepositoryException {
        Node node = null;
        try {
            node = getNode(absPath);
        } catch (Exception e) {
            // ignore
        }
        return node != null;
    }

    @Override
    public boolean propertyExists(String absPath)  {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void refresh(boolean keepChanges) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void removeItem(String absPath)  {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void removeLockToken(String lt) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void setNamespacePrefix(String prefix, String uri)  {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    protected void setRootNode(MockNode root) {
        this.rootNode = root;
    }

    @Override
    public String toString() {
        return "MockSession [rootNode=" + rootNode + ", workspace=" + workspace + "]";
    }
}
