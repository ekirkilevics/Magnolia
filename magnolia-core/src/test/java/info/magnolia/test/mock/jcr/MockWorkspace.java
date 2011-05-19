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

import javax.jcr.NamespaceRegistry;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionManager;

import org.xml.sax.ContentHandler;

/**
 * @version $Id$
 */
public class MockWorkspace implements Workspace {

    final private String name;

    private MockSession session;

    public MockWorkspace(String name) {
        this.name = name;
    }

    @Override
    public void clone(String srcWorkspace, String srcAbsPath, String destAbsPath, boolean removeExisting) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public void copy(String srcAbsPath, String destAbsPath) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public void copy(String srcWorkspace, String srcAbsPath, String destAbsPath) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public void createWorkspace(String name) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public void createWorkspace(String name, String srcWorkspace) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public void deleteWorkspace(String name) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public String[] getAccessibleWorkspaceNames() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public LockManager getLockManager() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NamespaceRegistry getNamespaceRegistry() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public NodeTypeManager getNodeTypeManager() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public ObservationManager getObservationManager() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public QueryManager getQueryManager() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public VersionManager getVersionManager() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public void move(String srcAbsPath, String destAbsPath) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    @Override
    public void restore(Version[] versions, boolean removeExisting) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");

    }

    protected void setSession(MockSession session) {
        this.session = session;
    }

}
