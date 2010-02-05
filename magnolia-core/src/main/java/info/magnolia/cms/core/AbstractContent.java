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
package info.magnolia.cms.core;

import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.NodeTypeFilter;
import info.magnolia.context.MgnlContext;
import info.magnolia.logging.AuditLoggingUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.Workspace;

import org.apache.commons.lang.StringUtils;

/**
 * A base class by implementing some default behavior.
 * A subclass must carefully implement {@link #newNodeDataInstance(String, int, boolean)},
 * {@link #getChildren(info.magnolia.cms.core.Content.ContentFilter, String, java.util.Comparator)} and
 * {@link #getNodeDataCollection(String)}.
 *
 * @author pbaerfuss
 * @version $Id$
 *
 */
public abstract class AbstractContent extends ContentHandler implements Content {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractContent.class);

    public Content createContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return createContent(name, ItemType.CONTENT);
    }

    public Content createContent(String name, ItemType contentType) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return createContent(name, contentType.getSystemName());
    }

    public NodeData createNodeData(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return setNodeData(name, "");
    }

    public NodeData createNodeData(String name, Value value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return setNodeData(name, value);
    }

    /**
     * @deprecated
     */
    public NodeData createNodeData(String name, Value[] value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return setNodeData(name, value);
    }

    /**
     * @deprecated 
     */
    public NodeData createNodeData(String name, int type) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        // set some default values to create the property
        switch (type) {
            case PropertyType.STRING:
                return setNodeData(name, StringUtils.EMPTY);
            case PropertyType.BOOLEAN:
                return setNodeData(name, Boolean.FALSE);
            case PropertyType.DATE:
                return setNodeData(name, Calendar.getInstance());
            case PropertyType.LONG:
                return setNodeData(name, new Long(0));
            case PropertyType.DOUBLE:
                return setNodeData(name, new Double(0.0));
            default:
                return newNodeDataInstance(name, type, true);
        }
    }
    
    /**
     * @deprecated
     */
    public NodeData createNodeData(String name, Object valueObj) throws RepositoryException {
        return setNodeData(name, valueObj);
    }
    
    /**
     * {@inheritDoc}
     * Delegates to {@link #newNodeDataInstance(String, int, boolean)} by setting the type to PropertyType.UNDEFINED. A subclass has to handle this by trying to determine the type if the node data exists. The reason for this is that implementations want to instantiate different node data classes per type
     */
    public NodeData getNodeData(String name) {
        try {
            // will try to determine the type if the node data exists, otherwise an non-mutable node data will be returned
            return newNodeDataInstance(name, PropertyType.UNDEFINED, false);
        }
        catch(RepositoryException e){
            throw new IllegalStateException("Can't instantiate node data " + name + " on node " + toString(), e);
        }
    }
    
    /**
     * As defined in {@link Content#getNodeData(String)} this method always returns a node data object. If the type is {@link PropertyType#UNDEFINED} the implementation should check if the node data exists and determine the type to use.
     * 
     * @param createIfNotExisting if false an empty non-mutable node data will be returned if the node data doesn't exist otherwise a mutable nodedata object is returned (depending on the type)
     */
    abstract public NodeData newNodeDataInstance(String name, int type, boolean createIfNotExisting) throws AccessDeniedException, RepositoryException;

    /**
     * Delegates to {@link NodeData#isExist()}.
     */
    public boolean hasNodeData(String name) throws RepositoryException {
        return getNodeData(name).isExist();
    }

    public NodeData setNodeData(String name, Value value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        NodeData nodeData = newNodeDataInstance(name, value.getType(), true);
        nodeData.setValue(value);
        return nodeData;
    }

    public NodeData setNodeData(String name, Value[] value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if(value.length == 0){
            throw new IllegalArgumentException("Value array can't be empty");
        }
        NodeData nodeData = newNodeDataInstance(name, value[0].getType(), true);
        nodeData.setValue(value);
        return nodeData;
    }
    
    public NodeData setNodeData(String name, boolean value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        NodeData nodeData = newNodeDataInstance(name, PropertyType.BOOLEAN, true);
        nodeData.setValue(value);
        return nodeData;
    }
    
    public NodeData setNodeData(String name, long value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        NodeData nodeData = newNodeDataInstance(name, PropertyType.LONG, true);
        nodeData.setValue(value);
        return nodeData;
    }
    
    public NodeData setNodeData(String name, double value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        NodeData nodeData = newNodeDataInstance(name, PropertyType.DOUBLE, true);
        nodeData.setValue(value);
        return nodeData;
    }
    
    public NodeData setNodeData(String name, String value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        NodeData nodeData = newNodeDataInstance(name, PropertyType.STRING, true);
        nodeData.setValue(value);
        return nodeData;
    }
    
    public NodeData setNodeData(String name, InputStream value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        NodeData nodeData = newNodeDataInstance(name, PropertyType.BINARY, true);
        nodeData.setValue(value);
        return nodeData;
    }
    
    public NodeData setNodeData(String name, Calendar value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        NodeData nodeData = newNodeDataInstance(name, PropertyType.DATE, true);
        nodeData.setValue(value);
        return nodeData;
    }
    
    public NodeData setNodeData(String name, Content value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        NodeData nodeData = newNodeDataInstance(name, PropertyType.STRING, true);
        nodeData.setValue(value.getUUID());
        return nodeData;
    }
    
    /**
     * Uses the {@link NodeDataUtil} to create and set the node data based on the object type.
     */
    public NodeData setNodeData(String name, Object value) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        NodeData nodeData = newNodeDataInstance(name, NodeDataUtil.getJCRPropertyType(value), true);
        NodeDataUtil.setValue(nodeData, value);
        return nodeData;
    }
    
    public void deleteNodeData(String name) throws PathNotFoundException, RepositoryException {
        getNodeData(name).delete();
    }

    /**
     * {@inheritDoc}
     * Delegates to {@link #getChildren(ItemType)} passing the current node's type.
     */
    public Collection<Content> getChildren() {
        String type = null;
    
        try {
            type = this.getNodeTypeName();
        }
        catch (RepositoryException re) {
            throw new RuntimeException("Can't read type of node [" + toString() + "]", re);
    
        }
        // fix all getChildren calls from the root node
        if ("rep:root".equalsIgnoreCase(type)) { //$NON-NLS-1$
            type = ItemType.CONTENT.getSystemName();
        }
        // --------------------------------------------------
        return this.getChildren(type);
    }

    /**
     * {@inheritDoc}
     * Delegates to {@link #getChildren(info.magnolia.cms.core.Content.ContentFilter, java.util.Comparator).
     */    
    public Collection<Content> getChildren(ContentFilter filter) {
        return getChildren(filter, null);
    }

    /**
     * {@inheritDoc}
     * Delegates to {@link #getChildren(info.magnolia.cms.core.Content.ContentFilter, java.util.Comparator).
     */    
    public Collection<Content> getChildren(ItemType itemType) {
        return getChildren(new NodeTypeFilter(itemType), null);
    }

    /**
     * {@inheritDoc}
     * Delegates to {@link #getChildren(info.magnolia.cms.core.Content.ContentFilter, java.util.Comparator).
     */
    public Collection<Content> getChildren(String contentType) {
        return getChildren(new NodeTypeFilter(contentType), null);
    }

    /**
     * {@inheritDoc}
     * Delegates to {@link #getChildren(info.magnolia.cms.core.Content.ContentFilter, String, java.util.Comparator)}
     */    
    public Collection<Content> getChildren(final String contentType, final String namePattern) {
        return getChildren(new NodeTypeFilter(contentType), namePattern, null);
    }

    public Collection<Content> getChildren(ContentFilter filter, Comparator<Content> orderCriteria) {
        return getChildren(filter, null, orderCriteria);
    }

    /**
     * @param namePattern ignored if null.
     */
    abstract public Collection<Content> getChildren(ContentFilter filter, String namePattern, Comparator<Content> orderCriteria);

    /**
     * @deprecated
     */
    public Content getChildByName(String namePattern) {
        Collection<Content> children = getChildren("nt:base", namePattern);;
        if (!children.isEmpty()) {
            return children.iterator().next();
        }
        return null;
    }

    public Collection<NodeData> getNodeDataCollection() {
        return getNodeDataCollection(null);
    }

    protected Collection<NodeData> getBinaryNodeDatas(String namePattern) throws RepositoryException {
        Collection<NodeData> nodeDatas = new ArrayList<NodeData>();
        Collection<Content> binaryNodes = getChildren(ItemType.NT_RESOURCE, namePattern);
        for (Content binaryNode : binaryNodes) {
            nodeDatas.add(newNodeDataInstance(binaryNode.getName(), PropertyType.BINARY, false));
        }
        return nodeDatas;
    }


    public boolean hasChildren() {
        return (this.getChildren().size() > 0);
    }

    public boolean hasChildren(String contentType) {
        return (this.getChildren(contentType).size() > 0);
    }
    
    public void delete(String path) throws RepositoryException {
        if(isNodeData(path)){
            deleteNodeData(path);
        }
        else{
            getContent(path).delete();
        }
    }

    public boolean isNodeData(String path) throws AccessDeniedException, RepositoryException {
        return hasNodeData(path);
    }

    public String getTemplate() {
        return this.getMetaData().getTemplate();
    }

    public String getTitle() {
        return I18nContentSupportFactory.getI18nSupport().getNodeData(this, "title").getString();
    }

    public void updateMetaData() throws RepositoryException, AccessDeniedException {
        MetaData md = this.getMetaData();
        md.setModificationDate();
        md.setAuthorId(MgnlContext.getUser().getName());
        AuditLoggingUtil.log( AuditLoggingUtil.ACTION_MODIFY, hierarchyManager.getName(),this.getItemType(), getHandle());
    }

    public boolean isGranted(long permissions) {
        return hierarchyManager.getAccessManager().isGranted(getHandle(), permissions);
    }

    public Workspace getWorkspace() throws RepositoryException {
        return getHierarchyManager().getWorkspace();
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getHierarchyManager() == null ? "null" : getHierarchyManager().getName());
        buffer.append(":" + getHandle());
        String type = "";
        try {
            type = getItemType().getSystemName();
        }
        catch (RepositoryException e) {
            // ignore
        }
        buffer.append("[");
        buffer.append(type);
        buffer.append("]");

        return buffer.toString();
    }


}
