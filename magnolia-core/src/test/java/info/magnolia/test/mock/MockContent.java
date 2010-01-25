/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.cms.core.AbstractContent;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.version.ContentVersion;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.Rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ChildrenCollectorFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 */
public class MockContent extends AbstractContent {

    private static Logger log = LoggerFactory.getLogger(MockContent.class);

    private String uuid;

    private int index = 1;

    private Content parent;

    private String name;

    private Map<String, NodeData> nodeDatas = new ListOrderedMap();

    private Map<String, MockContent> children = new ListOrderedMap();

    private String nodeTypeName = ItemType.CONTENTNODE.getSystemName();

    private Node node;

    public MockContent(String name) {
        this.name = name;
        this.node = new MockJCRNode(this);
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
            return this.getNodeTypeName().equals(type);
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

    public MockMetaData createMetaData() {
        addContent(new MockContent("MetaData"));//, ItemType."mgnl:metaData"));
        return getMetaData();
    }

    public Content createContent(String name, String contentType) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        MockContent c = new MockContent(name, new ItemType(contentType));
        c.setHierarchyManager(this.getHierarchyManager());
        addContent(c);
        return c;
    }

    public void addContent(MockContent child) {
        child.setParent(this);
        children.put(child.getName(), child);
    }

    public Content getContent(String path) throws RepositoryException {
        Content c;
        if (path.contains("/")) {
            String[] names = StringUtils.split(path, "/");
            Content current = this;
            for (String name : names) {
                if(name.equals("..")){
                    current = current.getParent();
                }
                else{
                    current = current.getContent(name);
                }
            }
            return current;
        }
        else {
            c = (Content) children.get(path);
        }
        if (c == null) {
            throw new PathNotFoundException(path);
        }
        return c;
    }

    public boolean hasContent(String name) throws RepositoryException {
        try {
            getContent(name);
        }
        catch (PathNotFoundException e) {
            return false;
        }
        return true;
    }

    public String getHandle() {
        if (this.getParent() != null && !this.getParent().getName().equals("jcr:root")) {
            return getParent().getHandle() + "/" + this.getName();
        }
        else{
            return "/" + this.getName();
        }
    }

    public int getLevel() throws PathNotFoundException, RepositoryException {
        if (this.getParent() == null) {
            return 0;
        }
        return getParent().getLevel() + 1;
    }

    public Collection<NodeData> getNodeDataCollection(String namePattern) {
        // FIXME try to find a better solution than filtering now
        // problem is that getNodeData(name, type) will have to add the node data
        // as setValue() might be called later on an the node data starts to exist
        ArrayList<NodeData> onlyExistingNodeDatas = new ArrayList<NodeData>();
        for (NodeData nodeData : nodeDatas.values()) {
            if(nodeData.isExist()){
                if (namePattern == null || matchesNamePattern(nodeData, namePattern)) {
                    onlyExistingNodeDatas.add(nodeData);
                }
            }
        }

        // adding binaries too:
        try {
            onlyExistingNodeDatas.addAll(getBinaryNodeDatas(namePattern));
        } catch (RepositoryException e) {
            throw new IllegalStateException("Can't read node datas of " + toString(), e);
        }

        return onlyExistingNodeDatas;
    }

    public Collection<Content> getChildren(final ContentFilter filter, final String namePattern, Comparator<Content> orderCriteria) {
        // copy
        final List<Content> children = new ArrayList<Content>(this.children.values());

        final Predicate filterPredicate = new Predicate() {
            public boolean evaluate(Object object) {
                return filter.accept((Content) object);
            }
        };

        CollectionUtils.filter(children, filterPredicate);

        if (namePattern != null) {
            CollectionUtils.filter(children, new NamePatternFilter(namePattern));
        }


        return children;
    }

    public void orderBefore(String srcName, String beforeName) throws RepositoryException {
        MockContent movedNode = children.get(srcName);
        List<MockContent> tmp = new ArrayList<MockContent>(children.values());
        tmp.remove(movedNode);
        tmp.add(tmp.indexOf(children.get(beforeName)), movedNode);
        children.clear();
        for (MockContent child : tmp) {
            children.put(child.getName(), child);
        }
    }

    public void save() throws RepositoryException {
        // nothing to do
    }

    public void setJCRNode(Node jcrNode) {
        this.node = jcrNode;
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
        HierarchyManager hm = getHierarchyManager();
        if(hm instanceof MockHierarchyManager){
            ((MockHierarchyManager)hm).removedCachedNode(this);
        }
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

    public Content getAncestor(int level) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (level > this.getLevel()) {
            throw new PathNotFoundException();
        }
        Content ancestor = this;
        for (int i=getLevel();i==level;i--){
            ancestor=ancestor.getParent();
        }
        return ancestor;
    }
    
    public MockHierarchyManager getHierarchyManager() {
        if (this.hierarchyManager == null && getParent() != null) {
            return ((MockContent) getParent()).getHierarchyManager();
        }
        return (MockHierarchyManager) this.hierarchyManager;
    }
    
    public String getUUID() {
        return this.uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public MockMetaData getMetaData() {
        try {
            return new MockMetaData((MockContent) getContent(MetaData.DEFAULT_META_NODE));
        } catch (RepositoryException e) {
            //we mimick the default behaviour here, so lets create an empty metadata node
        }
        return new MockMetaData(new MockContent(MetaData.DEFAULT_META_NODE));
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public NodeData getNodeData(String name, int type) throws RepositoryException {
        if(nodeDatas.containsKey(name)){
            return nodeDatas.get(name);
        }
        else{
            MockNodeData nodeData;
            // TODO if(type == PropertyType.UNDEFINED){
            //    if (hasContent(name) && getContent(name).isNodeType(ItemType.NT_RESOURCE)) {
            //        type = PropertyType.BINARY;
            //    } - else ?

            if(type == PropertyType.BINARY){
                nodeData = new BinaryMockNodeData(name, (MockContent) getContent(name));
            }
            else{
                nodeData = new MockNodeData(name, type);
            }
            addNodeData(nodeData);
            return nodeData;
        }
    }

    public Collection<Content> getAncestors() throws PathNotFoundException, RepositoryException {
        ArrayList<Content> ancestors = new ArrayList<Content>();
        Content parent = getParent();
        while(parent != null){
            ancestors.add(parent);
            parent.getParent();
        }
        return ancestors;
    }

    public ItemType getItemType() throws RepositoryException {
        return new ItemType(getNodeTypeName());
    }
    
    public Node getJCRNode() {
        return node;
    }

    public boolean hasMetaData() {
        return true;
    }
    
    public void addMixin(String type) throws RepositoryException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public Version addVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public Version addVersion(Rule rule) throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public VersionIterator getAllVersions() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public ContentVersion getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public Lock getLock() throws LockException, RepositoryException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public NodeType getNodeType() throws RepositoryException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public ContentVersion getVersionedContent(Version version) throws RepositoryException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public ContentVersion getVersionedContent(String versionName) throws RepositoryException {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public boolean holdsLock() throws RepositoryException {
        return false;
    }

    public boolean isLocked() throws RepositoryException {
        return false;
    }

    public boolean isModified() {
        return false;
    }

    public Lock lock(boolean isDeep, boolean isSessionScoped) throws LockException, RepositoryException {
        return null;
    }

    public Lock lock(boolean isDeep, boolean isSessionScoped, long yieldFor) throws LockException, RepositoryException {
        return null;
    }

    public void refresh(boolean keepChanges) throws RepositoryException {
    }

    public void removeMixin(String type) throws RepositoryException {
    }

    public void removeVersionHistory() throws AccessDeniedException, RepositoryException {
    }

    public void restore(String versionName, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException,
        RepositoryException {
    }

    public void restore(Version version, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException,
        RepositoryException {
    }

    public void restore(Version version, String relPath, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException,
        RepositoryException {
    }

    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException,
        RepositoryException {
    }

    public void unlock() throws LockException, RepositoryException {
    }

    public void updateMetaData() throws RepositoryException, AccessDeniedException {
    }


    /**
     * Filters a name of a NodeData or Content instance according to the same rules applied by Jackrabbit
     * in the Property and Node interfaces.
     */
    private static class NamePatternFilter implements Predicate {
        private final String namePattern;

        public NamePatternFilter(String namePattern) {
            this.namePattern = namePattern;
        }

        public boolean evaluate(Object object) {
            return matchesNamePattern(object, namePattern);
        }
    }

    private static boolean matchesNamePattern(Object object, String namePattern) {
        final String name;
        if (object instanceof NodeData) {
            name = ((NodeData) object).getName();
        } else if (object instanceof Content) {
            name = ((Content) object).getName();
        } else {
            throw new IllegalStateException("Unsupported object type: " + object.getClass());
        }
        return ChildrenCollectorFilter.matches(name, namePattern);
    }
}
