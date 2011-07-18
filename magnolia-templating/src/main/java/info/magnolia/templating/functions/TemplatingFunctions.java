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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.DefaultContent;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.NodeUtil;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * This is an object exposing a couple of methods useful for templates; it's exposed in templates as "cmsfn".
 *
 * @version $Id$
 * TODO dlipp: to be reviewed - see SCRUM-277
 */
public class TemplatingFunctions {

    public Content asContent(Node node) throws RepositoryException {
        return node == null ? null : new DefaultContent(node, null);
    }

    public Node asJCRNode(ContentMap contentMap) {
        return contentMap == null ? null : contentMap.getJCRNode();
    }

    public Node asJCRNode(Content content) {
        return content == null ? null : content.getJCRNode();
    }

    public ContentMap asContentMap(Node content) {
        return content == null ? null : new ContentMap(content);
    }

    public Node parent(Node content) throws RepositoryException {
        if(content == null || content.getDepth() == 0) {
            return null;
        }
        return content.getParent();
    }

    public ContentMap parent(ContentMap contentMap) throws RepositoryException {
        if(contentMap == null) {
            return null;
        }
        Node parentContent = this.parent(contentMap.getJCRNode());
        return asContentMap(parentContent);
    }

//    //TODO cringele : these would be the right way of creating links. LinkUtil needs to be Node capable and not only Content. (SCRUM-242)
//    public String link(Node content) throws RepositoryException{
//        //TODO cringele : LinkUtil should accept Node and not only Content
//        return content == null ? null : LinkUtil.createLink(asContent(content));
//    }

    //TODO cringele : hacky way of creating a link, but serves for now until LinkUtill is Node capable (SCRUM-242)
    public String link(Node content) throws RepositoryException{
        return content == null ? null : MgnlContext.getContextPath()+content.getPath();
    }

    public String link(ContentMap contentMap) throws RepositoryException{
        return contentMap == null ? null : this.link(asJCRNode(contentMap));
    }

    protected List<ContentMap> contentMapChildrenFrom(Iterable<Node> nodes) {
        List<ContentMap> childList = new ArrayList<ContentMap>();
        for (Node child : nodes) {
            childList.add(new ContentMap(child));
        }
        return childList;
    }

    public List<ContentMap> children(ContentMap content) throws RepositoryException {
        return content == null ? null : contentMapChildrenFrom(NodeUtil.getNodes(asJCRNode(content), NodeUtil.EXCLUDE_META_DATA_FILTER));
    }

    public List<ContentMap> children(ContentMap content, String nodeTypeName) throws RepositoryException {
        return content == null ? null : contentMapChildrenFrom(NodeUtil.getNodes(asJCRNode(content), nodeTypeName));
    }

    protected List<Node> nodeChildrenFrom(Iterable<Node> nodes) {
        List<Node> childList = new ArrayList<Node>();
        for (Node child : nodes) {
            childList.add(child);
        }
        return childList;
    }

    public List<Node> children(Node content) throws RepositoryException {
        return content == null ? null : nodeChildrenFrom(NodeUtil.getNodes(content, NodeUtil.EXCLUDE_META_DATA_FILTER));
    }

    public List<Node> children(Node content, String nodeTypeName) throws RepositoryException {
        return content == null ? null: nodeChildrenFrom(NodeUtil.getNodes(content, nodeTypeName));
    }

    public ContentMap root(ContentMap content) throws RepositoryException{
        return content == null ? null : asContentMap(this.root(asJCRNode(content)));
    }

    //TODO cringele : should replace the method root(Node) below, can't test it yet. See SCRUM-277
//    public Node root(Node content) throws RepositoryException{
//        return content == null ? null :  content.getSession().getRootNode();
//    }

    public Node root(Node content) throws RepositoryException{
        if(content == null) {
            return null;
        }
        Node root = content;
        while(root.getDepth() > 0){
            root = this.parent(root);
        }
        return root;
    }

    public ContentMap rootPage(ContentMap content) throws RepositoryException{
        return content == null ? null : asContentMap(this.rootPage(asJCRNode(content)));
    }

    //TODO cringele: should replace the method rootPage(Node) below, can't test it yet. See SCRUM-277
//  public Node rootPage(Node content) throws RepositoryException{
//      if(content == null) {
//          return null;
//      }
//      String firstPagesPath = "/"+StringUtils.split(content.getPath(), "/")[0];
//      if(StringUtils.equals(content.getPath(), firstPagesPath)){
//          return content;
//      }
//      return content.getSession().getNode(firstPagesPath);
//  }

    public Node rootPage(Node content) throws RepositoryException{
        if(content == null) {
            return null;
        }
        Node rootPage = content;
        while(rootPage.getDepth() > 1){
            rootPage = this.parent(rootPage);
        }
        return rootPage;
    }

    public ContentMap page(ContentMap content) throws RepositoryException{
        return content == null ? null : asContentMap(this.page(asJCRNode(content)));
    }

    public Node page(Node content) throws RepositoryException{
        if(content == null) {
            return null;
        }
        Node page = content;
        while(!isPageNode(page) && this.parent(page).getDepth()>0){
            page = this.parent(page);
        }
        return page;
    }

    private boolean isPageNode(Node node) throws RepositoryException {
        return StringUtils.equals(MgnlNodeType.NT_CONTENT, node.getPrimaryNodeType().getName());
    }



    //TODO cringele : May all be optional. Decide on weather to provide them or not

//    public String linkExteral(Node content) throws RepositoryException{
//        return content == null ? null : LinkUtil.createExternalLink(asContent(content));
//    }
//
//    public String linkExternal(ContentMap contentMap) throws RepositoryException{
//        return contentMap == null ? null : this.linkExteral(asJCRNode(contentMap));
//    }
//
//    public String linkAbsolute(Node content) throws RepositoryException{
//        return content == null ? null : LinkUtil.createAbsoluteLink(asContent(content));
//    }
//
//    public String linkAbsolute(ContentMap contentMap) throws RepositoryException{
//        return contentMap == null ? null : this.linkAbsolute(asJCRNode(contentMap));
//    }

}
