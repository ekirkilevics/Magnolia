/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */




package info.magnolia.cms.exchange.simple;

import info.magnolia.cms.core.*;
import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.security.Authenticator;
import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.PropertyType;
import javax.jcr.PathNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;


/**
 * Date: Jun 25, 2004
 * Time: 9:48:44 AM
 *
 * @author Sameer Charles
 * @version 2.0
 */



public class ContentWriter {



    private static Logger log = Logger.getLogger(ContentWriter.class);


    private String repositoryName;
    private String baseURL;
    private String credentials;
    private HttpServletRequest request;
    private HierarchyManager hierarchyManager;
    private SerializableContent backup;


    public ContentWriter() {}



    public ContentWriter(HierarchyManager hm, String repositoryName, String baseURL, HttpServletRequest request) {
        this.repositoryName = repositoryName;
        this.baseURL = baseURL;
        this.request = request;
        this.credentials = Authenticator.getCredentials(this.request);
        this.hierarchyManager = hm;
    }



    public void writeObject(String destination, Object content)
            throws RepositoryException {
        if (content instanceof SerializableContentNode) {
            this.writeObject(this.hierarchyManager.getPage(destination), (SerializableContentNode)content);
        }
        else if (content instanceof SerializableContent) {
            this.writeObject(this.hierarchyManager.getPage(destination), (SerializableContent)content);
        }
    }



    public void writeObject(Content destination, SerializableContent content)
            throws RepositoryException {
        this.writeContent(destination, content);
        this.hierarchyManager.save();
        log.info("Repository refreshed / saved");
    }



    public void writeObject(Content destination, SerializableContentNode contentNode)
            throws RepositoryException {
        this.writeContent(destination, contentNode);
        this.hierarchyManager.save();
        log.info("Repository refreshed / saved");
    }



    /**
     * <p>writes the serialized object to the specified location.<br>
     * If the content already exists in a persistent layer, update values<br>
     * else, create persistent object
     * <p>
     *
     * */
    private void writeContent(Content parent, SerializableContent serializableContent)
            throws RepositoryException {
        String newPageName = serializableContent.getName();
        String parentHandle = parent.getHandle();
        if (parentHandle.equals("/"))
            parentHandle = "";
        Content content = null;
        if (serializableContent instanceof SerializableContentNode) {
            try {
                content = this.hierarchyManager.getContentNode(parentHandle+"/"+newPageName);
                this.safeDelete(content);
            } catch (PathNotFoundException e) {
                content = this.hierarchyManager.createContent(parent.getHandle(), newPageName, ItemType.NT_CONTENTNODE);
            }
        } else {
            try {
                content = this.hierarchyManager.getPage(parentHandle+"/"+newPageName);
                this.safeDelete(content);
            } catch (PathNotFoundException e) {
                content = this.hierarchyManager.createPage(parent.getHandle(), newPageName);
            }
        }

        try {
            /* write meta data */
            this.writeMetaData(content,serializableContent.getMetaData());
            /* write all node data */
            this.writeNodeData(content, serializableContent);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        /* write all content nodes */
        this.writeContentNode(content, serializableContent);

        Iterator contentIterator = serializableContent.getContentCollection().iterator();
        while (contentIterator.hasNext()) {
            SerializableContent sContent = (SerializableContent) contentIterator.next();
            try {
                this.writeContent(content, sContent);
            } catch (RepositoryException re) {
                log.error("Failed to update "+(parent.getHandle()+"/"+newPageName));
                log.error(re.getMessage(), re);
            }
        }
    }



    private void safeDelete(Content content) {
        log.info("Taking backup for "+content.getHandle());
        this.backup = new SerializableContent(content);
        //todo restore? in case of exception 
        log.info("Removing existing page "+content.getHandle());
        this.removeNodedataList(content);
        this.removeContentNodeList(content);
    }


    //todo test this
    private void removeNodedataList(Content node) {
        Collection subNodes = node.getChildren(ItemType.NT_NODEDATA);
        if (subNodes.size() > 0) {
            Iterator nodeIterator = subNodes.iterator();
            while(nodeIterator.hasNext()) {
                NodeData subNode = (NodeData) nodeIterator.next();
                try {
                    node.deleteNodeData(subNode.getName());
                } catch(RepositoryException re) {
                    log.error("Failed to remove node data - "+subNode.getHandle());
                    log.error(re.getMessage(), re);
                }
            }
        }
    }



    private void removeContentNodeList(Content node) {
        Collection subNodes = node.getChildren(ItemType.NT_CONTENTNODE);
        if (subNodes.size() > 0) {
            Iterator nodeIterator = subNodes.iterator();
            while(nodeIterator.hasNext()) {
                ContentNode subNode = (ContentNode) nodeIterator.next();
                try {
                    node.deleteContentNode(subNode.getName());
                } catch(RepositoryException re) {
                    log.error("Failed to remove content node - "+subNode.getHandle());
                    log.error(re.getMessage(), re);
                }
            }
        }
    }


    /**
     *
     * @param parent
     * @param serializableContent
     * */
    private void writeContentNode(Content parent, SerializableContent serializableContent) {
        Iterator contentNodeIterator = serializableContent.getContentNodeCollection().iterator();
        while (contentNodeIterator.hasNext()) {
            SerializableContentNode sContentNode = (SerializableContentNode) contentNodeIterator.next();
            try {
                ContentNode newContentNode = parent.createContentNode(sContentNode.getName());
                try {
                    /* write meta data */
                    this.writeMetaData(newContentNode,sContentNode.getMetaData());
                    this.writeNodeData(newContentNode, sContentNode);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                /* check for sub content Nodes */
                if (sContentNode.getContentNodeCollection().size() > 0)
                    this.writeContentNode(newContentNode, sContentNode);
            } catch (RepositoryException re) {
                log.error(re.getMessage(), re);
            }
        }
    }



    private void writeMetaData(Content content, SerializableMetaData serializableMetaData) {
        MetaData metaData = content.getMetaData();
        ArrayList propertyList= serializableMetaData.getMetaProperties();
        for (int index=0; index<propertyList.size(); index++) {
            MetaDataProperty property = (MetaDataProperty) propertyList.get(index);
            switch(property.getType()) {
                case PropertyType.STRING:
                    metaData.setProperty(property.getName(),property.getString());
                    break;
                case PropertyType.LONG:
                    metaData.setProperty(property.getName(),property.getLong());
                    break;
                case PropertyType.DOUBLE:
                    metaData.setProperty(property.getName(),property.getDouble());
                    break;
                case PropertyType.BOOLEAN:
                    metaData.setProperty(property.getName(),property.getBoolean());
                    break;
                case PropertyType.DATE:
                    metaData.setProperty(property.getName(),property.getDate());
                    break;
                default:
                    log.error("Unknown property type - "+property.getType());
            }
        }
    }



    /**
     *
     * @param content parent content object
     * @param serializableContent as received
     * */
    private void writeNodeData(Content content, SerializableContent serializableContent)
            throws Exception {
        Iterator nodeDataIterator = serializableContent.getNodeDataCollection().iterator();
        while (nodeDataIterator.hasNext()) {
            SerializableNodeData sNodeData = (SerializableNodeData) nodeDataIterator.next();
            log.info("Writing NodeData list for "+content.getHandle());
            log.info("Writing NodeData [ "+sNodeData.getName()+" ] Type [ "
                    +PropertyType.nameFromValue(sNodeData.getType())+" ]");
            try {
                NodeData nodeData = content.createNodeData(sNodeData.getName());
                switch (sNodeData.getType()) {
                    case PropertyType.STRING:
                        nodeData.setValue(sNodeData.getString());
                        break;
                    case PropertyType.LONG:
                        nodeData.setValue(sNodeData.getLong());
                        break;
                    case PropertyType.DOUBLE:
                        nodeData.setValue(sNodeData.getDouble());
                        break;
                    case PropertyType.BOOLEAN:
                        nodeData.setValue(sNodeData.getBoolean());
                        break;
                    case PropertyType.DATE:
                        nodeData.setValue(sNodeData.getDate());
                        break;
                    case PropertyType.BINARY:
                        String binaryResourceHandle = sNodeData.getBinaryAsLink();
                        URL url = new URL(this.baseURL);
                        URLConnection urlConnection = url.openConnection();
                        urlConnection.setRequestProperty("Authorization",credentials);
                        urlConnection.addRequestProperty(Syndicator.PAGE,binaryResourceHandle);
                        urlConnection.addRequestProperty(Syndicator.ACTION,Syndicator.GET);
                        urlConnection.addRequestProperty(Syndicator.GET_TYPE,Syndicator.GET_TYPE_BINARY);
                        urlConnection.addRequestProperty(Syndicator.WORKING_CONTEXT,this.repositoryName);
                        nodeData.setValue(urlConnection.getInputStream());
                }
            } catch (RepositoryException re) {
                log.error(re.getMessage(), re);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

    }




}
