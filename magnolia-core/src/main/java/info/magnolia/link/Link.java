/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import javax.jcr.PropertyType;

import org.apache.commons.lang.StringUtils;

/**
 * @author had
 *
 */
public class Link {
    private String repository;
    private String handle;
    private String uuid;
    private String nodeDataName;
    private String extension;
    private Content node;
    private NodeData nodeData;
    private String fileName;
    private String fallbackHandle;
    private String anchor;
    private String parameters;

    /**
     * @param content
     */
    public Link(Content content) {
        setNode(content);
        setRepository(content.getHierarchyManager().getName());
        setUUID(content.getUUID());
    }

    public Link(String repoName, Content parent, NodeData nodedata) {
        setNode(parent);
        setRepository(repoName);
        setNodeData(nodedata);
        setNodeDataName(nodedata.getName());
    }

    public String getExtension() {
        if(StringUtils.isEmpty(this.extension) && this.getNodeData() != null && this.getNodeData().getType() == PropertyType.BINARY){
            File binary = new File(nodeData);
            extension = binary.getExtension();
        }
        return StringUtils.defaultIfEmpty(this.extension, ServerConfiguration.getInstance().getDefaultExtension());
    }


    public void setExtension(String extension) {
        this.extension = extension;
    }


    public String getFileName() {
        if(StringUtils.isEmpty(this.fileName) && this.getNodeData() != null && this.getNodeData().getType() == PropertyType.BINARY){
            File binary = new File(nodeData);
            fileName = binary.getFileName();
        }
        return fileName;
    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Content getNode() {
        return this.node;
    }


    public void setNode(Content node) {
        this.node = node;
    }

    public NodeData getNodeData() {
        if(this.nodeData == null && StringUtils.isNotEmpty(this.nodeDataName) && this.getNode() != null){
            this.nodeData = this.getNode().getNodeData(this.nodeDataName);
        }
        return this.nodeData;
    }

    public void setNodeData(NodeData nodeData) {
        this.nodeData = nodeData;
    }

    public String getNodeDataName() {
        return this.nodeDataName;
    }

    public void setNodeDataName(String nodeDataName) {
        this.nodeDataName = nodeDataName;
    }

    public String getHandle() {
        if(StringUtils.isEmpty(this.handle)){
            if(getNode() != null){
                handle = getNode().getHandle();
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
        if(StringUtils.isEmpty(this.uuid) && this.getNode() != null){
            this.uuid = this.getNode().getUUID();
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
