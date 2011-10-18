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

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.test.mock.MockNodeType;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jcr.Binary;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.lock.Lock;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.AbstractNode;
import org.apache.jackrabbit.util.ChildrenCollectorFilter;

/**
 * Mock implementation of a Node. Initially gets a random generated UUID set.
 *
 * @version $Id$
 */
public class MockNode extends AbstractNode {

    public static final String ROOT_NODE_NAME = "jcr:root";

    public static String generateIdentifier() {
        return UUID.randomUUID().toString();
    }
    private final LinkedHashMap<String, MockNode> children = new LinkedHashMap<String, MockNode>();

    private String identifier = generateIdentifier();

    private int index = 1;

    private final List<String> mixins = new ArrayList<String>();

    private String name;

    public void setName(String name) {
        this.name = name;
    }
    private MockNode parent;

    private String primaryType;

    private final LinkedHashMap<String, Property> properties = new LinkedHashMap<String, Property>();

    private Session session;

    /**
     * Creates a root node -> name == ROOT_NODE_NAME.
     */
    public MockNode() {
        this(ROOT_NODE_NAME);
    }
    public MockNode(String name) {
        this(name, MgnlNodeType.NT_CONTENTNODE);
    }

    public MockNode(String name, Map<String, MockValue> properties, Map<String, MockNode> children)  throws RepositoryException{
        this(name);
        Iterator<String> propertiesIterator = properties.keySet().iterator();
        while (propertiesIterator.hasNext()) {
            String propertyName = propertiesIterator.next();
            setProperty(propertyName, properties.get(propertyName));

        }
        Iterator<MockNode> childrenIterator = children.values().iterator();
        while (childrenIterator.hasNext()) {
            MockNode child = childrenIterator.next();
            addNode(child);
        }
    }

    public MockNode(String name, String primaryType) {
        this.name = name;
        this.primaryType = primaryType;
    }

    @Override
    public void accept(ItemVisitor visitor) throws RepositoryException {
        visitor.visit(this);
    }

    @Override
    public void addMixin(String mixinName) {
        this.mixins.add(mixinName);
    }

    public void addNode(MockNode child) {
        child.setParent(this);
        children.put(child.getName(), child);
    }

    @Override
    public Node addNode(String relPath) throws RepositoryException {
        return addNode(relPath, primaryType);
    }

    @Override
    public Node addNode(String relPath, String primaryNodeTypeName) throws RepositoryException {
        final MockNode newChild = new MockNode(relPath);
        newChild.setPrimaryType(primaryNodeTypeName);
        addNode(newChild);
        return newChild;
    }

    @Override
    public boolean canAddMixin(String mixinName) {
        return !StringUtils.isEmpty(mixinName);
    }

    @Override
    public void cancelMerge(Version version) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Version checkin() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void checkout() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void doneMerge(Version version) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void followLifecycleTransition(String transition) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public String[] getAllowedLifecycleTransistions() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Item getAncestor(int depth) throws RepositoryException {
        if(this.getDepth() == depth){
            return this;
        }
        Node parentNode = this.getParent();
        while(parentNode.getDepth() != depth){
            parentNode = parentNode.getParent();
        }
        return parentNode;
    }

    @Override
    public Version getBaseVersion() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    public Map<String, MockNode> getChildren() {
        return this.children;
    }

    @Override
    public String getCorrespondingNodePath(String workspaceName) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public NodeDefinition getDefinition() throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public Lock getLock() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        NodeType[] nodeTypes = new NodeType[mixins.size()];
        for (int i = 0; i < mixins.size(); i++) {
            nodeTypes[i] = new MockNodeType(mixins.get(i));
        }
        return nodeTypes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {
        Node c;
        if (relPath.contains("/")) {
            String[] names = StringUtils.split(relPath, "/");
            Node current = this;
            for (String name : names) {
                if (name.equals("..")) {
                    current = current.getParent();
                } else {
                    current = current.getNode(name);
                }
            }
            return current;
        }
        c = children.get(relPath);
        if (c == null) {
            throw new PathNotFoundException(relPath);
        }
        return c;
    }

    @Override
    public NodeIterator getNodes() {
        return new MockNodeIterator(children.values());
    }

    @Override
    public NodeIterator getNodes(String namePattern) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Node getParent() throws ItemNotFoundException {
        if (isRoot()) {
            throw new ItemNotFoundException("This is the rootNode - it doesn't have a parent!");
        }
        return parent;
    }

    private boolean isRoot() {
        return ROOT_NODE_NAME.equals(getName()) && parent == null;
    }

    @Override
    public String getPath() throws RepositoryException {
        if (parent == null) {
            return ROOT_NODE_NAME.equals(getName()) ? "/" : "";
        }
        return super.getPath();
    }

    @Override
    public Item getPrimaryItem() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public NodeType getPrimaryNodeType() {
        return new MockNodeType(this.primaryType);
    }

    @Override
    public PropertyIterator getProperties() {
        return new MockPropertyIterator(properties.values());
    }

    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        // Inspired by org.apache.jackrabbit.core.NodeImpl
        return ChildrenCollectorFilter.collectProperties(this, namePattern);
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException {
        if ("jcr:primaryType".equals(relPath)) {
            return new MockProperty(relPath, primaryType, this);
        }
        Property prop = properties.get(relPath);
        if (prop == null) {
            throw new PathNotFoundException(relPath);
        }
        return prop;
    }

    @Override
    public PropertyIterator getReferences() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public PropertyIterator getReferences(String name) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Session getSession() {
        if (session == null && parent != null) {
            // fallback - avoid session has to be set on every level
            return parent.getSession();
        }
        return session;
    }

    @Override
    public NodeIterator getSharedSet() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    /**
     * @deprecated as on {@link Node} - use getIdentifier instead
     */
    @Override
    public String getUUID() {
        return getIdentifier();
    }

    @Override
    public VersionHistory getVersionHistory() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public PropertyIterator getWeakReferences() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public PropertyIterator getWeakReferences(String name) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean hasNode(String relPath) throws RepositoryException {
        try {
            getNode(relPath);
        } catch (PathNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasNodes() {
        return (this.getChildren().size() > 0);
    }

    @Override
    public boolean hasProperties() {
        return (this.properties.size() > 0);
    }

    @Override
    public boolean hasProperty(String relPath) {
        return (this.properties.get(relPath) != null);
    }

    @Override
    public boolean holdsLock() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean isCheckedOut() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean isLocked() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean isModified() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean isNew() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public boolean isNode() {
        return true;
    }

    @Override
    public boolean isNodeType(String nodeTypeName) {
        if(primaryType.equals(nodeTypeName)) {
            return true;
        }
        for(NodeType nodeType : getPrimaryNodeType().getSupertypes()){
            if(nodeTypeName.equals(nodeType.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSame(Item otherItem) throws RepositoryException {
        // very strict but better than nothing ;-)
        return equals(otherItem);
    }

    @Override
    public Lock lock(boolean isDeep, boolean isSessionScoped) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public NodeIterator merge(String srcWorkspace, boolean bestEffort) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    /**
     * @see javax.jcr.Node#orderBefore(String, String) for description of desired behaviour
     */
    @Override
    public void orderBefore(String srcName, String beforeName) {
        // don't do anything if both names are identical
        if (!srcName.equals(beforeName)) {
            int childrenSize = children.size();
            MockNode nodeToMove = children.remove(srcName);
            List<MockNode> newOrder = new ArrayList<MockNode>();

            for (MockNode child : children.values()) {
                if (child.getName().equals(beforeName)) {
                    newOrder.add(nodeToMove);
                }
                newOrder.add(child);
            }

            if (childrenSize > newOrder.size()) {
                // in that case nodeToMove has not yet been added but should be added at the end - so do it!
                newOrder.add(nodeToMove);
            }

            children.clear();

            for (MockNode child : newOrder) {
                children.put(child.getName(), child);
            }
        }
    }

    @Override
    public void refresh(boolean keepChanges) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void remove() {
        try {
            ((MockNode) getParent()).removeFromChildren(this);
        } catch (ItemNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean removeFromChildren(Node childNode) {
        if (children.containsValue(childNode)) {
            Iterator<String> childrenNames = children.keySet().iterator();
            while (childrenNames.hasNext()) {
                String childName = childrenNames.next();
                if (childNode.equals(children.get(childName))) {
                    children.remove(childName);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void removeMixin(String mixinName) {
        mixins.remove(mixinName);
    }

    protected boolean removeProperty(String propertyName) {
        Property property = properties.remove(propertyName);
        return property != null;
    }

    @Override
    public void removeShare() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void removeSharedSet() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void restore(String versionName, boolean removeExisting) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void restore(Version version, boolean removeExisting) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void restore(Version version, String relPath, boolean removeExisting) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void restoreByLabel(String versionLabel, boolean removeExisting) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setParent(MockNode parent) {
        this.parent = parent;
        setSessionFrom(parent);
    }

    @Override
    public void setPrimaryType(String primaryType) {
        this.primaryType = primaryType;
    }

    @Override
    public Property setProperty(String name, BigDecimal value) throws RepositoryException {
        return setProperty(name, new MockValue(value));
    }

    @Override
    public Property setProperty(String name, Binary value) throws RepositoryException {
        return setProperty(name, new MockValue(value));
    }

    @Override
    public Property setProperty(String name, boolean value) throws RepositoryException {
        return setProperty(name, new MockValue(value));
    }

    @Override
    public Property setProperty(String name, Calendar value) throws RepositoryException {
        return setProperty(name, new MockValue(value));
    }

    @Override
    public Property setProperty(String name, double value) throws RepositoryException {
        return setProperty(name, new MockValue(value));
    }

    @Override
    public Property setProperty(String name, InputStream value) throws RepositoryException {
        return setProperty(name, new MockValue(value));
    }

    @Override
    public Property setProperty(String name, long value) throws RepositoryException {
        return setProperty(name, new MockValue(value));
    }

    @Override
    public Property setProperty(String name, Node value) throws RepositoryException {
        return setProperty(name, value.getIdentifier(), PropertyType.REFERENCE);
    }

    @Override
    public Property setProperty(String name, String value) throws RepositoryException {
        return setProperty(name, new MockValue(value));
    }

    @Override
    public Property setProperty(String name, String value, int type) throws RepositoryException {
        return setProperty(name, new MockValue(value, type));
    }

    @Override
    public Property setProperty(String name, String[] values) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Property setProperty(String name, String[] values, int type) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Property setProperty(String name, Value value) throws RepositoryException{
        MockProperty property = (MockProperty) this.properties.get(name);
        if (property == null) {
            property = new MockProperty(name, (MockValue) value, this);
            properties.put(name, property);
        } else {
            property.setValue(value);
        }
        return property;
    }

    @Override
    public Property setProperty(String name, Value value, int type) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Property setProperty(String name, Value[] values) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Property setProperty(String name, Value[] values, int type) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    public void setSession(Session session) {
        this.session = session;
    }

    private void setSessionFrom(MockNode parent) {
        setSession(parent == null ? null : parent.getSession());
    }

    @Override
    public String toString() {
        return "MockNode [primaryType=" + primaryType + ", name="+ name + "]";
    }

    @Override
    public void unlock() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void update(String srcWorkspaceName) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }
}
