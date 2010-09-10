/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.tree.container;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.LifeTimeJCRSessionUtil;
import info.magnolia.module.admincentral.tree.TreeColumn;
import info.magnolia.module.admincentral.tree.TreeDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Buffered;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;


/**
 * Implementation of a JCR backed Vaadin <code>Container</code>.
 *
 * All ID's must be Strings to comply with JCR identifiers.
 *
 * @author daniellipp
 * @version $Id$
 */
public class JcrContainer implements Serializable, Container.Hierarchical, Buffered {

    private static final Logger log = LoggerFactory.getLogger(JcrContainer.class);

    private static final long serialVersionUID = 240035255907683559L;

    protected Map<String, PropertyDefinition> containerProperties = new LinkedHashMap<String, PropertyDefinition>();

    private final TreeDefinition definition;

    protected HashMap<String, NodeItem> nodeItems = new HashMap<String, NodeItem>();

    private final String[] roots;

    /**
     * Create JCR container with provided definition and root.
     * Note that in this case (a single root) this root will not be added to the container on level 0 but its children.
     *
     * @param definition the definition of the tree
     * @param root the single root
     */
    public JcrContainer(TreeDefinition definition, String root) {
        this(definition, new String[]{root});
    }

    public JcrContainer(TreeDefinition definition, String[] roots) {
        this.definition = definition;
        this.roots = roots;
    }

    public boolean addContainerProperty(Object propertyId, Class< ? > type,
            Object defaultValue) {
        assertIsString(propertyId);
        containerProperties.put((String) propertyId, new PropertyDefinition(
                (String) propertyId, type, defaultValue));
        return false;
    }

    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "JCR does not support empty Items");
    }

    public Item addItem(Object itemId) {
        assertIsString(itemId);
        try {
            if (!containsId(itemId)
                    && !getHierarchyManager().isExist((String) itemId)) {
                String relativePath = getRelativePathToRoot(itemId);
                Content content = getHierarchyManager().getRoot().createContent(relativePath);

                // not sure whether this is needed like that...
                for (TreeColumn treeColumn : this.definition.getColumns()) {
                    getContainerProperty(itemId, treeColumn.getLabel()).setValue(treeColumn.getValue(content));
                }

            }
            return getItem(itemId);
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean areChildrenAllowed(Object itemId) {
        return true;
    }

    protected void assertIsString(Object itemId) {
        if (!(itemId instanceof String)) {
            throw new IllegalArgumentException("JCR id's must be String");
        }
    }

    public void commit() throws SourceException, InvalidValueException {
        try {
            getHierarchyManager().save();
        }
        catch (RepositoryException e) {
            throw new SourceException(this);
        }
    }

    public boolean containsId(Object itemId) {
        assertIsString(itemId);
        return nodeItems.containsKey(itemId);
    }

    public void discard() throws SourceException {
        // TODO Not sure how to revert
    }

    public Collection<String> getChildren(Object itemId) {
        ArrayList<String> children = new ArrayList<String>();
        try {
            for (Content node : getNodeItem(itemId).getChildren()) {
                children.add(getNodeItem(node.getHandle()).getItemId());
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return children;
    }

    public Property getContainerProperty(Object itemId, Object propertyId) {
        assertIsString(itemId);
        assertIsString(propertyId);
        try {
            return new NodeProperty(getHierarchyManager().getContent((String) itemId),
                    (String) propertyId, definition);
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<String> getContainerPropertyIds() {
        return containerProperties.keySet();
    }

    /**
     *
     * @return the Session associated with this container.
     * @throws RepositoryException
     */
    public HierarchyManager getHierarchyManager() throws RepositoryException {
        return LifeTimeJCRSessionUtil.getHierarchyManager(definition.getRepository());

    }

    public Item getItem(Object itemId) {
        assertIsString(itemId);
        NodeItem item;
        try {
            if (nodeItems.containsKey(itemId)) {
                log.debug("found in nodeitems");
                item = nodeItems.get(itemId);
            }
            else if (getHierarchyManager().isExist((String) itemId)) {
                log.info("Found item with id {} in repository.", itemId);

                Content content = getHierarchyManager().getContent((String) itemId);
                item = new NodeItem(content, definition);

                // load all the parents
                if (item.getLevel() > 1) {
                    log.debug("parent check: depth=" + item.getLevel()
                            + " parentpath=" + item.getParent().getHandle());
                    getItem(item.getParent().getHandle());
                }
                nodeItems.put((String) itemId, item);
            }
            else {
                throw new IllegalArgumentException("itemId not found");
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return item;
    }

    public Collection<String> getItemIds() {
        final Collection<String> col = new ArrayList<String>();
        final Content root;
        try {
            root = getHierarchyManager().getRoot();
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }

        Collection<Content> children = root.getChildren();
        for (Iterator<Content> iterator2 = children.iterator(); iterator2.hasNext();) {
            col.add(iterator2.next().getHandle());
        }

        return Collections.unmodifiableCollection(col);
    }

    /**
     * Casts the getItem() call to return a NodeItem.
     *
     * @param itemId
     * @return NodeItem
     */
    public NodeItem getNodeItem(Object itemId) {
        return (NodeItem) getItem(itemId);
    }

    /*
         * This method is required by the interface. Unfortunately it's badly named as it has to return the id of the parent not the parent itself!
         */
    public Object getParent(Object itemId) {
        return getParentId(itemId);
    }

    public Object getParentId(Object itemId) {
        NodeItem item = getNodeItem(itemId);
        try {
            if (item.getLevel() > 0) {
                return item.getParent().getHandle();
            }
            else {
                return null;
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getRelativePathToRoot(Object itemId) {
        String path = (String) itemId;
        String slash = "/";
        if (!path.startsWith(slash)) {
            throw new IllegalArgumentException("path not relative to target");
        }
        return path.substring(slash.length());
    }

    public Class< ? > getType(Object propertyId) {
        return containerProperties.get(propertyId).getType();
    }

    public boolean hasChildren(Object itemId) {
        return getNodeItem(itemId).hasChildren();
    }

    public boolean isModified() {
        try {
            return getHierarchyManager().hasPendingChanges();
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isReadThrough() {
        return false;
    }

    public boolean isRoot(Object itemId) {
        if (!(itemId instanceof String)) {
            return false;
        }
        for (int i = 0; i < roots.length; i++) {
            if (roots[i].equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWriteThrough() {
        return false;
    }

    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Use removeItem() at the Node level");
    }

    protected void removeChildItems(NodeItem item) throws RepositoryException {
        for (String childId : nodeItems.keySet()) {
            NodeItem child = nodeItems.get(childId);
            for (int depth = 1; depth < child.getLevel(); depth++) {
                if (child.getAncestor(depth).equals(item)) {
                    nodeItems.remove(childId);
                }
            }
        }
    }

    public boolean removeContainerProperty(Object propertyId)
            throws UnsupportedOperationException {
        containerProperties.remove(propertyId);
        return true;
    }

    public boolean removeItem(Object itemId)
            throws UnsupportedOperationException {
        assertIsString(itemId);
        try {
            removeChildItems(nodeItems.get(itemId));
            getNodeItem(itemId).delete();
            nodeItems.remove(itemId);
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public Collection<String> rootItemIds() {
        // in single root case we use children as in FilesystemContainer
        if (roots.length == 1) {
            return getChildren(roots[0]);
        }
        return Collections.unmodifiableCollection(Arrays.asList(roots));
    }

    public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Cannot override Node Definition");
    }

    public boolean setParent(Object itemId, Object newParentId)
            throws UnsupportedOperationException {
        try {
            NodeItem item = getNodeItem(itemId);
            NodeItem newparent = getNodeItem(itemId);
            String newid = newparent.getHandle() + item.getName();
            nodeItems.remove(itemId);
            removeChildItems(item);
            getHierarchyManager().moveTo(item.getHandle(), newid);
            getNodeItem(newid);
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public void setReadThrough(boolean readThrough) throws SourceException {
        throw new SourceException(this);
    }

    public void setWriteThrough(boolean writeThrough) throws SourceException,
            InvalidValueException {
        throw new SourceException(this);
    }

    public int size() {
        return nodeItems.size();
    }

}
