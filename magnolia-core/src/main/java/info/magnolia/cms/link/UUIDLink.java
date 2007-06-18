/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.link;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.MgnlContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class UUIDLink{
    private String repository;
    private String handle;
    private String uuid;
    private String nodeDataName;
    private String extension;
    private Content node;
    private NodeData nodeData;
    private String fileName;
    private String fallbackHandle;

    /**
     * Pattern to find a magnolia formatted link
     */
    public static Pattern UUID_PATTERN = Pattern.compile(
        "\\$\\{link:\\{uuid:\\{([^\\}]*)\\}," // the uuid of the node
        + "repository:\\{([^\\}]*)\\},"
        + "(workspace:\\{[^\\}]*\\},)?" // is not supported anymore
        + "(path|handle):\\{([^\\}]*)\\}"        // fallback handle should not be used unless the uuid is invalid
        + "(,nodeData:\\{([^\\}]*)\\}," // in case we point to a binary (node data has no uuid!)
        + "extension:\\{([^\\}]*)\\})?" // the extension to be used in rendering
        +"\\}\\}"); // the handle

    protected static final Pattern LINK_PATTERN = Pattern.compile(
        "(/[^\\.\"#]*)" + // the handle
        "(\\.(\\w+))?" + // extension (if any)
        "(#([^\\?\"])*)?" + // anchor
        "(\\?([^\"])*)?" // parameters
    );

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(UUIDLink.class);

    /**
     * Use parseUUIDLink() or parseLink() to initialize the link object
     */
    public UUIDLink() {
    }

    public UUIDLink parseUUIDLink(String uuidLink){
        Matcher matcher = UUID_PATTERN.matcher(uuidLink);
        if(matcher.matches()){
            uuid = matcher.group(1);
            repository = StringUtils.defaultIfEmpty(matcher.group(2), ContentRepository.WEBSITE);
            fallbackHandle = matcher.group(5);
            nodeDataName = matcher.group(7);
            extension = matcher.group(8);
        }
        else{
            log.error("can't parse [{}]", uuidLink);
        }
        return this;
    }

    public UUIDLink parseLink(String link){
        // ignore context handle if existing
        link = StringUtils.removeStart(link, MgnlContext.getContextPath());

        Matcher matcher = LINK_PATTERN.matcher(link);
        if(matcher.matches()){
            handle = URI2RepositoryManager.getInstance().getHandle(matcher.group(1));
            repository = URI2RepositoryManager.getInstance().getRepository(handle);
            extension = StringUtils.defaultIfEmpty(matcher.group(3), Server.getDefaultExtension());

            try {
                HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
                if(hm.isExist(handle)){
                    node = hm.getContent(handle);
                }
                if(node == null){
                    // this is a binary containing the name at the end
                    // this name is stored as an attribute but is not part of the handle
                    if(hm.isNodeData(StringUtils.substringBeforeLast(handle, "/"))){
                        fileName = StringUtils.substringAfterLast(handle, "/");
                        handle = StringUtils.substringBeforeLast(handle, "/");
                    }

                    // link to the binary node data
                    if(hm.isNodeData(handle)){
                        nodeDataName = StringUtils.substringAfterLast(handle, "/");
                        handle = StringUtils.substringBeforeLast(handle, "/");
                        node = hm.getContent(handle);
                        nodeData = node.getNodeData(nodeDataName);
                    }
                }
                if(node != null){
                    uuid = node.getUUID();
                }
            }
            catch (RepositoryException e) {
                log.error("can't parse link [" + link+ "]", e);
            }
        }
        else{
            log.error("can't parse [{}]", link);
        }
        return this;
    }

    public String toPattern(){
        return "${link:{"
            + "uuid:{" + getUUID() + "},"
            + "repository:{" + getRepository() + "},"
            + "handle:{" + getHandle() + "}," // original handle represented by the uuid
            + "nodeData:{" + StringUtils.defaultString(getNodeDataName()) + "}," // in case of binaries
            + "extension:{" + StringUtils.defaultString(getExtension()) + "}" // the extension to use if no extension can be resolved otherwise
            + "}}";
    }


    public String getExtension() {
        if(StringUtils.isEmpty(this.extension) && this.getNodeData() != null){
            if(this.getNodeData().getType() == PropertyType.BINARY){
                File binary = new File(nodeData);
                extension = binary.getExtension();
            }
        }
        return this.extension;
    }


    public void setExtension(String extension) {
        this.extension = extension;
    }


    public String getFileName() {
        if(StringUtils.isEmpty(this.fileName) && this.getNodeData() != null){
            if(this.getNodeData().getType() == PropertyType.BINARY){
                File binary = new File(nodeData);
                fileName = binary.getFileName();
            }
        }
        return fileName;
    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public Content getNode() {

        if(this.node == null){
            HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
            if(StringUtils.isNotEmpty(uuid)){
                try {
                    node = hm.getContentByUUID(uuid);
                }
                catch (RepositoryException e) {
                    log.warn("can't get node with uuid [{}] will try stored handle", uuid);
                }
            }

            // uuid is not defined or resolving by uuid failed
            if (this.node == null && StringUtils.isNotEmpty(handle)){
                try {
                    node = hm.getContent(handle);
                }
                catch (RepositoryException e1) {
                    log.warn("can't read node by using handle [{}]", handle);
                }
            }
        }
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
            }
            else{
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

}

