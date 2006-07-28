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
import info.magnolia.cms.filters.ContentTypeFilter;
import info.magnolia.cms.filters.MgnlCmsFilter;
import info.magnolia.cms.filters.MgnlContextFilter;
import info.magnolia.cms.filters.MgnlInterceptFilter;
import info.magnolia.cms.filters.MgnlVirtualUriFilter;
import info.magnolia.cms.filters.MultipartRequestFilter;
import info.magnolia.cms.security.SecurityFilter;

import java.util.Map;

import javax.servlet.Filter;

import org.apache.commons.lang.builder.CompareToBuilder;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class FilterManager extends ObservedManager {

    private static FilterManager instance = new FilterManager();

    private Filter[] filterChain = new Filter[]{ //
    new ContentTypeFilter(), // 100
        new MgnlVirtualUriFilter(), // 200
        new MultipartRequestFilter(), // 300
        new SecurityFilter(), // 400
        new MgnlContextFilter(), // 500
        new MgnlInterceptFilter(), // 600
        new MgnlCmsFilter()}; // 700

    private FilterManager() {
        // private constructor
    }

    public static FilterManager getInstance() {
        if (instance == null) {
            instance = new FilterManager();
        }
        return instance;
    }

    public Filter[] getFilters() {
        return filterChain;
    }

    /**
     * @see info.magnolia.cms.beans.config.ObservedManager#onClear()
     */
    protected void onClear() {
        // @todo clean filters?

    }

    /**
     * @see info.magnolia.cms.beans.config.ObservedManager#onRegister(info.magnolia.cms.core.Content)
     */
    protected void onRegister(Content node) {
        // @todo load filters

        log.info("Config: loading Filters from {}", node.getHandle()); //$NON-NLS-1$

    }

    class FilterDefinition implements Comparable {

        private String className;

        private int priority;

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
        public int getPriority() {
            return this.priority;
        }

        /**
         * Setter for <code>priority</code>.
         * @param priority The priority to set.
         */
        public void setPriority(int priority) {
            this.priority = priority;
        }

        /**
         * @see java.lang.Comparable#compareTo(Object)
         */
        public int compareTo(Object object) {
            FilterDefinition myClass = (FilterDefinition) object;
            return new CompareToBuilder().append(this.parameters, myClass.parameters).append(
                this.className,
                myClass.className).append(this.priority, myClass.priority).toComparison();
        }

    }
}
