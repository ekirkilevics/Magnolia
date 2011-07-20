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

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.NodeUtil;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * This is an object exposing a couple of methods useful for templates; it's exposed in templates as "cmsfn".
 *
 * @version $Id$
 * TODO dlipp: to be reviewed - see SCRUM-277
 */
public class TemplatingFunctions {


    public Node asJCRNode(ContentMap contentMap) {
        return contentMap == null ? null : contentMap.getJCRNode();
    }

    public ContentMap asContentMap(Node content) {
        return content == null ? null : new ContentMap(content);
    }

//    //TODO cringele : these would be the right way of creating links. LinkUtil needs to be Node capable and not only Content. (SCRUM-242)
//    public String link(Node content) throws RepositoryException{
//        return content == null ? null : LinkUtil.createLink(content);
//    }

    //TODO cringele : hacky way of creating a link, but serves for now until LinkUtill is Node capable (SCRUM-242)
    public String link(Node content) throws RepositoryException{
        return content == null ? null : MgnlContext.getContextPath()+content.getPath();
    }

    public String link(ContentMap contentMap) throws RepositoryException{
        return contentMap == null ? null : this.link(asJCRNode(contentMap));
    }

    public List<Node> children(Node content) throws RepositoryException {
        return content == null ? null : nodeChildrenFrom(NodeUtil.getNodes(content, NodeUtil.EXCLUDE_META_DATA_FILTER));
    }

    public List<Node> children(Node content, String nodeTypeName) throws RepositoryException {
        return content == null ? null: nodeChildrenFrom(NodeUtil.getNodes(content, nodeTypeName));
    }

    protected List<Node> nodeChildrenFrom(Iterable<Node> nodes) {
        List<Node> childList = new ArrayList<Node>();
        for (Node child : nodes) {
            childList.add(child);
        }
        return childList;
    }

    public List<ContentMap> children(ContentMap content) throws RepositoryException {
        return content == null ? null : contentMapChildrenFrom(NodeUtil.getNodes(asJCRNode(content), NodeUtil.EXCLUDE_META_DATA_FILTER));
    }

    public List<ContentMap> children(ContentMap content, String nodeTypeName) throws RepositoryException {
        return content == null ? null : contentMapChildrenFrom(NodeUtil.getNodes(asJCRNode(content), nodeTypeName));
    }

    protected List<ContentMap> contentMapChildrenFrom(Iterable<Node> nodes) {
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

    public ContentMap root(ContentMap contentMap) throws RepositoryException {
        return contentMap == null ? null : asContentMap(this.root(contentMap.getJCRNode()));
    }

    public ContentMap root(ContentMap contentMap, String nodeTypeName) throws RepositoryException {
        return contentMap == null ? null : asContentMap(this.root(contentMap.getJCRNode(), nodeTypeName));
    }

    //TODO cringele: finish code for ancestors(ContentMap)
//    public List<ContentMap> ancestors(ContentMap contentMap) throws RepositoryException {
//        return contentMap == null ? null : this.ancestors(contentMap.getJCRNode());
//    }
//
//    public List<ContentMap> ancestors(ContentMap contentMap, String nodeTypeName) throws RepositoryException {
//        return contentMap == null ? null : this.ancestors(contentMap.getJCRNode(), nodeTypeName));
//    }

    public Node parent(Node content) throws RepositoryException {
        return this.parent(content, null);
    }

    public Node parent(Node content, String nodeTypeName) throws RepositoryException {
        if(content == null) {
            return null;
        }
        if(isRoot(content)) {
            return null;
        }
        if(nodeTypeName==null ){
            return content.getParent();
        }
        Node parent = content.getParent();
        while(!parent.isNodeType(nodeTypeName)){
            if(isRoot(parent)){
                return null;
            }
            parent = parent.getParent();
        }
        return parent;
    }

    public Node root(Node content) throws RepositoryException{
        return this.root(content, null);
    }

    public Node root(Node content, String nodeTypeName) throws RepositoryException{
        if(content == null) {
            return null;
        }
        if(nodeTypeName==null ){
            return (Node) content.getAncestor(0);
        }
        if(isRoot(content) && content.isNodeType(nodeTypeName)) {
            return content;
        }

        Node parentNode = this.parent(content, nodeTypeName);
        if(parentNode == null){
            return null;
        }
        while(!parentNode.isNodeType(nodeTypeName) && parentNode != null){
            parentNode = this.parent(parentNode, nodeTypeName);
        }
        return parentNode;
    }

    public List<Node> ancestors(Node content) throws RepositoryException{
        return content == null ? null : this.ancestors(content, null);
    }

    public List<Node> ancestors(Node content, String nodeTypeName) throws RepositoryException{
        if(content == null) {
            return null;
        }
        List<Node> ancestors = new ArrayList<Node>();
        int depth = content.getDepth();
        for(int i=1; i<depth; ++i){
            Node possibelAncestor = (Node)content.getAncestor(i);
            if(nodeTypeName == null){
                ancestors.add(possibelAncestor);
            } else {
                if(possibelAncestor.isNodeType(nodeTypeName)){
                    ancestors.add(possibelAncestor);
                }
            }
        }
        return ancestors;
    }

    private boolean isRoot(Node content) throws RepositoryException {
        return content.getDepth() == 0;
    }

}