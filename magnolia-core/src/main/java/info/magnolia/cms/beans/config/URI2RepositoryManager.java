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

import info.magnolia.cms.link.UUIDLink;
import info.magnolia.cms.link.UUIDLinkException;
import info.magnolia.cms.util.FactoryUtil;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Mapps uri prefixes to repositories
 * @author Philipp Bracher
 * @version $Id$
 */
public class URI2RepositoryManager {

    protected static final URI2RepositoryMapping DEFAULT_MAPPING = new URI2RepositoryMapping("", ContentRepository.WEBSITE,"");

    private static Logger log = LoggerFactory.getLogger(URI2RepositoryManager.class);

    /**
     * The mappings
     */
    private Collection mappings;

    public URI2RepositoryManager() {
        mappings = new TreeSet(new Comparator() {
            public int compare(Object arg0, Object arg1) {
                URI2RepositoryMapping m0 = (URI2RepositoryMapping) arg0;
                URI2RepositoryMapping m1 = (URI2RepositoryMapping) arg1;
                return m1.getURIPrefix().length() - m0.getURIPrefix().length();
            }
        });

    }

    /**
     * The mapping to use for this uri
     */
    public URI2RepositoryMapping getMapping(String uri) {
        for (Iterator iter = mappings.iterator(); iter.hasNext();) {
            URI2RepositoryMapping mapping = (URI2RepositoryMapping) iter.next();
            if (mapping.matches(uri)) {
                return mapping;
            }
        }
        return this.getDefaultMapping();
    }

    public URI2RepositoryMapping getDefaultMapping() {
        return DEFAULT_MAPPING;
    }

    /**
     * Get the handle for this uri
     * @param uri
     * @return
     */
    public String getHandle(String uri) {
        return this.getMapping(uri).getHandle(uri);
    }

    /**
     * Get the repository to use for this uri
     * @param uri
     * @return
     */
    public String getRepository(String uri) {
        return this.getMapping(uri).getRepository();
    }

    /**
     * Get the uri to use for this handle
     */
    public String getURI(String repository, String handle) {
        try {
            return getURI(new UUIDLink().initWithHandle(repository, handle));
        }
        catch (UUIDLinkException e) {
            log.error("can't map [" + handle + "] to a uri", e);
        }
        return handle;
    }

    public String getURI(UUIDLink uuidLink) {
        for (Iterator iter = mappings.iterator(); iter.hasNext();) {
            URI2RepositoryMapping mapping = (URI2RepositoryMapping) iter.next();
            if (StringUtils.equals(mapping.getRepository(), uuidLink.getRepository()) && uuidLink.getHandle().startsWith(mapping.getHandlePrefix())) {
                return mapping.getURI(uuidLink);
            }
        }
        return this.getDefaultMapping().getURI(uuidLink);
    }

    public void addMapping(URI2RepositoryMapping mapping) {
        mappings.add(mapping);
    }

    public static URI2RepositoryManager getInstance() {
        return (URI2RepositoryManager) FactoryUtil.getSingleton(URI2RepositoryManager.class);
    }

    /**
     * @return the mappings
     */
    public Collection getMappings() {
        return this.mappings;
    }

}
