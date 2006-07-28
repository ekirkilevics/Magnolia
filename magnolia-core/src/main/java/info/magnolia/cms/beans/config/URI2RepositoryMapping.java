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

import org.apache.commons.lang.StringUtils;

/**
 * Describes a mapping
 * @author Philipp Bracher
 * @version $Id$
 */
public class URI2RepositoryMapping{
    /**
     * The prefix which triggers this mapping
     */
    private String uriPrefix;
    
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
        super();
        this.uriPrefix = uriPrefix;
        this.repository = repository;
        this.handlePrefix = handlePrefix;
    }
    
    public URI2RepositoryMapping() {
        // TODO Auto-generated constructor stub
    }

    public boolean matches(String uri){
        return uri.startsWith(uriPrefix);
    }
    
    public String getHandle(String uri){
        String handle;
        handle = StringUtils.removeStart(uri, this.uriPrefix);
        if(StringUtils.isNotEmpty(this.handlePrefix)){
            handle = this.handlePrefix + "/" + handle;
        }
        return handle;
    }

    public String getURI(String handle) {
        if(StringUtils.isNotEmpty(this.handlePrefix)){
            handle = StringUtils.removeStart(handle, this.handlePrefix);
        }
        if(StringUtils.isNotEmpty(this.uriPrefix)){
            handle = this.uriPrefix + "/" + handle;
        }
        return handle;
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

    
    public String getUriPrefix() {
        return uriPrefix;
    }

    
    public void setUriPrefix(String uriPrefix) {
        this.uriPrefix = uriPrefix;
    }
    
    public String toString() {
        return this.uriPrefix + " --> " + repository + ":" +  this.handlePrefix;
    }
}
