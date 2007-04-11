/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.test.mock;

import info.magnolia.cms.core.Content;
import info.magnolia.api.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;


/**
 * @author philipp
 * @version $Id$
 */
public class MockContent extends Content {

    private String uuid;

    private Content parent;

    private HierarchyManager hierarchyManager;

    private String name;

    private OrderedMap nodeDatas = new ListOrderedMap();

    private OrderedMap children = new ListOrderedMap();

    private String nodeTypeName = ItemType.CONTENTNODE.getSystemName();

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

    public void addNodeData(MockNodeData nd) {
        nd.setParent(this);
        nodeDatas.put(nd.getName(), nd);
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
        return (NodeData) this.nodeDatas.get(name);
    }

    public boolean hasNodeData(String name) throws RepositoryException {
        return nodeDatas.containsKey(name);
    }

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

    public String getName() {
        return this.name;
    }

    public String getNodeTypeName() throws RepositoryException {
        return this.nodeTypeName;
    }

    public void setNodeTypeName(String nodeTypeName) {
        this.nodeTypeName = nodeTypeName;
    }

    public Content getParent() {
        return this.parent;
    }

    public void setParent(Content parent) {
        this.parent = parent;
    }

    public HierarchyManager getHierarchyManager() {
        if (this.hierarchyManager == null && getParent() != null) {
            return ((MockContent) getParent()).getHierarchyManager();
        }
        return this.hierarchyManager;
    }

    /**
     * @param hm the hm to set
     */
    public void setHierarchyManager(HierarchyManager hm) {
        this.hierarchyManager = hm;
    }

    public String getUUID() {
        return this.uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

}