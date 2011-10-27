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
package info.magnolia.templating.functions;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.SiblingsHelper;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.wrapper.InheritanceNodeWrapper;
import info.magnolia.link.LinkUtil;
import info.magnolia.objectfactory.Components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * This is an object exposing a couple of methods useful for templates; it's exposed in templates as "cmsfn".
 *
 * @version $Id$
 */
public class TemplatingFunctions {

    public Node asJCRNode(ContentMap contentMap) {
        return contentMap == null ? null : contentMap.getJCRNode();
    }

    public ContentMap asContentMap(Node content) {
        return content == null ? null : new ContentMap(content);
    }

    /**
     * Create link for the Node identified by nodeIdentifier in the specified workspace.
     */
    public String link(String workspace, String nodeIdentifier) {
        try {
            return LinkUtil.createLink(workspace, nodeIdentifier);
        } catch (RepositoryException e) {
            return null;
        }
    }

    /**
     * FIXME Add a LinkUtil.createLink(Property property).... Dirty Hack
     */
    public String link(Property property) {
        try {
            Node parentNode = null;
            String propertyName = null;
            if (property.getType() == PropertyType.BINARY) {
                parentNode = property.getParent().getParent();
                propertyName = property.getParent().getName();
            } else {
                parentNode = property.getParent();
                propertyName = property.getName();
            }
            NodeData equivNodeData = ContentUtil.asContent(parentNode).getNodeData(propertyName);
            return LinkUtil.createLink(equivNodeData);
        } catch (Exception e) {
            return null;
        }
    }

    // TODO fgrilli: LinkUtil needs to be Node capable and not only Content. Switch to node based impl when SCRUM-242
    // will be done.
    public String link(Node content) {
        return content == null ? null : LinkUtil.createLink(ContentUtil.asContent(content));
    }

    public String link(ContentMap contentMap) throws RepositoryException {
        return contentMap == null ? null : this.link(asJCRNode(contentMap));
    }

    public List<Node> children(Node content) throws RepositoryException {
        return content == null ? null : asNodeList(NodeUtil.getNodes(content, NodeUtil.EXCLUDE_META_DATA_FILTER));
    }

    public List<Node> children(Node content, String nodeTypeName) throws RepositoryException {
        return content == null ? null : asNodeList(NodeUtil.getNodes(content, nodeTypeName));
    }

    // TODO fgrilli: should we unwrap children?
    protected List<Node> asNodeList(Iterable<Node> nodes) {
        List<Node> childList = new ArrayList<Node>();
        for (Node child : nodes) {
            childList.add(child);
        }
        return childList;
    }

    public List<ContentMap> children(ContentMap content) throws RepositoryException {
        return content == null ? null : asContentMapList(NodeUtil.getNodes(asJCRNode(content),
                NodeUtil.EXCLUDE_META_DATA_FILTER));
    }

    public List<ContentMap> children(ContentMap content, String nodeTypeName) throws RepositoryException {
        return content == null ? null : asContentMapList(NodeUtil.getNodes(asJCRNode(content), nodeTypeName));
    }

    // TODO fgrilli: should we unwrap children?
    protected List<ContentMap> asContentMapList(Iterable<Node> nodes) {
        List<ContentMap> childList = new ArrayList<ContentMap>();
        for (Node child : nodes) {
            childList.add(new ContentMap(child));
        }
        return childList;
    }

    public ContentMap parent(ContentMap contentMap) throws RepositoryException {
        return contentMap == null ? null : asContentMap(this.parent(contentMap.getJCRNode()));
    }

    public ContentMap parent(ContentMap contentMap, String nodeTypeName) throws RepositoryException {
        return contentMap == null ? null : asContentMap(this.parent(contentMap.getJCRNode(), nodeTypeName));
    }

    /**
     * Returns the page node of the passed node. If the passed Node is a page, the passed node will be returned. If the
     * passed Node has no parent page at all, null is returned.
     *
     * FIXME cringele model: test missing.
     *
     * @param content
     * @return returns the page node of the passed content node.
     * @throws RepositoryException
     */
    public Node page(Node content) throws RepositoryException {
        if (content.isNodeType(MgnlNodeType.NT_PAGE)) {
            return content;
        }
        return parent(content, MgnlNodeType.NT_PAGE);
    }

    public ContentMap root(ContentMap contentMap) throws RepositoryException {
        return contentMap == null ? null : asContentMap(this.root(contentMap.getJCRNode()));
    }

    public ContentMap root(ContentMap contentMap, String nodeTypeName) throws RepositoryException {
        return contentMap == null ? null : asContentMap(this.root(contentMap.getJCRNode(), nodeTypeName));
    }

    public Node parent(Node content) throws RepositoryException {
        return this.parent(content, null);
    }

    public Node parent(Node content, String nodeTypeName) throws RepositoryException {
        if (content == null) {
            return null;
        }
        if (isRoot(content)) {
            return null;
        }
        if (nodeTypeName == null) {
            return content.getParent();
        }
        Node parent = content.getParent();
        while (!parent.isNodeType(nodeTypeName)) {
            if (isRoot(parent)) {
                return null;
            }
            parent = parent.getParent();
        }
        return parent;
    }

    public Node root(Node content) throws RepositoryException {
        return this.root(content, null);
    }

    public Node root(Node content, String nodeTypeName) throws RepositoryException {
        if (content == null) {
            return null;
        }
        if (nodeTypeName == null) {
            return (Node) content.getAncestor(0);
        }
        if (isRoot(content) && content.isNodeType(nodeTypeName)) {
            return content;
        }

        Node parentNode = this.parent(content, nodeTypeName);
        if (parentNode == null) {
            return null;
        }
        while (!parentNode.isNodeType(nodeTypeName) && parentNode != null) {
            parentNode = this.parent(parentNode, nodeTypeName);
        }
        return parentNode;
    }

    public List<ContentMap> ancestors(ContentMap contentMap) throws RepositoryException {
        return ancestors(contentMap, null);
    }

    public List<ContentMap> ancestors(ContentMap contentMap, String nodeTypeName) throws RepositoryException {
        List<Node> ancestorsAsNodes = this.ancestors(contentMap.getJCRNode(), nodeTypeName);
        return asContentMapList(ancestorsAsNodes);
    }

    public List<Node> ancestors(Node content) throws RepositoryException {
        return content == null ? null : this.ancestors(content, null);
    }

    public List<Node> ancestors(Node content, String nodeTypeName) throws RepositoryException {
        if (content == null) {
            return null;
        }
        List<Node> ancestors = new ArrayList<Node>();
        int depth = content.getDepth();
        for (int i = 1; i < depth; ++i) {
            Node possibleAncestor = (Node) content.getAncestor(i);
            if (nodeTypeName == null) {
                ancestors.add(possibleAncestor);
            } else {
                if (possibleAncestor.isNodeType(nodeTypeName)) {
                    ancestors.add(possibleAncestor);
                }
            }
        }
        return ancestors;
    }

    public Node inherit(Node content, String relPath) throws RepositoryException {
        if (content == null) {
            return null;
        }
        if (StringUtils.isBlank(relPath)) {
            throw new IllegalArgumentException("relative path cannot be null or empty");
        }
        InheritanceNodeWrapper inheritedNode = new InheritanceNodeWrapper(content);
        try {
            Node subNode = inheritedNode.getNode(relPath);
            return NodeUtil.unwrap(subNode);
        } catch (PathNotFoundException e) {
            // TODO fgrilli: rethrow exception?
        }
        return null;
    }

    public ContentMap inherit(ContentMap content, String relPath) throws RepositoryException {
        if (content == null) {
            return null;
        }
        Node node = inherit(content.getJCRNode(), relPath);
        return node == null ? null : new ContentMap(node);
    }

    public Property inheritProperty(Node content, String relPath) throws RepositoryException {
        if (content == null) {
            return null;
        }
        if (StringUtils.isBlank(relPath)) {
            throw new IllegalArgumentException("relative path cannot be null or empty");
        }
        InheritanceNodeWrapper inheritedNode = new InheritanceNodeWrapper(content);
        try {
            return inheritedNode.getProperty(relPath);

        } catch (PathNotFoundException e) {
            // TODO fgrilli: rethrow exception?
        } catch (RepositoryException e) {
            // TODO fgrilli:rethrow exception?
        }

        return null;
    }

    public Property inheritProperty(ContentMap content, String relPath) throws RepositoryException {
        if (content == null) {
            return null;
        }
        return inheritProperty(content.getJCRNode(), relPath);
    }

    public List<Node> inheritList(Node content, String relPath) throws RepositoryException {
        if (content == null) {
            return null;
        }
        if (StringUtils.isBlank(relPath)) {
            throw new IllegalArgumentException("relative path cannot be null or empty");
        }
        InheritanceNodeWrapper inheritedNode = new InheritanceNodeWrapper(content);
        Node subNode = inheritedNode.getNode(relPath);
        return children(subNode);
    }

    public List<ContentMap> inheritList(ContentMap content, String relPath) throws RepositoryException {
        if (content == null) {
            return null;
        }
        if (StringUtils.isBlank(relPath)) {
            throw new IllegalArgumentException("relative path cannot be null or empty");
        }
        InheritanceNodeWrapper inheritedNode = new InheritanceNodeWrapper(asJCRNode(content));
        Node subNode = inheritedNode.getNode(relPath);
        return children(new ContentMap(subNode));

    }

    public boolean isFromCurrentPage(Node content) {
        return !isInherited(content);
    }

    public boolean isFromCurrentPage(ContentMap content) {
        return isFromCurrentPage(asJCRNode(content));
    }

    public boolean isInherited(Node content) {
        if (content instanceof InheritanceNodeWrapper) {
            return ((InheritanceNodeWrapper) content).isInherited();
        }
        return false;
    }

    public boolean isInherited(ContentMap content) {
        return isInherited(asJCRNode(content));
    }

    private boolean isRoot(Node content) throws RepositoryException {
        return content.getDepth() == 0;
    }

    /**
     * Returns an external link prepended with <code>http://</code> in case the protocol is missing or an empty String
     * if the link does not exist.
     *
     * @param content
     *            The node where the link property is stored on.
     * @param linkPropertyName
     *            The property where the link value is stored in.
     * @return The link prepended with <code>http://</code>
     */
    public String externalLink(Node content, String linkPropertyName) {
        String externalLink = PropertyUtil.getString(content, linkPropertyName);
        if (StringUtils.isBlank(externalLink)) {
            return StringUtils.EMPTY;
        }
        if (!hasProtocol(externalLink)) {
            externalLink = "http://" + externalLink;
        }
        return externalLink;
    }

    /**
     * Returns an external link prepended with <code>http://</code> in case the protocol is missing or an empty String
     * if the link does not exist.
     *
     * @param content
     *            The node's map representation where the link property is stored on.
     * @param linkPropertyName
     *            The property where the link value is stored in.
     * @return The link prepended with <code>http://</code>
     */
    public String externalLink(ContentMap content, String linkPropertyName) {
        return externalLink(asJCRNode(content), linkPropertyName);
    }

    /**
     * Return a link title based on the @param linkTitlePropertyName. When property @param linkTitlePropertyName is
     * empty or null, the link itself is provided as the linkTitle (prepended with <code>http://</code>).
     *
     * @param content
     *            The node where the link property is stored on.
     * @param linkPropertyName
     *            The property where the link value is stored in.
     * @param linkTitlePropertyName
     *            The property where the link title value is stored
     * @return the resolved link title value
     */
    public String externalLinkTitle(Node content, String linkPropertyName, String linkTitlePropertyName) {
        String linkTitle = PropertyUtil.getString(content, linkTitlePropertyName);
        if (StringUtils.isNotEmpty(linkTitle)) {
            return linkTitle;
        }
        return externalLink(content, linkPropertyName);
    }

    /**
     * Return a link title based on the @param linkTitlePropertyName. When property @param linkTitlePropertyName is
     * empty or null, the link itself is provided as the linkTitle (prepended with <code>http://</code>).
     *
     * @param content
     *            The node where the link property is stored on.
     * @param linkPropertyName
     *            The property where the link value is stored in.
     * @param linkTitlePropertyName
     *            The property where the link title value is stored
     * @return the resolved link title value
     */
    public String externalLinkTitle(ContentMap content, String linkPropertyName, String linkTitlePropertyName) {
        return externalLinkTitle(asJCRNode(content), linkPropertyName, linkTitlePropertyName);
    }

    public List<ContentMap> asContentMapList(Collection<Node> nodeList) {
        if (nodeList != null) {
            List<ContentMap> contentMapList = new ArrayList<ContentMap>();
            for (Node node : nodeList) {
                contentMapList.add(asContentMap(node));
            }
            return contentMapList;
        }
        return null;
    }

    public List<Node> asNodeList(Collection<ContentMap> contentMapList) {
        if (contentMapList != null) {
            List<Node> nodeList = new ArrayList<Node>();
            for (ContentMap node : contentMapList) {
                nodeList.add(node.getJCRNode());
            }
            return nodeList;
        }
        return null;
    }

    public boolean isEditMode() {
        // TODO : see CmsFunctions.isEditMode, which checks a couple of other properties.
        return isAuthorInstance() && !isPreviewMode();
    }

    public boolean isPreviewMode() {
        return MgnlContext.getAggregationState().isPreviewMode();
    }

    public boolean isAuthorInstance() {
        return Components.getComponent(ServerConfiguration.class).isAdmin();
    }

    public boolean isPublicInstance() {
        return !isAuthorInstance();
    }

    /**
     * Checks if passed string has a <code>http://</code> protocol.
     *
     * @param link
     *            The link to check
     * @return If @param link contains a <code>http://</code> protocol
     */
    private boolean hasProtocol(String link) {
        return link != null && link.contains("://");
    }

    /**
     * Util method to create html attributes <code>name="value"</code>. If the value is empty an empty string will be
     * returned. This is mainlly helpful to avoid empty attributes.
     */
    public String createAttribute(String name, String value) {
        value = StringUtils.trim(value);
        if (StringUtils.isNotEmpty(value)) {
            return new StringBuffer().append(name).append("=\"").append(value).append("\"").toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Returns an instance of SiblingsHelper for the given node.
     */
    public SiblingsHelper siblings(Node node) throws RepositoryException {
        return SiblingsHelper.of(ContentUtil.asContent(node));
    }

}
