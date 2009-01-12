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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.context.MgnlContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author had
 *
 */
public class UUIDLink {
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
     * Pattern to find a magnolia formatted link
     */
    public static Pattern UUID_PATTERN = Pattern.compile(
        "\\$\\{link:\\{uuid:\\{([^\\}]*)\\}," // the uuid of the node
        + "repository:\\{([^\\}]*)\\},"
        + "(workspace:\\{[^\\}]*\\},)?" // is not supported anymore
        + "(path|handle):\\{([^\\}]*)\\}"        // fallback handle should not be used unless the uuid is invalid
        + "(,nodeData:\\{([^\\}]*)\\}," // in case we point to a binary (node data has no uuid!)
        + "extension:\\{([^\\}]*)\\})?" // the extension to be used in rendering
        + "\\}\\}"  // the handle
        + "(#([^\\?\"]*))?" // anchor
        + "(\\?([^\"]*))?"); // parameters

    protected static final Pattern LINK_PATTERN = Pattern.compile(
        "(/[^\\.\"#\\?]*)" + // the handle
        "(\\.([\\w[^#\\?]]+))?" + // extension (if any)
        "(#([^\\?\"]*))?" + // anchor
        "(\\?([^\"]*))?" // parameters
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

    public UUIDLink parseUUIDLink(String uuidLink) throws UUIDLinkException{
        Matcher matcher = UUID_PATTERN.matcher(uuidLink);
        if(matcher.matches()){
            initByUUIDPatternMatcher(matcher);
        }
        else{
            throw new UUIDLinkException("can't parse [ " + uuidLink + "]");
        }
        return this;
    }

    UUIDLink initByUUIDPatternMatcher(Matcher matcher) {
        uuid = matcher.group(1);
        repository = StringUtils.defaultIfEmpty(matcher.group(2), ContentRepository.WEBSITE);
        fallbackHandle = matcher.group(5);
        nodeDataName = matcher.group(7);
        extension = matcher.group(8);
        anchor = matcher.group(10);
        parameters = matcher.group(12);
        return this;
    }

    public UUIDLink parseLink(String link) throws UUIDLinkException{
        // ignore context handle if existing
        link = StringUtils.removeStart(link, MgnlContext.getContextPath());

        Matcher matcher = LINK_PATTERN.matcher(link);
        if(matcher.matches()){
            String orgHandle = matcher.group(1);
            orgHandle = I18nContentSupportFactory.getI18nSupport().toRawURI(orgHandle);
            String repository = URI2RepositoryManager.getInstance().getRepository(orgHandle);
            String handle = URI2RepositoryManager.getInstance().getHandle(orgHandle);
            init(repository, handle, matcher.group(3),matcher.group(5),matcher.group(7));
        }
        else{
            throw new UUIDLinkException("can't parse [ " + link + "]");
        }
        return this;
    }

    public UUIDLink initWithHandle(String repository, String handle) throws UUIDLinkException{
        init(repository, handle, null, null, null);
        return this;
    }

    protected void init(String repository, String path, String extension, String anchor, String parameters) throws UUIDLinkException {
        this.repository = repository;
        this.extension = extension;
        this.anchor = anchor;
        this.parameters = parameters;

        try {
            HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
            if (hm.isExist(path) && !hm.isNodeData(path)) {
                node = hm.getContent(path);
            }
            if (node == null) {
                // this is a binary containing the name at the end
                // this name is stored as an attribute but is not part of the handle
                if (hm.isNodeData(StringUtils.substringBeforeLast(path, "/"))) {
                    fileName = StringUtils.substringAfterLast(path, "/");
                    path = StringUtils.substringBeforeLast(path, "/");
                }

                // link to the binary node data
                if (hm.isNodeData(path)) {
                    nodeDataName = StringUtils.substringAfterLast(path, "/");
                    path = StringUtils.substringBeforeLast(path, "/");
                    node = hm.getContent(path);
                    nodeData = node.getNodeData(nodeDataName);
                }
            }
            if (node != null) {
                uuid = node.getUUID();
            }
            this.handle = path;
        }
        catch (RepositoryException e) {
            throw new UUIDLinkException("can't find node " + path + " in repository " + repository, e);
        }

        if(node == null){
            throw new UUIDLinkException("can't find node " + path + " in repository " + repository);
        }
    }

    public String toPattern(){
        return "${link:{"
            + "uuid:{" + getUUID() + "},"
            + "repository:{" + getRepository() + "},"
            + "handle:{" + getHandle() + "}," // original handle represented by the uuid
            + "nodeData:{" + StringUtils.defaultString(getNodeDataName()) + "}," // in case of binaries
            + "extension:{" + StringUtils.defaultString(getExtension()) + "}" // the extension to use if no extension can be resolved otherwise
            + "}}"
            + (StringUtils.isNotEmpty(getAnchor())? "#" + getAnchor():"")
            + (StringUtils.isNotEmpty(getParameters())? "?" + getParameters() : "");
    }


    public String getExtension() {
        if(StringUtils.isEmpty(this.extension) && this.getNodeData() != null){
            if(this.getNodeData().getType() == PropertyType.BINARY){
                File binary = new File(nodeData);
                extension = binary.getExtension();
            }
        }
        return StringUtils.defaultIfEmpty(this.extension, ServerConfiguration.getInstance().getDefaultExtension());
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
                    log.warn("can't get node with uuid [{}] will try stored handle [{}]", new String[]{uuid, handle});
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
