/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.link;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;

/**
 * Representation of the link to a content in Magnolia CMS. The target for the link might be a content (page, paragraph) or the node data (binary file).
 * @author had
 *
 */
public class Link {

    private String workspace;
    private String path;
    private String uuid;
    private String extension;
    private String fileName;
    private String fallbackPath;
    private String anchor;
    private String parameters;
    
    private Node jcrNode;
    private Property property;
    private String propertyName;

    /**
     * A constructor for undefined links. (i.e linking to a nonexistent page, for instance).
     */
    public Link() {
    }

    /**
     * @param content
     * @deprecated Since 5.0 use Link(Node).
     */
    public Link(Content content) {
        this(content.getJCRNode());
    }

    /**
     * @param node
     */
    public Link(Node node) {
        try {
            setJCRNode(node);
            setWorkspace(node.getSession().getWorkspace().getName());
            if (node.isNodeType(JcrConstants.MIX_REFERENCEABLE)) {
                setUUID(node.getIdentifier());
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    /**
     * @deprecated Since 5.0 use Link(Node).
     */
    public Link(String workspaceName, Content parent, NodeData nodedata) {
        initLink(workspaceName, parent, nodedata);
    }

    public Link(Property property) {
        try{
            setJCRNode(property.getParent());
            setWorkspace(property.getSession().getWorkspace().toString());
            setProperty(property);
            setPropertyName(property.getName());
        }catch(RepositoryException e){
            throw new RuntimeRepositoryException(e);
        }
    }

    /**
     * Initialisation method for Link(String, Content, NodeData) constructor.
     * @param workspaceName
     * @param parent
     * @param nodedata
     * @deprecated Since 5.0 use Link(Node).
     */
    public Link initLink(String workspaceName, Content parent, NodeData nodedata){
        try {
            if(nodedata.getType() != PropertyType.BINARY){
                return new Link(nodedata.getJCRProperty());
            }
            return new Link(MgnlContext.getJCRSession(nodedata.getHierarchyManager().getWorkspace().getName()).getNode(nodedata.getHandle()));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public String getExtension() {
        try{
            if(StringUtils.isEmpty(this.extension) && this.getJCRNode() != null && this.getJCRNode().isNodeType(NodeTypes.Resource.NAME)){
                File binary = new File(jcrNode);
                extension = binary.getExtension();
            }
        }catch(RepositoryException e){
            //Just return extension if already set, default if not.
        }
        return StringUtils.defaultIfEmpty(this.extension, Components.getComponent(ServerConfiguration.class).getDefaultExtension());
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getFileName() {
        try{
            if(StringUtils.isEmpty(this.fileName) && this.getJCRNode() != null && this.getJCRNode().isNodeType(NodeTypes.Resource.NAME)){
                File binary = new File(jcrNode);
                fileName = binary.getFileName();
            }
        }catch(RepositoryException e){
            //Just return fileName.
        }
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @deprecated Since 5.0 use Link.getJCRNode() instead.
     */
    public Content getNode() {
        return ContentUtil.asContent(this.jcrNode);
    }

    /**
     * @deprecated since 5.0 use Link.setJCRNode() instead.
     */
    public void setNode(Content node) {
        this.jcrNode = node.getJCRNode();
    }

    public Node getJCRNode() {
        return this.jcrNode;
    }

    public void setJCRNode(Node jcrNode) {
        this.jcrNode = jcrNode;
    }

    public Property getProperty() throws LinkException{
        try{
            if(this.property == null && StringUtils.isNotEmpty(this.propertyName) && this.getJCRNode() != null && this.getJCRNode().hasProperty(propertyName)){
                this.property = this.getJCRNode().getProperty(this.propertyName);
            }
        }catch(RepositoryException e){
            //Just return null;
        }
        return this.property;
    }

    public void setProperty(Property property){
        this.property = property;
    }

    /**
     * @deprecated since 5.0 use Link.getProperty() instead.
     */
    public NodeData getNodeData() {
        try {
            if(this.jcrNode != null && this.jcrNode.isNodeType(NodeTypes.Resource.NAME)){
                return ContentUtil.asContent(jcrNode.getParent()).getNodeData(jcrNode.getName());
            }
            else if(this.property == null && StringUtils.isNotEmpty(this.propertyName) && this.getJCRNode() != null){
                this.property = this.getJCRNode().getProperty(this.propertyName);
            }
            if(property == null){
                return null;
            }
            return ContentUtil.asContent(this.property.getParent()).getNodeData(this.propertyName);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    /**
     * @deprecated since 5.0 use Link.setProperty() instead.
     */
    public void setNodeData(NodeData nodeData) {
        if(nodeData != null){
            try {
                if(nodeData.getType() != PropertyType.BINARY){
                    this.property = nodeData.getJCRProperty();
                }else{
                    this.jcrNode = nodeData.getParent().getJCRNode().getNode(nodeData.getName());
                }
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }else{
            this.property = null;
        }
    }

    public boolean isEditorBinaryLink(){
        try {
            return getJCRNode().isNodeType(NodeTypes.Resource.NAME);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @deprecated Since 5.0 use Link.getPropertyName() instead.
     */
    public String getNodeDataName() {
        return this.propertyName;
    }

    /**
     * @deprecated Since 5.0 use Link.setPropertyName() instead.
     */
    public void setNodeDataName(String nodeDataName) {
        this.propertyName = nodeDataName;
    }

    /**
     * @deprecated Since 5.0 use Link.getPath instead.
     */
    public String getHandle() {
        return getPath();
    }

    /**
     * @deprecated Since 5.0 use Link.setPath instead.
     */
    public void setHandle(String path) {
        setPath(path);
    }
    
    public String getPath() {
        if(StringUtils.isEmpty(this.path)){
            if(getJCRNode() != null){
                try {
                    path = getJCRNode().getPath();
               } catch (RepositoryException e) {
                   throw new RuntimeRepositoryException(e);
               }
            } else {
                path = this.getFallbackHandle();
            }
        }
        return this.path;
    }

   public void setPath(String path) {
       this.path = path;
   }

    /**
     * @deprecated Since 5.0 use Link.getWorkspace instead.
     */
    public String getRepository() {
        return getWorkspace();
    }

    /**
     * @deprecated Since 5.0 use Link.setWorkspace instead.
     */
    public void setRepository(String repository) {
        setWorkspace(repository);
    }

    public String getWorkspace() {
        return this.workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }
    
    public String getUUID() {
        if(StringUtils.isEmpty(this.uuid) && this.getJCRNode() != null){
            try {
                this.uuid = this.getJCRNode().getIdentifier();
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
         }
         return this.uuid;
     }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @deprecated Since 5.0 use Link.getFallbackPath instead.
     */
    public String getFallbackHandle() {
        return getFallbackPath();
    }

    /**
     * @deprecated Since 5.0 use Link.setFallbackPath instead.
     */
    public void setFallbackHandle(String fallbackPath) {
        setFallbackPath(fallbackPath);
    }

    public String getFallbackPath() {
        return this.fallbackPath;
    }

    public void setFallbackPath(String fallbackPath) {
        this.fallbackPath = fallbackPath;
    }

    public String getAnchor() {
        return this.anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public String getParameters() {
        return this.parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}