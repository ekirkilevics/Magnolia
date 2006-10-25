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
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.context.MgnlContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages configuration of webapp filters, stored in jcr. Configured filters are loaded and used by
 * {@link info.magnolia.cms.filters.MagnoliaManagedFilter}.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public final class FilterManager {

    /**
     * Name for the config node that contains filter initialization parameters.
     */
    private static final String PARAM_CONFIG = "config";

    /**
     * Name for the parameters that contains the priority.
     */
    private static final String PARAM_PRIORITY = "priority";

    /**
     * Name for the parameters that contains the filter class.
     */
    private static final String PARAM_FILTERCLASS = "class";

    /**
     * config path
     */
    private static final String SERVER_FILTERS = "/server/filters";

    /**
     * Logger
     */
    protected Logger log = LoggerFactory.getLogger(getClass());

    private Filter[] filterChain;

    /**
     * Array of filter definitions.
     */
    private FilterDefinition[] filterDefinitions;

    /**
     * Filterconfig set by the *real* filter.
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
    public Filter[] getFilters() {
        return filterChain;
    }

    /**
     * Getter for <code>filterDefinitions</code>.
     * @return Returns the filterDefinitions.
     */
    public FilterDefinition[] getFilterDefinitions() {
        return this.filterDefinitions;
    }

    /**
     * initialized filter chain
     */
    protected void init() {

        extractDefinitions();

        this.filterChain = new Filter[filterDefinitions.length];

        for (int j = 0; j < filterDefinitions.length; j++) {
            FilterDefinition definition = filterDefinitions[j];
            try {
                Filter filter = (Filter) ClassUtil.newInstance(definition.getClassName());
                this.filterChain[j] = filter;
            }
            catch (Throwable e) {
                log.error("Failed to instantiate filter [ " + definition.getClassName() + " ]", e);

                // remove definition!
                filterDefinitions = (FilterDefinition[]) ArrayUtils.remove(filterDefinitions, j);
                j--;
            }
        }

        // the first initialization is done by the filter, but this is needed for reload
        // not quite a clean design, but working...
        if (filterConfig != null) {
            initFilters(filterConfig);
        }
    }

    /**
     *
     */
    private void extractDefinitions() {
        List definitionList = new ArrayList();

        Content node = ContentUtil.getContent(ContentRepository.CONFIG, SERVER_FILTERS);

        if (node != null) {

            Collection children = node.getChildren(ItemType.CONTENT);

            Iterator childIterator = children.iterator();

            while (childIterator.hasNext()) {
                Content child = (Content) childIterator.next();

                String filterClass = child.getNodeData(PARAM_FILTERCLASS).getString();

                if (StringUtils.isNotEmpty(filterClass)) {
                    FilterDefinition definition = new FilterDefinition();
                    definition.setClassName(filterClass);
                    definition.setPriority(child.getNodeData(PARAM_PRIORITY).getLong());

                    try {
                        if (child.hasContent(PARAM_CONFIG)) {
                            Content config = child.getContent(PARAM_CONFIG);
                            definition.setParameters(ContentUtil.toMap(config));
                        }
                    }
                    catch (RepositoryException e) {
                        log.error("Unable to read config parameters for filter {} due to a ", filterClass, e
                            .getClass()
                            .getName());
                    }

                    log.debug("Adding filter [{}] to managed filter list", filterClass);

                    definitionList.add(definition);

                }
            }
        }

        Collections.sort(definitionList);

        filterDefinitions = (FilterDefinition[]) definitionList.toArray(new FilterDefinition[definitionList.size()]);
    }

    /**
     * @param filterConfig
     * @throws ServletException
     */
    public void initFilters(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        for (int j = 0; j < getFilterDefinitions().length; j++) {
            FilterDefinition filter = getFilterDefinitions()[j];
            FilterManager.CustomFilterConfig customFilterConfig = new FilterManager.CustomFilterConfig(
                filterConfig,
                filter.getParameters());

            try {
                getFilters()[j].init(customFilterConfig);
            }
            catch (ServletException e) {
                log.error("Error initializing filter " + filter.getClassName(), e);
            }

            log.info("Initializing filter {}", filter.getClassName());
        }
    }

    /**
     * Represents a configured filter.
     * @author fgiust
     * @version $Revision$ ($Author$)
     */
    public static class FilterDefinition implements Comparable {

        /**
         * Filter class name.
         */
        private String className;

        /**
         * Priority, lower is first.
         */
        private long priority;

        /**
         * Optional parameters.
         */
        private Map parameters;

        /**
         * Getter for <code>className</code>.
         * @return Returns the className.
         */
        public String getClassName() {
            return this.className;
        }

        /**
         * Setter for <code>className</code>.
         * @param className The className to set.
         */
        public void setClassName(String className) {
            this.className = className;
        }

        /**
         * Getter for <code>parameters</code>.
         * @return Returns the parameters.
         */
        public Map getParameters() {
            return this.parameters;
        }

        /**
         * Setter for <code>parameters</code>.
         * @param parameters The parameters to set.
         */
        public void setParameters(Map parameters) {
            this.parameters = parameters;
        }

        /**
         * Getter for <code>priority</code>.
         * @return Returns the priority.
         */
        public long getPriority() {
            return this.priority;
        }

        /**
         * Setter for <code>priority</code>.
         * @param priority The priority to set.
         */
        public void setPriority(long priority) {
            this.priority = priority;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(Object object) {
            FilterDefinition myClass = (FilterDefinition) object;
            return new CompareToBuilder().append(this.priority, myClass.priority).toComparison();
        }

    }

    public static class CustomFilterConfig implements FilterConfig {

        private Map parameters;

        private FilterConfig parent;

        /**
         * @param parameters
         */
        public CustomFilterConfig(FilterConfig parent, Map parameters) {
            super();
            this.parent = parent;
            if (parameters != null) {
                this.parameters = parameters;
            }
            else {
                this.parameters = new HashMap();
            }
        }

        /**
         * @see javax.servlet.FilterConfig#getFilterName()
         */
        public String getFilterName() {
            return parent.getFilterName();
        }

        /**
         * @see javax.servlet.FilterConfig#getInitParameter(java.lang.String)
         */
        public String getInitParameter(String name) {
            return (String) parameters.get(name);
        }

        /**
         * @see javax.servlet.FilterConfig#getInitParameterNames()
         */
        public Enumeration getInitParameterNames() {
            return new Hashtable(parameters).keys();
        }

        /**
         * @see javax.servlet.FilterConfig#getServletContext()
         */
        public ServletContext getServletContext() {
            return parent.getServletContext();
        }

    }

}
