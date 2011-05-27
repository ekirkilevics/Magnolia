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

import info.magnolia.cms.core.ItemType;
import info.magnolia.test.mock.MockNodeType;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Binary;
import javax.jcr.Item;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.lock.Lock;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import org.apache.commons.lang.StringUtils;

/**
 * @version $Id$
 */
public class MockNode extends MockItem implements Node {

    private final LinkedHashMap<String, MockNode> children = new LinkedHashMap<String, MockNode>();

    private String identifier;

    private int index = 1;

    private final List<String> mixins = new ArrayList<String>();

    private String primaryType = ItemType.CONTENTNODE.getSystemName();

    private final LinkedHashMap<String, Property> properties = new LinkedHashMap<String, Property>();

    public MockNode(String name) {
        super(name);
    }

    @Override
    public void accept(ItemVisitor visitor) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void addMixin(String mixinName) {
        this.mixins.add(mixinName);
    }

    protected void addNode(MockNode child) {
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
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
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
    public Item getAncestor(int depth) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Version getBaseVersion() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    protected Map<String, MockNode> getChildren() {
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
    public Node getNode(String path) throws PathNotFoundException, RepositoryException {
        Node c;
        if (path.contains("/")) {
            String[] names = StringUtils.split(path, "/");
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
        c = children.get(path);
        if (c == null) {
            throw new PathNotFoundException(getPath() + "/" + path);
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
    public Item getPrimaryItem() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public NodeType getPrimaryNodeType() {
        return new MockNodeType(this.primaryType);
    }

    @Override
    public PropertyIterator getProperties() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public PropertyIterator getProperties(String namePattern) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Property getProperty(String relPath) {
        return properties.get(relPath);
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
    public NodeIterator getSharedSet() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

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
        return primaryType.equals(nodeTypeName);
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
        ((MockNode) getParent()).removeFromChildren(this);
    }

    protected boolean removeFromChildren(Node childNode) {
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

    @Override
    public void setPrimaryType(String primaryType) {
        this.primaryType = primaryType;
    }

    @Override
    public Property setProperty(String name, BigDecimal value) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Property setProperty(String name, Binary value) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Property setProperty(String name, boolean value) {
        return setProperty(name, new MockValue(value));
    }

    @Override
    public Property setProperty(String name, Calendar value) {
        return setProperty(name, new MockValue(value));
    }

    @Override
    public Property setProperty(String name, double value) {
        return setProperty(name, new MockValue(value));
    }

    @Override
    public Property setProperty(String name, InputStream value) {
        return setProperty(name, new MockValue(value));
    }

    @Override
    public Property setProperty(String name, long value) {
        return setProperty(name, new MockValue(value));
    }

    @Override
    public Property setProperty(String name, Node value) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public Property setProperty(String name, String value) {
        return setProperty(name, new MockValue(value));
    }

    @Override
    public Property setProperty(String name, String value, int type) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
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
    public Property setProperty(String name, Value value) {
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

    @Override
    public void unlock() {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public void update(String srcWorkspaceName) {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public String toString() {
        return "MockNode [primaryType=" + primaryType + super.toString() + "]";
    }

    protected boolean removeProperty(String propertyName) {
        Property property = properties.remove(propertyName);
        return property != null;
    }
}
