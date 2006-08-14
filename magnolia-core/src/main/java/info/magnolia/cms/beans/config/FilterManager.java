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
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;

import java.util.Map;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.Filter;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventIterator;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class FilterManager {

    /**
     * config path
     * */
    public static final String SERVER_FILTERS = "/server/filters";

    /**
     * Logger
     * */
    protected Logger log = LoggerFactory.getLogger(getClass());

    /**
     * singleton instance
     * */
    private static FilterManager instance = (FilterManager) FactoryUtil.getSingleton(FilterManager.class);;

    private Filter[] filterChain;

    /**
     * Do not instantiate yourself
     */
    public FilterManager() {
        init();
        ObservationUtil.registerChangeListener(
            ContentRepository.CONFIG,
            SERVER_FILTERS,
            new EventListener() {
                public void onEvent(EventIterator arg0) {
                    MgnlContext.setInstance(MgnlContext.getSystemContext());
                    init();
                }
            });
    }

    public static FilterManager getInstance() {
        return instance;
    }

    /**
     * @return array of filters as configured
     * */
    public Filter[] getFilters() {
        return filterChain;
    }

    /**
     * initialized filter chain
     * */
    protected void init() {
        Content node = ContentUtil.getContent(ContentRepository.CONFIG, SERVER_FILTERS);
        if (node != null) {
            Collection children = node.getChildren(ItemType.CONTENT);
            this.filterChain = new Filter[children.size()];
            Iterator childIterator = children.iterator();
            int index = 0;
            while (childIterator.hasNext()) {
                Content child = (Content) childIterator.next();
                // ignore priority since all filters are under server config
                String classPath = child.getNodeData("class").getString();
                if (StringUtils.isNotEmpty(classPath)) {
                    if (log.isDebugEnabled()) {
                        log.debug(new StringBuffer().append("Adding filter [ ")
                                .append(classPath)
                                .append(" ] to managed filter list").toString());
                    }
                    try {
                        this.filterChain[index++] = (Filter) Class.forName(classPath).newInstance();
                    } catch (Throwable e) {
                        log.error("Failed to add filter [ "+classPath+" ]", e);
                    }
                }
            }
        }
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
