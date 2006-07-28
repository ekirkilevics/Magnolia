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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.context.MgnlContext;


/**
 * Mapps uri prefixes to repositories
 * @author Philipp Bracher
 * @version $Id$
 */
public class URI2RepositoryManager {

    public static final String SERVER_REPOSITORY_URIMAPPING = "/server/URI2RepositoryMapping";

    private static URI2RepositoryManager instance = (URI2RepositoryManager) FactoryUtil
        .getSingleton(URI2RepositoryManager.class);

    /**
     * The mappings
     */
    private List mappings = new ArrayList();

    /**
     * Mapping used if none configured
     */
    private URI2RepositoryMapping defaultMapping = new URI2RepositoryMapping("", ContentRepository.WEBSITE, "");

    /**
     * First initialization and starting observation of the config node.
     */
    public URI2RepositoryManager() {
        init();
        ObservationUtil.registerChangeListener(
            ContentRepository.CONFIG,
            SERVER_REPOSITORY_URIMAPPING,
            new EventListener() {

                public void onEvent(EventIterator arg0) {
                    MgnlContext.setInstance(MgnlContext.getSystemContext());
                    init();
                }
            });
    }

    /**
     * Add the mappings found in the config repository
     */
    public void init() {
        this.mappings.clear();
        Content node = ContentUtil.getContent(ContentRepository.CONFIG, SERVER_REPOSITORY_URIMAPPING);
        if (node != null) {
            for (Iterator iter = node.getChildren(ItemType.CONTENTNODE).iterator(); iter.hasNext();) {
                Content mappingNode = (Content) iter.next();
                URI2RepositoryMapping mapping = new URI2RepositoryMapping();
                mapping.setRepository(NodeDataUtil.getString(mappingNode, "repository", ""));
                mapping.setUriPrefix(NodeDataUtil.getString(mappingNode, "URIPrefix", ""));
                mapping.setHandlePrefix(NodeDataUtil.getString(mappingNode, "handlePrefix", ""));
                this.addMapping(mapping);
            }
        }

        // check first the longer prefixes
        Collections.sort(this.mappings, new Comparator() {

            public int compare(Object arg0, Object arg1) {
                URI2RepositoryMapping m0 = (URI2RepositoryMapping) arg0;
                URI2RepositoryMapping m1 = (URI2RepositoryMapping) arg1;
                return m1.getUriPrefix().length() - m0.getUriPrefix().length();
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
        return this.defaultMapping;
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
     * @param repository
     * @param handle
     * @return
     */
    public String getURI(String repository, String handle) {
        for (Iterator iter = mappings.iterator(); iter.hasNext();) {
            URI2RepositoryMapping mapping = (URI2RepositoryMapping) iter.next();
            if (StringUtils.equals(mapping.getRepository(), repository) && handle.startsWith(mapping.getHandlePrefix())) {
                return mapping.getURI(handle);
            }
        }
        return handle;
    }

    public void addMapping(URI2RepositoryMapping mapping) {
        mappings.add(mapping);
    }

    public static URI2RepositoryManager getInstance() {
        return instance;
    }

}
