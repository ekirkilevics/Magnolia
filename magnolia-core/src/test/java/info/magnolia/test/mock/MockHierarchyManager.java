/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.DefaultHierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

/**
 * @version $Id$
 */
public class MockHierarchyManager extends DefaultHierarchyManager {

    public MockHierarchyManager() {
        this(new MockSession("test"));
    }

    public MockHierarchyManager(Session jcrSession) {
        super(jcrSession);
    }


    /**
     * Publically expose it.
     * @see info.magnolia.cms.core.DefaultHierarchyManager#getJcrSession()
     */
    @Override
    public Session getJcrSession() {
        return super.getJcrSession();
    }

    /**
     * Set mock workspace if observation or similar things are needed. Will only work when using a MockSession.
     */
    public void setWorkspace(Workspace workspace) {
        ((MockSession) getJcrSession()).setWorkspace(workspace);
    }

    @Override
    public MockContent getRoot(){
        try {
            return new MockContent((MockNode) getJcrSession().getRootNode());
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public Content createContent(String path, String label, String contentType) throws PathNotFoundException,
    RepositoryException, AccessDeniedException {
        Node parent = NodeUtil.createPath(getJcrSession().getRootNode(), path, ItemType.CONTENTNODE.getSystemName());
        Content content = wrapAsContent(parent, label, contentType);
//        setMetaData(content.getMetaData());
        return content;
    }


    @Override
    protected Content wrapAsContent(Node node) {
        return new MockContent((MockNode) node);
    }

    @Override
    public Content wrapAsContent(Node rootNode, String path) throws AccessDeniedException, PathNotFoundException, RepositoryException {
        return new MockContent((MockNode) rootNode, path);
    }

    @Override
    protected Content wrapAsContent(Node rootNode, String path, String contentType) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return new MockContent((MockNode) rootNode, path, contentType);
    }
}
