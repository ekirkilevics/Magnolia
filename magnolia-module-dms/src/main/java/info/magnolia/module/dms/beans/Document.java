/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.dms.beans;

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.misc.FileProperties;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * Convinience class to handle document nodes.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class Document {

    private static final String DMS_DOCUMENT_CURRENT = "info.magnolia.module.dms.beans.Document.current";

    private static final String PROPERTY_FILEDATA = "document";

    private Content node;

    private Content orgNode;

    private NodeData fileNode;

    private String version;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(AdminTreeMVCHandler.class);

    public Document(Content node) {
        this.orgNode = node;
        this.setNode(node);
    }

    public Document(Content node, String version)
        throws PathNotFoundException,
        VersionException,
        UnsupportedRepositoryOperationException,
        RepositoryException {
        this(node);
        setVersion(version);
    }

    public static boolean isDocument(Content node) {
        try {
            return node.getParent().getHandle().equals("/")
                || node.getParent().getNodeType().getName().equals(ItemType.CONTENT.getSystemName());
        }
        catch (Exception e) {
            return false;
        }
    }

    public Version addVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return addVersion(node);
    }

    private Version addVersion(Content node) throws UnsupportedRepositoryOperationException, RepositoryException {
        Version version = node.addVersion();

        return version;
    }

    public void restore(String name) throws VersionException, UnsupportedRepositoryOperationException,
        RepositoryException {
        node.restore(name, true);
        node.addVersion();
    }

    public static String getMimeType(String ext) {
        return MIMEMapping.getMIMEType(ext);
    }

    public String getMimeType() {
        return this.fileNode.getAttribute(FileProperties.PROPERTY_CONTENTTYPE);
    }

    /**
     * depends on mime/type
     */
    public String getMimeTypeIcon() {
        return getMimeTypeIcon(getFileExtension());
    }

    public static String getMimeTypeIcon(String ext) {
        return MIMEMapping.getMIMETypeIcon(ext);
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) throws PathNotFoundException, VersionException,
        UnsupportedRepositoryOperationException, RepositoryException {
        if (StringUtils.isEmpty(version)) {
            return;
        }
        this.version = version;
        this.setNode(orgNode.getVersionedContent(version));
    }

    /**
     * @return
     */
    public String getFileName() {
        if (this.fileNode == null) {
            return "";
        }
        return this.fileNode.getAttribute(FileProperties.PROPERTY_FILENAME);
    }

    /**
     * @return
     */
    public long getFileSize() {
        if (this.fileNode == null) {
            return 0;
        }
        return Long.valueOf(this.fileNode.getAttribute(FileProperties.PROPERTY_SIZE)).longValue();
    }

    public String getFileExtension() {
        if (this.fileNode == null) {
            return "";
        }

        return this.fileNode.getAttribute(FileProperties.PROPERTY_EXTENSION);
    }

    public void setFileName(String name) {
        try {
            this.fileNode.setAttribute(FileProperties.PROPERTY_FILENAME, name);
        }
        catch (Exception e) {
            log.error("can't set filename [" + name + "]", e);
        }
    }

    public void setFileExtension(String extension) {
        try {
            this.fileNode.setAttribute(FileProperties.PROPERTY_EXTENSION, extension);
        }
        catch (Exception e) {
            log.error("can't set file extension [" + extension + "]", e);
        }
    }

    public InputStream getFileStream() throws IllegalStateException, RepositoryException {
        return this.fileNode.getStream();
    }

    public String getName() {
        return this.node.getName();
    }

    public String getTitle() {
        return this.node.getNodeData("title").getString();
    }

    /**
     * @deprecated
     * @param name
     * @return
     */
    public NodeData getNodeData(String name) {
        return node.getNodeData(name);
    }

    public Calendar getModificationDate() {
        return node.getMetaData().getModificationDate();
    }

    public String getLink() {
        return this.node.getHandle() + "/" + this.getFileName() + "." + this.getFileExtension();
    }

    public String getPath() {
        return this.orgNode.getHandle();
    }

    public String getStaticLink() {
        String link = "/dms-static/" + this.node.getUUID() + "/" + this.getEncodedFileName();
        if (StringUtils.isNotEmpty(this.getVersion())) {
            link += "?mgnlVersion=" + this.getVersion();
        }
        return link;
    }

    public String getEncodedFileName() {
        String name = this.getFileName() + "." + this.getFileExtension();
        try {
            name = URLEncoder.encode(name, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            log.error("can't encode filename", e);
        }
        name = StringUtils.replace(name, "+", "%20");
        return name;
    }

    public static void setCurrent(HttpServletRequest request, Document doc) {
        request.setAttribute(DMS_DOCUMENT_CURRENT, doc);
    }

    public static Document getCurrent(HttpServletRequest request) {
        return (Document) request.getAttribute(DMS_DOCUMENT_CURRENT);
    }

    public String getUserName() {
        try {
            return this.node.getNodeData("modifier").getString();
        }

        catch (Exception e) {
            return "";
        }

    }

    public String getVersionComment() {
        try {
            return this.node.getNodeData("versionComment").getString();
        }

        catch (Exception e) {
            return "";
        }
    }

    public void setFile(String label, String extension, InputStream stream, long size) throws RepositoryException,
        AccessDeniedException, PathNotFoundException {
        // write file
        this.fileNode = node.createNodeData(PROPERTY_FILEDATA, PropertyType.BINARY);
        this.fileNode.setValue(stream);

        // set properties
        this.fileNode.setAttribute(FileProperties.PROPERTY_EXTENSION, extension);
        this.fileNode.setAttribute(FileProperties.PROPERTY_FILENAME, label);
        this.fileNode.setAttribute(FileProperties.PROPERTY_SIZE, Long.toString(size));
        this.fileNode.setAttribute(FileProperties.PROPERTY_CONTENTTYPE, MIMEMapping.getMIMEType(extension));
        
        // set time flag
        Calendar value = new GregorianCalendar(TimeZone.getDefault());
        this.fileNode.setAttribute(FileProperties.PROPERTY_LASTMODIFIES, value);

        this.updateMetaData();
    }

    public void updateMetaData() {
        try {
            NodeDataUtil.getOrCreate(node,"title").setValue(this.getFileName());
            NodeDataUtil.getOrCreate(node,"type").setValue(this.getFileExtension().toLowerCase());
            NodeDataUtil.getOrCreate(node,"name").setValue(node.getName());

            // store the file type (for sorting)
            NodeDataUtil.getOrCreate(node,"type").setValue(this.getFileExtension().toLowerCase());

            // store a sortable date
            NodeDataUtil.getOrCreate(node,"creationDate").setValue(node.getMetaData().getCreationDate());
            NodeDataUtil.getOrCreate(node,"modificationDate").setValue(node.getMetaData().getModificationDate());

            if (StringUtils.isEmpty(NodeDataUtil.getOrCreate(node,"creator").getString())) {
                node.getNodeData("creator").setValue(node.getMetaData().getAuthorId());
            }

            NodeDataUtil.getOrCreate(node,"modifier").setValue(node.getMetaData().getAuthorId());

            // store the node name in the name field
            NodeDataUtil.getOrCreate(node,"title").setValue(this.getFileName());

            node.save();

        }
        catch (Exception e) {
            log.error("can't update the metadata of the document", e);
        }
    }

    public void save() throws RepositoryException {
        node.save();
    }

    /**
     * @return Returns the node.
     */
    public Content getNode() {
        return this.node;
    }

    /**
     * @param node The node to set.
     */
    protected void setNode(Content node) {
        this.node = node;

        try {
            this.fileNode = this.node.getNodeData(PROPERTY_FILEDATA);
        }
        catch (Exception e) {
        }
    }

}
