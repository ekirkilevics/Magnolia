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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.DefaultContent;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.easymock.classextension.EasyMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 */
public class MockContent extends DefaultContent {

    private static Logger log = LoggerFactory.getLogger(MockContent.class);

    private String uuid;

    private int index = 1;

    private Content parent;

    private MockHierarchyManager hierarchyManager;

    private String name;

    private OrderedMap nodeDatas = new ListOrderedMap();

    private OrderedMap children = new ListOrderedMap();

    private String nodeTypeName = ItemType.CONTENTNODE.getSystemName();

    private Node jcrNode;


    public MockContent(String name) {
        this.name = name;
    }

    public MockContent(String name, ItemType contentType) {
        this(name);
        this.setNodeTypeName(contentType.getSystemName());
    }

    public MockContent(String name, OrderedMap nodeDatas, OrderedMap children) {
        this(name);
        for (Iterator iter = children.values().iterator(); iter.hasNext();) {
            MockContent c = (MockContent) iter.next();
            addContent(c);
        }
        for (Iterator iter = nodeDatas.values().iterator(); iter.hasNext();) {
            MockNodeData nd = (MockNodeData) iter.next();
            addNodeData(nd);
        }
    }

    public boolean isNodeType(String type) {
        try {
            return this.getNodeTypeName().equalsIgnoreCase(type);
        }
        catch (RepositoryException e) {
            log.error("can't read node type name", e);
        }
        return false;
    }

    public void addNodeData(MockNodeData nd) {
        nd.setParent(this);
        nodeDatas.put(nd.getName(), nd);
    }

    public NodeData createNodeData(String name, int type) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        final MockNodeData nd = new MockNodeData(name, type);
        addNodeData(nd);
        return nd;
    }

    public NodeData createNodeData(String name, Object obj) throws RepositoryException {
        final MockNodeData nd = new MockNodeData(name, obj);
        addNodeData(nd);
        return nd;
    }

    public MockMetaData createMetaData() {
        addContent(new MockContent("MetaData"));//, ItemType."mgnl:metaData"));
        return getMetaData();
    }

    public Content createContent(String name, String contentType) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        return createContent(name, new ItemType(contentType));
    }

    public Content createContent(String name, ItemType contentType) {
        MockContent c = new MockContent(name, contentType);
        addContent(c);
        return c;
    }

    public void addContent(MockContent child) {
        child.setParent(this);
        children.put(child.getName(), child);
    }

    public Content getContent(String name) throws RepositoryException {
        Content c;
        if (name.contains("/")) {
            c = getContent(StringUtils.substringBefore(name, "/"));
            if (c != null) {
                return c.getContent(StringUtils.substringAfter(name, "/"));
            }
        }
        else {
            c = (Content) children.get(name);
        }
        if (c == null) {
            throw new PathNotFoundException(name);
        }
        return c;
    }

    public boolean hasContent(String name) throws RepositoryException {
        return children.containsKey(name);
    }

    public String getHandle() {
        if (this.getParent() != null && !this.getParent().getName().equals("jcr:root")) {
            return getParent().getHandle() + "/" + this.getName();
        }
        return "/" + this.getName();
    }

    public int getLevel() throws PathNotFoundException, RepositoryException {
        if (this.getParent() == null) {
            return 0;
        }
        return getParent().getLevel() + 1;
    }

    public Collection getNodeDataCollection() {
        return this.nodeDatas.values();
    }

    public NodeData getNodeData(String name) {
        final MockNodeData nodeData = (MockNodeData) this.nodeDatas.get(name);
        if (nodeData != null) {
            return nodeData;
        } else {
            final MockNodeData fakeNodeData= new MockNodeData(name, null);
            fakeNodeData.setParent(this);
            return fakeNodeData;
        }
    }

    public boolean hasNodeData(String name) throws RepositoryException {
        return nodeDatas.containsKey(name);
    }

    // TODO : use the given Comparator
    public Collection getChildren(final ContentFilter filter, Comparator orderCriteria) {
        // copy
        List children = new ArrayList(this.children.values());

        CollectionUtils.filter(children, new Predicate() {

            public boolean evaluate(Object object) {
                return filter.accept((Content) object);
            }
        });

        return children;
    }

    public Collection getChildren(final String contentType, String namePattern) {
        if (!"*".equals(namePattern)) {
            throw new IllegalStateException("Only the \"*\" name pattern is currently supported in MockContent.");
        }
        return getChildren(new ContentFilter() {
            public boolean accept(Content content) {
                return contentType == null || content.isNodeType(contentType);
            }
        });

    }

    public Content getChildByName(String namePattern) {
        return (Content) children.get(namePattern);
    }

    public void orderBefore(String srcName, String beforeName) throws RepositoryException {
        Content movedNode = (Content)children.get(srcName);
        List tmp = new ArrayList(children.values());
        tmp.remove(movedNode);
        tmp.add(tmp.indexOf(children.get(beforeName)), movedNode);
        children.clear();
        for (Iterator iter = tmp.iterator(); iter.hasNext();) {
            Content child = (Content) iter.next();
            children.put(child.getName(), child);
        }
    }

    public void save() throws RepositoryException {
        // nothing to do
    }

    public Node getJCRNode() {
        if(jcrNode==null){
            jcrNode = new MockJCRNode(this);
            /*
            jcrNode = EasyMock.createNiceMock(Node.class);
            // we should get that from the hierarchy manager
            Session session = EasyMock.createNiceMock(Session.class);
            //session.isLive();
            try {
                EasyMock.expect(jcrNode.getSession()).andStubReturn(session);
                EasyMock.replay(jcrNode);
                EasyMock.replay(session);
            }
            catch (RepositoryException e) {
                log.error("WONT HAPPEN", e);
            }
            */
        }
        return this.jcrNode;
    }


    public void setJCRNode(Node jcrNode) {
        this.jcrNode = jcrNode;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeTypeName() throws RepositoryException {
        return this.nodeTypeName;
    }

    public void setNodeTypeName(String nodeTypeName) {
        this.nodeTypeName = nodeTypeName;
    }

    public void delete() throws RepositoryException {
        final MockContent parent = (MockContent) getParent();
        final boolean removedFromParent = parent.children.values().remove(this);
        getHierarchyManager().removedCachedNode(this);
        if (!removedFromParent) {
            throw new RepositoryException("MockContent could not delete itself");
        }
    }

    public Content getParent() {
        return this.parent;
    }

    public void setParent(Content parent) {
        this.parent = parent;
    }

    public Content getAncestor(int digree) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (digree > this.getLevel()) {
            throw new PathNotFoundException();
        }
        Content ancestor = this;
        for (int i=getLevel();i==digree;i--){
            ancestor=ancestor.getParent();
        }
        return ancestor;
    }

    public MockHierarchyManager getHierarchyManager() {
        if (this.hierarchyManager == null && getParent() != null) {
            return ((MockContent) getParent()).getHierarchyManager();
        }
        return this.hierarchyManager;
    }

    /**
     * @param hm the hm to set
     */
    public void setHierarchyManager(MockHierarchyManager hm) {
        this.hierarchyManager = hm;
    }

    public String getUUID() {
        return this.uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public MockMetaData getMetaData() {
        try {
            return new MockMetaData((MockContent) getContent("MetaData"));
        } catch (RepositoryException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public String toString() {
        return super.toString() + ": " + this.getHandle();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
