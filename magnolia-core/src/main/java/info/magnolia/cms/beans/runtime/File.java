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
package info.magnolia.cms.beans.runtime;

import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;

import org.apache.commons.lang.math.NumberUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.InputStream;


/**
 * Wraps a NodeData and exposes it into a simple file-like bean.
 *
 * @author Sameer Charles
 * @version 1.1
 */
public class File {

    private Node node;

    private String extension;

    private String fileName;

    private String contentType;

    private int size;

    /**
     * @deprecated Since 5.0 use File(Node)
     */
    public File(NodeData data) {
        initFile(data);
    }
    
    public File(Node node) {
        this.node = node;

        try{
            if(node.hasProperty("extension")){
                setExtension(node.getProperty("extension").getString());
            }
            if(node.hasProperty("fileName")){
                setFileName(node.getProperty("fileName").getString());
            }
            if(node.hasProperty("contentType")){
                setContentType(node.getProperty("contentType").getString());
            }

            if(node.hasProperty("size")){
                String sizeString = node.getProperty("size").getString();
                if (NumberUtils.isNumber(sizeString)) {
                    setSize(Integer.parseInt(sizeString));
                }
            }
        }catch(RepositoryException e){
            throw new RuntimeException(e);
        }

    }

    /**
     * Initialisation method for File(NodeData) constructor.
     * @deprecated Since 5.0 use File(Node)
     */
    private File initFile(NodeData nodedata){
        try {
            return new File(MgnlContext.getJCRSession(nodedata.getHierarchyManager().getWorkspace().getName()).getNode(nodedata.getHandle()));
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @deprecated Since 5.0.
     */
    public String getNodeDataTemplate() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Since 5.0.
     */
    public void setNodeDataTemplate(String nodeDataTemplate) {
        throw new UnsupportedOperationException();
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
    
    public Node getNode() {
        return node;
    }

    /**
     * @deprecated Since 5.0 use getNode(Node) instead. 
     */
    public NodeData getNodeData() {
        try {
            if(ContentUtil.asContent(node.getParent()).hasNodeData(node.getName())){
                return ContentUtil.asContent(node.getParent()).getNodeData(node.getName());
            }
        } catch (RepositoryException e) {
            //Cannot retrieve NodeData, return null.
        }
        return null;
    }

    /**
     * @deprecated Since 5.0, unsupported on Node API. 
     */
    public InputStream getStream() {
        return getNodeData().getStream();
    }
}
