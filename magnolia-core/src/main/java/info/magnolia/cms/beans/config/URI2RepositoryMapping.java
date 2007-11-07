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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.link.UUIDLink;
import info.magnolia.cms.link.UUIDLinkException;

import org.apache.commons.lang.StringUtils;


/**
 * Describes a uri to repository mapping
 * @author Philipp Bracher
 * @version $Id$
 */
public class URI2RepositoryMapping {

    /**
     * The prefix which triggers this mapping
     */
    private String URIPrefix;

    /**
     * The repository used for this mapping
     */
    private String repository;

    /**
     * The prefix added to the uri to create a full handle
     */
    private String handlePrefix;

    /**
     * @param uriPrefix
     * @param repository
     * @param handlePrefix
     */
    public URI2RepositoryMapping(String uriPrefix, String repository, String handlePrefix) {
        this.URIPrefix = uriPrefix;
        this.repository = repository;
        this.handlePrefix = handlePrefix;
    }

    public URI2RepositoryMapping() {
    }

    /**
     * True if this mapping can get applied to the specified uri
     * @param uri
     * @return
     */
    public boolean matches(String uri) {
        return uri.startsWith(URIPrefix);
    }

    /**
     * Create a node handle based on an uri
     * @param uri
     * @return
     */
    public String getHandle(String uri) {
        String handle;
        handle = StringUtils.removeStart(uri, this.URIPrefix);
        if (StringUtils.isNotEmpty(this.handlePrefix)) {
            StringUtils.removeStart(handle, "/");
            handle = this.handlePrefix + "/" + handle;
        }
        //remove extension
        handle = StringUtils.substringBeforeLast(handle, ".");
        return cleanHandle(handle);
    }

    /**
     * Clean a handle. Remove double / and add allways a leading /
     * @param handle
     * @return
     */
    private String cleanHandle(String handle) {
        if (!handle.startsWith("/")) {
            handle = "/" + handle;
        }
        handle = StringUtils.replace(handle, "//", "/");
        return handle;
    }

    /**
     * Create a uri based on a handle
     * @param handle
     * @return
     */
    public String getURI(String handle) {
        try {
            return getURI(new UUIDLink().initWithHandle(this.getRepository(), handle));
        }
        catch (UUIDLinkException e) {
            return handle;
        }
    }

    public String getURI(UUIDLink uuidLink){
        String uri = uuidLink.getHandle();
        if (StringUtils.isNotEmpty(this.handlePrefix)) {
            uri = StringUtils.removeStart(uri, this.handlePrefix);
        }
        if (StringUtils.isNotEmpty(this.URIPrefix)) {
            uri = this.URIPrefix + "/" + uri;
        }

        String nodeDataName = uuidLink.getNodeDataName();
        String fileName = uuidLink.getFileName();
        String extension = uuidLink.getExtension();

        uri += (StringUtils.isNotEmpty(nodeDataName)? "/" + nodeDataName : "") +
                (StringUtils.isNotEmpty(fileName)? "/" + fileName : "") +
                (StringUtils.isNotEmpty(extension)? "." + extension : "");

        return cleanHandle(uri);
    }

    public String getHandlePrefix() {
        return handlePrefix;
    }

    public void setHandlePrefix(String handlePrefix) {
        this.handlePrefix = handlePrefix;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getURIPrefix() {
        return URIPrefix;
    }

    public void setURIPrefix(String uriPrefix) {
        this.URIPrefix = uriPrefix;
    }

    public String toString() {
        return this.URIPrefix + " --> " + repository + ":" + this.handlePrefix;
    }
}
