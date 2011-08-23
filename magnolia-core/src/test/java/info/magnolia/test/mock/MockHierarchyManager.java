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
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class MockHierarchyManager extends DefaultHierarchyManager {
    private static final Logger log = LoggerFactory.getLogger(MockHierarchyManager.class);

    private final Map nodes = new HashMap();

    private final MockContent root;

    private String name = "TestMockHierarchyManager";

    public MockHierarchyManager() {
        this(null);
    }

    public MockHierarchyManager(String name) {
        super();
        if (name != null) {
            this.name = name;
        }
        setJcrSession(new MockJCRSession(this));
        this.root = new MockContent("jcr:root");
        root.setUUID("jcr:root");
        root.setHierarchyManager(this);
    }

    /**
     * Expose internal JCR session to outside world
     */
    @Override
    public Session getJcrSession() {
        return super.getJcrSession();
    }

    @Override
    public Content getContent(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content c = (Content) nodes.get(path);
        if (c == null) {
            if ("/".equals(path)) {
                return root;
            }
            c = root.getContent(StringUtils.removeStart(path, "/"));
            cacheContent(c);
        }
        return c;
    }

    protected void cacheContent(Content node) {
        nodes.put(node.getHandle(), node);
        ((MockContent) node).setHierarchyManager(this);
    }

    void removedCachedNode(MockContent node) {
        nodes.values().remove(node);
    }

    @Override
    public Content createContent(String path, String label, String contentType) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content parent = ContentUtil.createPath(getRoot(), StringUtils.removeStart(path, "/"), ItemType.CONTENTNODE);
        return parent.createContent(label, contentType);
    }

    @Override
    public Content getContentByUUID(String uuid) throws ItemNotFoundException, RepositoryException, AccessDeniedException {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID can't be null");
        }
        final Content result = getContentByUUID(getRoot(), uuid);
        if (result == null) {
            throw new ItemNotFoundException("Can't find item with uuid " + uuid);
        }
        return result;
    }

    protected Content getContentByUUID(Content node, final String uuid) {
        if (uuid.equals(node.getUUID())) {
            return node;
        }

        for (Iterator iter = ContentUtil.getAllChildren(node).iterator(); iter.hasNext();) {
            Content child = (Content) iter.next();
            Content found = getContentByUUID(child, uuid);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @Override
    public void delete(String path) throws RepositoryException {
        getContent(path).delete();
    }

    @Override
    public MockContent getRoot() {
        return this.root;
    }

    @Override
    public boolean isExist(String path) {
        try {
            this.getContent(path);
            return true;
        } catch (RepositoryException e) {
            try {
                return this.isNodeData(path);
            } catch (RepositoryException e1) {
                return false;
            }
        }
    }

    @Override
    public boolean isNodeData(String path) throws AccessDeniedException {
        try {
            Content node = getContent(StringUtils.substringBeforeLast(path, "/"));
            return node.hasNodeData(StringUtils.substringAfterLast(path, "/"));
        } catch (RepositoryException e) {
            return false;
        }
    }

    @Override
    public NodeData getNodeData(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content node = getContent(StringUtils.substringBeforeLast(path, "/"));
        return node.getNodeData(StringUtils.substringAfterLast(path, "/"));
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder();
        try {
            ContentUtil.visit(getRoot(), new ContentUtil.Visitor() {
                @Override
                public void visit(Content node) throws Exception {
                    StringBuilder prefix = new StringBuilder();
                    for (int i = 1; i <= node.getLevel(); i++) {
                        prefix.append("  ");
                    }
                    str.append(prefix.toString()).append(node.getName()).append("\n");
                    prefix.append("  ");

                    for (Iterator<NodeData> iter = node.getNodeDataCollection().iterator(); iter.hasNext();) {
                        NodeData nd = iter.next();
                        str.append(prefix.toString()).append(nd.getName()).append(" = ").append(nd.getString())
                                .append("\n");
                    }
                }
            });
        } catch (Exception e) {
            log.error("can't print content", e);
        }

        return str.toString();
    }

    @Override
    public Workspace getWorkspace() {
        return getJcrSession().getWorkspace();
    }

    /**
     * Set mock workspace if observation or similar things are needed. Will only work when using a MockSession.
     */
    public void setWorkspace(Workspace workspace) {
        ((MockJCRSession) getJcrSession()).setWorkspace(workspace);
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @param website
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void save() throws RepositoryException {
        getJcrSession().save();
    }
}
