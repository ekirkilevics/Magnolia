/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.module.admincentral.tree.container;

import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.data.Buffered;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.jcr.JCRUtil;
import info.magnolia.module.admincentral.tree.TreeColumn;
import info.magnolia.module.admincentral.tree.TreeDefinition;
import info.magnolia.module.admincentral.tree.TreeItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Implementation of a JCR backed Vaadin <code>Container</code>.
 *
 * All ID's must be Strings to comply with JCR identifiers.
 *
 * @author daniellipp
 * @version $Id$
 */
public class JcrContainer implements Serializable, Container.Hierarchical, Buffered {

    /**
     * Keeps already used Resource in order to save resources/not create new Resource for every
     * item.
     */
    private static ConcurrentHashMap<String, Resource> itemIcons = new ConcurrentHashMap<String, Resource>();

    private static final Logger log = LoggerFactory.getLogger(JcrContainer.class);

    private static final long serialVersionUID = 240035255907683559L;

    protected Map<String, PropertyDefinition> containerProperties = new LinkedHashMap<String, PropertyDefinition>();

    private final TreeDefinition definition;

    protected HashMap<String, NodeItem> nodeItems = new HashMap<String, NodeItem>();

    protected JcrSessionProvider provider;

    private final String[] roots;

    private TreeTable tree;

    /**
     * Create JCR container with provided definition and root. Note that in this case (a single
     * root) this root will not be added to the container on level 0 but its children.
     *
     * @param tree TreeTable to set itemIcon on (can only be set on TreeTable)
     * @param definition the definition of the tree
     * @param root the single root
     */
    public JcrContainer(TreeTable tree, TreeDefinition definition, String root) {
        this(tree, definition, new String[]{root});
    }

    public JcrContainer(TreeTable tree, TreeDefinition definition, String[] roots) {
        this.provider = new SessionProviderImpl(definition.getName());
        this.definition = definition;
        this.roots = roots;
        this.tree = tree;
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
        String fullPath = (String) itemId;
        try {
            if (!containsId(itemId)
                    && !getSession().nodeExists(fullPath)) {
                String itemPath = JCRUtil.getItemIdWithoutPath(fullPath);
                String parentPath = JCRUtil.getPathWithoutItemId(fullPath);
                Node parent = getSession().getNode(parentPath);

                Node newNode = parent.addNode(itemPath);
                // not sure whether this is needed like that...
                for (TreeColumn< ? > treeColumn : this.definition.getColumns()) {
                    getContainerProperty(itemId, treeColumn.getLabel()).setValue(treeColumn.getValue(newNode));
                }

            }
            return getItem(itemId);
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    // is used to indicate in the TreeTable whether there're children or not
    public boolean areChildrenAllowed(Object itemId) {
        return hasChildren(itemId);
    }

    protected void assertIsString(Object itemId) {
        if (!(itemId instanceof String)) {
            throw new IllegalArgumentException("JCR id's must be String");
        }
    }

    public void commit() throws SourceException, InvalidValueException {
        try {
            getSession().save();
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

    public Collection<String> getChildren(Object parentId) {
        log.info("Now retrieving children id's for {}", parentId);
        ArrayList<String> children = new ArrayList<String>();
        try {
            NodeItem parent = getNodeItem(parentId);
            NodeIterator iterator = parent.getNodes();
            Node node = null;
            while (iterator.hasNext()) {
                node = iterator.nextNode();
                children.add(getNodeItem(node.getPath()).getItemId());
            }

            PropertyIterator properties = parent.getProperties();
            javax.jcr.Property property;
            while (properties.hasNext()) {
                property = (javax.jcr.Property) properties.next();
                log.info("Found " + property.toString());
                // TODO: treat properties as well!
            //    children.add(getNodePropertyItem())
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
            return new NodeProperty(getSession().getNode((String) itemId), definition,
                    (String) propertyId);
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<String> getContainerPropertyIds() {
        return containerProperties.keySet();
    }

    public Item getItem(Object itemId) {
        assertIsString(itemId);
        NodeItem item;
        try {
            if (nodeItems.containsKey(itemId)) {
                item = nodeItems.get(itemId);
            }
            else if (getSession().nodeExists((String) itemId)) {
                log.info("Retrieving item with id {} from repository.", itemId);

                Node content = getSession().getNode((String) itemId);
                item = new NodeItem(content, provider);

                // load all the parents
                if (item.getDepth() > 1) {
                    getItem(item.getParent().getPath());
                }
                nodeItems.put((String) itemId, item);
                // TODO: simplify!
                for (TreeItemType type : definition.getItemTypes()) {
                    if (item.isNodeType(type.getItemType())) {
                        log.info("Item has ItemType {}", type.getItemType());
                        tree.setItemIcon(itemId, getItemIconFor(type.getIcon()));
                    }
                }
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

    private Resource getItemIconFor(String pathToIcon) {
        if (!itemIcons.containsKey(pathToIcon)) {
            // check if this path starts or not with a /
            String tmp = MgnlContext.getContextPath()
                + (!pathToIcon.startsWith(JCRUtil.PATH_SEPARATOR) ? JCRUtil.PATH_SEPARATOR + pathToIcon : pathToIcon);
            itemIcons.put(pathToIcon, new ExternalResource(tmp));
        }
        return itemIcons.get(pathToIcon);
    }

    public Collection<String> getItemIds() {
        final Collection<String> col = new ArrayList<String>();
        try {
            Node root = getSession().getRootNode();
            NodeIterator childIterator = root.getNodes();
            while (childIterator.hasNext()) {
                col.add(((Node) childIterator.next()).getPath());
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
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
            return (item.getDepth() > 0) ? item.getParent().getPath() : null;
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return the Session associated with this container.
     * @throws RepositoryException
     */
    public Session getSession() throws RepositoryException {
        return provider.getSession();
    }

    public Class< ? > getType(Object propertyId) {
        return containerProperties.get(propertyId).getType();
    }

    public boolean hasChildren(Object itemId) {
        try {
            return getNodeItem(itemId).hasNodes();
        }
        catch (RepositoryException e) {
            log.warn(e.getLocalizedMessage());
            return false;
        }
    }

    public boolean isModified() {
        try {
            return getSession().hasPendingChanges();
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
            for (int depth = 1; depth < child.getDepth(); depth++) {
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
            getNodeItem(itemId).remove();
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

    public boolean setParent(Object absoluteItemPath, Object newRelativePath)
            throws UnsupportedOperationException {
        try {
            // next line is just required to get the itemIcon properly set...
            // getNodeItem(absoluteItemPath);
            String newid = (String) newRelativePath + JCRUtil.PATH_SEPARATOR + JCRUtil.getItemIdWithoutPath((String)absoluteItemPath);
            nodeItems.remove(absoluteItemPath);

            // TODO: all child paths have to be adapted!
            // removeChildItems(item);
            getSession().move((String) absoluteItemPath, newid);
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
