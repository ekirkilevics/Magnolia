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
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Representation of the link to a content in Magnolia CMS. The target for the link might be a content (page, paragraph) or the node data (binary file).
 * @author had
 *
 */
public class Link {

    private String repository;
    private String handle;
    private String uuid;
    private String extension;
    private String fileName;
    private String fallbackHandle;
    private String anchor;
    private String parameters;
    
    private Node jcrNode;
    private Property property;
    private String propertyName;

    /**
     * A constructor for undefined links. (i.e linking to a nonexistent page, for instance)
     */
    public Link() {
    }

    /**
     * @param content
     * @deprecated since 5.0
     */
    public Link(Content content) {
        this(content.getJCRNode());
    }

    /**
     * @param node
     * @throws RepositoryException 
     */
    public Link(Node node) {
        try {
            setJCRNode(node);
            setRepository(node.getSession().getWorkspace().getName());
            if (node.isNodeType(MgnlNodeType.MIX_REFERENCEABLE)) {
                setUUID(node.getIdentifier());
            }
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public Link(String repoName, Content parent, NodeData nodedata) {
        initLink(repoName, parent, nodedata);
    }

    public Link(String repoName, Node parent, Property property) {
        setJCRNode(parent);
        setRepository(repoName);
        setProperty(property);
        try{
            setPropertyName(property.getName());
        }catch(RepositoryException e){
            throw new RuntimeException(e);
        }
    }

    public Link initLink(String repoName, Content parent, NodeData nodedata){
        try {
            return new Link(repoName, parent.getJCRNode(), nodedata.getJCRProperty());
        } catch (PathNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getExtension() {
        try{
            if(StringUtils.isEmpty(this.extension) && this.getJCRNode() != null && this.getJCRNode().isNodeType(MgnlNodeType.NT_RESOURCE)){
                File binary = new File(jcrNode);
                extension = binary.getExtension();
            }
            return StringUtils.defaultIfEmpty(this.extension, Components.getComponent(ServerConfiguration.class).getDefaultExtension());
        }catch(RepositoryException e){
            throw new RuntimeException(e);
        }
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getFileName() {
        try{
            if(StringUtils.isEmpty(this.fileName) && this.getJCRNode() != null && this.getJCRNode().isNodeType(MgnlNodeType.NT_RESOURCE)){
                File binary = new File(jcrNode);
                fileName = binary.getFileName();
            }
            return fileName;
        }catch(RepositoryException e){
            throw new RuntimeException(e);
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @deprecated since 5.0
     */
    public Content getNode() {
        return ContentUtil.asContent(this.jcrNode);
    }

    /**
     * @deprecated since 5.0
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
            if(this.property == null && StringUtils.isNotEmpty(this.propertyName) && this.getJCRNode() != null){
                this.property = this.getJCRNode().getProperty(this.propertyName);
            }
        }catch(RepositoryException e){
            throw new LinkException(e);
        }
        return this.property;
    }

    public void setProperty(Property property){
        this.property = property;
    }

    /**
     * @deprecated since 5.0
     */
    public NodeData getNodeData() {
        try {
            if(this.property == null && StringUtils.isNotEmpty(this.propertyName) && this.getJCRNode() != null){
                this.property = this.getJCRNode().getProperty(this.propertyName);
            }
            if(property == null){
                return null;
            }
            return ContentUtil.asContent(this.property.getParent()).getNodeData(this.propertyName);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @deprecated since 5.0
     */
    public void setNodeData(NodeData nodeData) {
        if(nodeData != null){
            try {
                this.property = nodeData.getJCRProperty();
            } catch (PathNotFoundException e) {
                throw new RuntimeException(e);
            }
        }else{
            this.property = null;
        }
    }

    public boolean isEditorBinaryLink(){
        try {
            return getJCRNode().isNodeType(NodeTypes.Resource.NAME);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @deprecated
     */
    public String getNodeDataName() {
        return this.propertyName;
    }

    /**
     * @deprecated
     */
    public void setNodeDataName(String nodeDataName) {
        this.propertyName = nodeDataName;
    }

     public String getHandle() {
         if(StringUtils.isEmpty(this.handle)){
             if(getJCRNode() != null){
                 try {
                    handle = getJCRNode().getPath();
                } catch (RepositoryException e) {
                    throw new RuntimeException(e);
                }
             } else {
                 handle = this.getFallbackHandle();
             }
         }
         return this.handle;
     }

    public void setHandle(String path) {
        this.handle = path;
    }

    public String getRepository() {
        return this.repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }
    
    public String getUUID() {
        if(StringUtils.isEmpty(this.uuid) && this.getJCRNode() != null){
            try {
                this.uuid = this.getJCRNode().getIdentifier();
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
         }
         return this.uuid;
     }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getFallbackHandle() {
        return this.fallbackHandle;
    }

    public void setFallbackHandle(String fallbackPath) {
        this.fallbackHandle = fallbackPath;
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