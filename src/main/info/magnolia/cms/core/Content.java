/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core;

import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.core.util.Access;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.DateComparator;
import info.magnolia.cms.util.Path;
import info.magnolia.cms.util.SequenceComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.1
 */
public class Content extends ContentHandler implements Cloneable {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Content.class);

    private String path;

    private Node rootNode;

    private MetaData metaData;

    /**
     * constructor
     */
    Content() {
    }

    /**
     * constructor to get existing node
     * @param rootNode node to start with
     * @param path absolute (primary) path to this <code>Node</code>
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public Content(Node rootNode, String path, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(rootNode.getPath(), path), Permission.READ);
        this.setPath(path);
        this.setRootNode(rootNode);
        this.setNode(this.rootNode.getNode(this.path));
        this.setAccessManager(manager);
    }

    /**
     * constructor to get existing node
     * @param elem , initialized node object
     */
    public Content(Item elem, AccessManager manager) throws RepositoryException, AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(elem.getPath()), Permission.READ);
        this.setNode((Node) elem);
        this.setPath(this.getHandle());
        this.setAccessManager(manager);
    }

    /**
     * <p>
     * creates contentNode of type <b>contentType </b> contentType must be defined in item type definition of magnolia
     * as well as JCR implementation
     * </p>
     * @param rootNode node to start with
     * @param path absolute (primary) path to this <code>Node</code>
     * @param contentType , JCR node type as configured
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public Content(Node rootNode, String path, String contentType, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(rootNode.getPath(), path), Permission.WRITE);
        this.setPath(path);
        this.setRootNode(rootNode);
        this.node = this.rootNode.addNode(this.path, ItemType.getSystemName(contentType));
        this.setAccessManager(manager);
        this.addMixin(ItemType.getSystemName(ItemType.MIX_VERSIONABLE));
    }

    /**
     * <p>
     * checks if the requested resource is an NodeData (Property)
     * </p>
     * @param contentType of the requested NodeData
     * @return boolean true is the requested content is an NodeData
     */
    public boolean isType(String contentType) {
        try {
            return (this.node.isNodeType(ItemType.getSystemName(contentType)));
        }
        catch (RepositoryException e) {
            log.error(e);
        }
        return false;
    }

    /**
     * <p>
     * bit by bit copy of the current object
     * </p>
     * @return Object cloned object
     * @throws CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    protected void setNode(Node node) {
        this.node = node;
    }

    protected void setRootNode(Node node) {
        this.rootNode = node;
    }

    protected void setPath(String path) {
        this.path = path;
    }

    /**
     * <p>
     * get ContentNode node of the current node with the specified name
     * </p>
     * @param name of the node acting as <code>ContentNode</code>
     * @return <node>ContentNode </node>
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public ContentNode getContentNode(String name) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        ContentNode contentNode = new ContentNode(this.node, name, this.accessManager);
        return contentNode;
    }

    /**
     * <p>
     * create ContentNode node under the current node with the specified name
     * </p>
     * @param name of the node to be created as <code>ContentNode</code>
     * @return newly created <node>ContentNode </node>
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public ContentNode createContentNode(String name) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        ContentNode contentNode = (new ContentNode(this.node, name, true, this.accessManager));
        MetaData metaData = contentNode.getMetaData();
        metaData.setCreationDate();
        return contentNode;
    }

    /**
     * <p>
     * get Content node of the current node with the specified name
     * </p>
     * @param name of the node acting as <code>Content</code>
     * @return <node>Content </node>
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public Content getContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return (new Content(this.node, name, this.accessManager));
    }

    /**
     * <p>
     * create Content node under the current node with the specified name
     * </p>
     * @param name of the node to be created as <code>Content</code>
     * @return newly created <node>Content </node>
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public Content createContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return this.createContent(name, ItemType.getSystemName(ItemType.NT_CONTENT));
    }

    /**
     * <p>
     * create Content node under the current node with the specified name
     * </p>
     * @param name of the node to be created as <code>Content</code>
     * @return newly created <node>Content </node>
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public Content createContent(String name, String contentType) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Content content = (new Content(this.node, name, contentType, this.accessManager));
        MetaData metaData = content.getMetaData();
        metaData.setCreationDate();
        return content;
    }

    /**
     * @return String, template name
     */
    public String getTemplate() {
        return this.getMetaData().getTemplate();
    }

    /**
     * @return String, title
     */
    public String getTitle() {
        return this.getNodeData("title").getString();
    }

    /**
     * <p>
     * get meta data of the current node
     * </p>
     * @return MetaData meta information of the content <code>Node</code>
     */
    public MetaData getMetaData() {
        if (this.metaData == null) {
            this.metaData = new MetaData(this.node, this.accessManager);
        }
        return this.metaData;
    }

    /**
     * <p>
     * get meta data of the current node
     * </p>
     * @return MetaData meta information of the context under the content <code>Node</code>
     */
    public MetaData getMetaData(String context) {
        return new MetaData(this.node, context, this.accessManager);
    }

    /**
     * <p>
     * get top level NodeData
     * </p>
     * @return NodeData requested <code>NodeData</code> object
     */
    public NodeData getNodeData(String name) {
        try {
            return (new NodeData(this.node, name, this.accessManager));
        }
        catch (RepositoryException re) {
            return (new NodeData());
        }
    }

    /**
     * <p>
     * get node name
     * </p>
     * @return String name of the current <code>Node</code>
     */
    public String getName() {
        try {
            return this.node.getName();
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * <p>
     * create top level NodeData object
     * </p>
     * @param name to be created
     * @return NodeData requested <code>NodeData</code> object
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public NodeData createNodeData(String name) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        return (new NodeData(this.node, name, true, this.accessManager));
    }

    /**
     * <p>
     * create NodeData with the given value and type
     * </p>
     * @param name to be created
     * @param value to be set initially
     * @param type propertyType
     * @return NodeData requested <code>NodeData</code> object
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public NodeData createNodeData(String name, Value value, int type) throws PathNotFoundException,
        RepositoryException, AccessDeniedException {
        return (new NodeData(this.node, name, value, this.accessManager));
    }

    /**
     * <p>
     * delete NodeData with the specified name todo remove all dependencies of this method, there should be no
     * difference between delete(name) and deleteNodeData(name)
     * </p>
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public void deleteNodeData(String name) throws PathNotFoundException, RepositoryException {
        this.delete(name);
    }

    /**
     * <p>
     * delete Content node with the specified name from the current node todo remove all dependencies of this method,
     * there should be no difference between delete(name) and deleteContent(name)
     * </p>
     * @param name of the Content to be deleted
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void deleteContent(String name) throws PathNotFoundException, RepositoryException {
        this.delete(name);
    }

    /**
     * <p>
     * delete ContentNode with the specified name from the current node todo remove all dependencies of this method,
     * there should be no difference between delete(name) and deleteContentNode(name)
     * </p>
     * @param name of the ContentNode to be deleted
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void deleteContentNode(String name) throws PathNotFoundException, RepositoryException {
        this.delete(name);
    }

    /**
     * <p>
     * you could call this method anytime to update working page properties - Modification date & Author ID
     * </p>
     */
    public void updateMetaData(HttpServletRequest request) throws RepositoryException, AccessDeniedException {
        MetaData md = this.getMetaData();
        md.setModificationDate();
        md.setAuthorId(Authenticator.getUserId(request));
        md = null;
    }

    /**
     * <p>
     * gets a Collection containing all child nodes at the current level+1 level <br>
     * </p>
     * @return Collection of content nodes
     */
    public Collection getChildren() {
        return this.getChildren(ItemType.NT_CONTENT);
    }

    /**
     * <p>
     * get collection of specified content type <br>
     * use: <br>
     * ItemType.NT_CONTENT to get sub pages ItemType.NT_CONTENTNODE to get sub content nodes (paragraphs)
     * ItemType.NT_NODEDATA to get node data (properties) <b>else </b> YOUR_CUSTOM_TYPE as registered
     * </p>
     * @param contentType
     * @return Collection of content nodes
     * @deprecated instead use getChildren(String)
     */
    public Collection getChildren(int contentType) {
        String type = "";
        switch (contentType) {
            case ItemType.MAGNOLIA_PAGE:
                type = ItemType.NT_CONTENT;
                break;
            case ItemType.MAGNOLIA_CONTENT_NODE:
                type = ItemType.NT_CONTENTNODE;
                break;
            case ItemType.MAGNOLIA_NODE_DATA:
                type = ItemType.NT_NODEDATA;
                break;
            default:
                log.error("Un-Supported content type - " + contentType);
        }
        return this.getChildren(type, ContentHandler.SORT_BY_SEQUENCE);
    }

    /**
     * <p>
     * get collection of specified content type <br>
     * use: <br>
     * ItemType.NT_CONTENT to get sub pages ItemType.NT_CONTENTNODE to get sub content nodes (paragraphs)
     * ItemType.NT_NODEDATA to get node data (properties) <b>else </b> YOUR_CUSTOM_TYPE as registered
     * </p>
     * @param contentType
     * @return Collection of content nodes
     */
    public Collection getChildren(String contentType) {
        return this.getChildren(contentType, ContentHandler.SORT_BY_SEQUENCE);
    }

    /**
     * <p>
     * get collection of specified content type <br>
     * use: <br>
     * ItemType.NT_CONTENT to get sub pages ItemType.NT_CONTENTNODE to get sub content nodes (paragraphs)
     * ItemType.NT_NODEDATA to get node data (properties) <b>else </b> YOUR_CUSTOM_TYPE as registered
     * </p>
     * @param contentType
     * @param sortCriteria which can be either ContentHandler.SORT_BY_SEQUENCE , ContentHandler.SORT_BY_DATE or
     * ContentHandler.SORT_BY_NAME
     * @return Collection of content nodes
     */
    public Collection getChildren(String contentType, String sortCriteria) {
        Collection children = new ArrayList();
        try {
            if (contentType.equalsIgnoreCase(ItemType.NT_CONTENT)) {
                children = this.getChildPages();
                children = sort(children, sortCriteria);
            }
            else if (contentType.equalsIgnoreCase(ItemType.NT_CONTENTNODE)) {
                children = this.getChildContentNodes();
                children = sort(children, sortCriteria);
            }
            else if (contentType.equalsIgnoreCase(ItemType.NT_NODEDATA)) {
                children = this.getProperties();
            }
            else {
                children = this.getChildContent(contentType);
                children = sort(children, sortCriteria);
            }
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return children;
    }

    private Collection sort(Collection collection, String sortCriteria) {
        if (sortCriteria == null) {
            return collection;
        }
        if (sortCriteria.equals(ContentHandler.SORT_BY_DATE)) {
            return sortByDate(collection);
        }
        else if (sortCriteria.equals(ContentHandler.SORT_BY_SEQUENCE)) {
            return sortBySequence(collection);
        }
        return collection;
    }

    private Collection getChildPages() throws RepositoryException {
        return this.getChildContent(ItemType.NT_CONTENT);
    }

    private Collection getChildContent(String contentType) throws RepositoryException {
        Collection children = new ArrayList();
        NodeIterator nodeIterator = this.node.getNodes();
        if (nodeIterator == null) {
            return children;
        }
        while (nodeIterator.hasNext()) {
            Node subNode = (Node) nodeIterator.next();
            try {
                if (subNode.isNodeType(ItemType.getSystemName(contentType))) {
                    children.add(new Content(subNode, this.accessManager));
                }
            }
            catch (PathNotFoundException e) {
                log.error(e);
            }
            catch (AccessDeniedException e) {
                // ignore, simply wont add content in a list
            }
        }
        return children;
    }

    /**
     * @throws RepositoryException
     */
    private Collection getChildContentNodes() throws RepositoryException {
        Collection children = new ArrayList();
        NodeIterator nodeIterator = this.node.getNodes();
        if (nodeIterator == null) {
            return children;
        }
        while (nodeIterator.hasNext()) {
            Node subNode = (Node) nodeIterator.next();
            try {
                if (subNode.isNodeType(ItemType.getSystemName(ItemType.NT_CONTENTNODE))) {
                    children.add(new ContentNode(subNode, this.accessManager));
                }
            }
            catch (PathNotFoundException e) {
                log.error(e);
            }
            catch (AccessDeniedException e) {
                // ignore, simply wont add content in a list
            }
        }
        return children;
    }

    private Collection getProperties() throws RepositoryException {
        Collection children = new ArrayList();
        NodeIterator nodeIterator = this.node.getNodes();
        if (nodeIterator == null) {
            return children;
        }
        while (nodeIterator.hasNext()) {
            Node subNode = (Node) nodeIterator.next();
            try {
                if (subNode.isNodeType(ItemType.getSystemName(ItemType.NT_NODEDATA))) {
                    children.add(new NodeData(subNode, this.accessManager));
                }
            }
            catch (PathNotFoundException e) {
                log.error(e);
            }
            catch (AccessDeniedException e) {
                // ignore, simply wont add content in a list
            }
        }
        return children;
    }

    /**
     * @return Boolean, if sub node(s) exists
     */
    public boolean hasChildren() {
        return (this.getChildren(ItemType.NT_CONTENT).size() > 0);
    }

    /**
     * @return Boolean, if sub <code>collectionType</code> exists
     * @deprecated use hasChildren(String) instead
     */
    public boolean hasChildren(int collectionType) {
        return (this.getChildren(collectionType).size() > 0);
    }

    /**
     * @return Boolean, if sub <code>collectionType</code> exists
     */
    public boolean hasChildren(String contentType) {
        return (this.getChildren(contentType).size() > 0);
    }

    /**
     * <p>
     * gets a Collection containing all clild nodes at the current level+1 level
     * </p>
     * @return Collection of content nodes
     */
    public Collection sortByDate(Collection c) {
        try {
            if (c == null) {
                return c;
            }
            Collections.sort((List) c, new DateComparator());
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return c;
    }

    /**
     * <p>
     * gets a Collection containing all clild nodes at the current level+1 level
     * </p>
     * @return Collection of content nodes
     */
    public Collection sortBySequence(Collection c) {
        try {
            if (c == null) {
                return c;
            }
            Collections.sort((List) c, new SequenceComparator());
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return c;
    }

}
