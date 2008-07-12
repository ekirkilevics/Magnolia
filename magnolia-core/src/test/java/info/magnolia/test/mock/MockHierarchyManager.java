/**
 * This file Copyright (c) 2003-2008 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
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

    private static Logger log = LoggerFactory.getLogger(MockHierarchyManager.class);

    private Map nodes = new HashMap();

    private MockContent root ;

    private MockSession session;

    private String name;

    public MockHierarchyManager() {
        this(ContentRepository.CONFIG);
    }

    public MockHierarchyManager(String name) {
        this.name = name;
        session = new MockSession(this);
        root = new MockContent("jcr:root");
        root.setUUID("jcr:root");
        root.setHierarchyManager(this);
    }

    public Content getContent(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content c = (Content) nodes.get(path);
        if( c == null){
            c = root.getContent(StringUtils.removeStart(path, "/"));
            addContent(c);
        }
        return c;
    }

    public void addContent(Content node){
        nodes.put(node.getHandle(), node);
        ((MockContent)node).setHierarchyManager(this);
    }

    void removedCachedNode(MockContent node) {
        nodes.values().remove(node);
    }

    public Content createContent(String path, String label, String contentType) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content parent = ContentUtil.createPath(getRoot(), StringUtils.removeStart(path, "/"), ItemType.CONTENTNODE);
        return parent.createContent(label, contentType);
    }

    public Content getContentByUUID(String uuid) throws ItemNotFoundException, RepositoryException, AccessDeniedException {
        final Content result = getContentByUUID(getRoot(), uuid);
        if (result == null) {
            throw new ItemNotFoundException("Can't find item with uuid " + uuid);
        }
        return result;
    }

    protected Content getContentByUUID(Content node, final String uuid) {
        if(uuid.equals(node.getUUID())){
            return node;
        }

        for (Iterator iter = ContentUtil.getAllChildren(node).iterator(); iter.hasNext();) {
            Content child = (Content) iter.next();
            Content found = getContentByUUID(child, uuid);
            if(found != null){
                return found;
            }
        }
        return null;
    }

    public void delete(String path) throws RepositoryException {
        getContent(path).delete();
    }

    public MockContent getRoot() {
        return this.root;
    }


    public void setRoot(MockContent root) {
        this.root = root;
    }

    public boolean isExist(String path) {
        try {
            this.getContent(path);
            return true;
        }
        catch (RepositoryException e) {
            try {
                return this.isNodeData(path);
            }
            catch (RepositoryException e1) {
                return false;
            }
        }
    }

    public boolean isNodeData(String path) throws AccessDeniedException {
        try {
            Content node = getContent(StringUtils.substringBeforeLast(path, "/"));
            return node.hasNodeData(StringUtils.substringAfterLast(path, "/"));
        }
        catch (RepositoryException e) {
            return false;
        }
    }

    public NodeData getNodeData(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content node = getContent(StringUtils.substringBeforeLast(path, "/"));
        return node.getNodeData(StringUtils.substringAfterLast(path, "/"));
    }



    public String toString() {
        final StringBuffer str = new StringBuffer();
        try {
            ContentUtil.visit(getRoot(), new ContentUtil.Visitor(){
                public void visit(Content node) throws Exception {
                    String prefix = "";
                    for(int i =1 ; i <= node.getLevel(); i++){
                        prefix += "  ";
                    }
                    str.append(prefix).append(node.getName()).append("\n");
                    prefix += "  ";

                    for (Iterator iter = node.getNodeDataCollection().iterator(); iter.hasNext();) {
                        NodeData nd = (NodeData) iter.next();
                        str.append(prefix).append(nd.getName()).append(" = ").append(nd.getString()).append("\n");
                    }
                }
            });
        }
        catch (Exception e) {
            log.error("can't print content", e);
        }

        return str.toString();
    }

    public Workspace getWorkspace() {
        return this.session.getWorkspace();
    }

    /**
     * Set mock workspace if observation or similar things are needed
     */
    public void setWorkspace(Workspace workspace) {
        this.session.setWorkspace(workspace);
    }


    public String getName() {
        return this.name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public MockSession getSession() {
        return this.session;
    }


    public void setSession(MockSession session) {
        this.session = session;
    }

}
