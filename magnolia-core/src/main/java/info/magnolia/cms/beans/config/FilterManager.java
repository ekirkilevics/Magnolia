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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.filters.MagnoliaFilter;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages configuration of webapp filters, stored in jcr. Configured filters are loaded and used by
 * {@link info.magnolia.cms.filters.MagnoliaMainFilter}.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public final class FilterManager {

    /**
     * config path
     */
    private static final String SERVER_FILTERS = "/server/filters";

    /**
     * Logger
     */
    protected Logger log = LoggerFactory.getLogger(getClass());

    private MagnoliaFilter[] filterChain;

    /**
     * Remember the main filters config
     */
    private FilterConfig filterConfig;

    /**
     * Do not instantiate yourself
     */
    public FilterManager() {
        init();
        ObservationUtil.registerChangeListener(ContentRepository.CONFIG, SERVER_FILTERS, new EventListener() {

            public void onEvent(EventIterator arg0) {
                MgnlContext.setInstance(MgnlContext.getSystemContext());
                init();
            }
        });
    }

    /**
     * Returns the FilterManager instance.
     * @return FilterManager instance.
     */
    public static FilterManager getInstance() {
        return (FilterManager) FactoryUtil.getSingleton(FilterManager.class);
    }

    /**
     * @return array of filters as configured
     */
    public MagnoliaFilter[] getFilters() {
        return filterChain;
    }

    /**
     * initialized filter chain
     */
    protected void init() {
        Content node;

        List filters = new ArrayList();
        try {
            node = MgnlContext.getSystemContext()
            .getHierarchyManager(ContentRepository.CONFIG).getContent(SERVER_FILTERS);

            // for convinience we sort the nodes by priority
            orderFilterNodes(node);

            Content2BeanUtil.setProperties(filters, node, true);
        }
        catch (RepositoryException e) {
            log.error("can't read filter definitions", e);
            return;
        }
        catch (Content2BeanException e) {
            log.error("can't create filter objects", e);
        }
        Collections.sort(filters, PRIORITY_COMPERATOR);

        filterChain = (MagnoliaFilter[]) filters.toArray(new MagnoliaFilter[filters.size()]);

        // in case it is a reloading
        if(this.filterConfig != null){
            initFilters(this.filterConfig);
        }
    }

    protected void orderFilterNodes(Content node) throws RepositoryException {
        ContentUtil.orderNodes(node, new Comparator(){

            public int compare(Object o1, Object o2) {
                Content c1 =(Content)o1;
                Content c2 =(Content)o2;

                int prio1 = (int) NodeDataUtil.getLong(c1, "priority", Integer.MAX_VALUE);
                int prio2 = (int) NodeDataUtil.getLong(c2, "priority", Integer.MAX_VALUE);

                return prio1 - prio2;
            }

        });
    }

    /**
     * The first time called by the main filter.
     */
    public void initFilters(FilterConfig filterConfig) {
        // remember this config
        this.filterConfig = filterConfig;

        for (int j = 0; j < filterChain.length; j++) {
            MagnoliaFilter filter = filterChain[j];

            try {
                filter.init(filterConfig);
            }
            catch (ServletException e) {
                log.error("Error initializing filter [" + filter.getName() + "]", e);
            }

            log.info("Initializing filter [{}]", filter.getName());
        }
    }

    static final Comparator PRIORITY_COMPERATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            return ((MagnoliaFilter) o1).getPriority() - ((MagnoliaFilter) o2).getPriority();
        }
    };

}
