/**
 * This file Copyright (c) 2007 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 * 
 * Any modifications to this file must keep this entire header
 * intact.
 * 
 */
package info.magnolia.setup.for3_5;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllChildrenNodesOperation;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

/**
 * Currently only checks for modifications between current filter configuration and the 3.0 default configuration.
 * If there are some, a warning is displayed.
 * 
 * TODO: transform bypass configuration to new format if that's the only change.
 * 
 * @author vsteller
 * @version $Id$
 *
 */
public final class CheckAndUpdateExistingFilters extends AllChildrenNodesOperation {
    private final LinkedHashMap filterChain30 = new LinkedHashMap();
    private final String existingFiltersPath;
    
    public CheckAndUpdateExistingFilters(String existingFiltersPath) {
        super("Filters", "Installs or updates the new filter configuration.", ContentRepository.CONFIG, existingFiltersPath);
        this.existingFiltersPath = existingFiltersPath;

        // filter chain that is bootstrapped with latest Magnolia 3.0.x
        filterChain30.put("contentType",    
            new Filter30("info.magnolia.cms.filters.ContentTypeFilter",     
                Long.valueOf(100)));
        filterChain30.put("security",       
            new Filter30("info.magnolia.cms.security.SecurityFilter",       
                Long.valueOf(200)));
        filterChain30.put("virtualURI",     
            new Filter30("info.magnolia.cms.filters.MgnlVirtualUriFilter",  
                Long.valueOf(300)));
        filterChain30.put("multipartRequest", 
            new Filter30("info.magnolia.cms.filters.MultipartRequestFilter", 
                Long.valueOf(400)));
        filterChain30.put("context", 
            new Filter30("info.magnolia.cms.filters.MgnlContextFilter", 
                Long.valueOf(500)));
        final HashMap interceptFilterParams = new HashMap();
        interceptFilterParams.put("test", "true");
        filterChain30.put("intercept", 
            new Filter30("info.magnolia.cms.filters.MgnlInterceptFilter", 
                Long.valueOf(600),
                null,
                interceptFilterParams));
        filterChain30.put("cms", 
            new Filter30("info.magnolia.cms.filters.MgnlCmsFilter", 
                Long.valueOf(800),
                "/.,/docroot/,/admindocroot/,/tmp/fckeditor/,/ActivationHandler"));
    }

    protected void operateOnChildNode(Content node, InstallContext ctx) throws RepositoryException,
        TaskExecutionException {
        
        try {
            final Map existingFilter = Content2BeanUtil.toPureMaps(node, true);
            final String currentFilter = node.getName();
            final CheckAndUpdateExistingFilters.Filter30 originalFilter = (CheckAndUpdateExistingFilters.Filter30) filterChain30.get(currentFilter);
            
            if (originalFilter == null || 
                hasClassChanged(originalFilter, existingFilter) || 
                hasPriorityChanged(originalFilter, existingFilter) || 
                hasParamsChanged(originalFilter, existingFilter)) {
                ctx.warn("Existing configuration of filter '" + currentFilter + "' has been modified or was not existing in original filter chain. Magnolia put a backup in " + existingFiltersPath + "/" + currentFilter + ". Please review the changes manually.");
            } 
            
            if (originalFilter != null && hasBypassChanged(originalFilter, existingFilter)) {
                // TODO: transform the old bypasses to the new ones
                ctx.warn("Existing configuration of filter '" + currentFilter + "' has different bypass definitions. Magnolia put a backup in " + existingFiltersPath + "/" + currentFilter + ". Please review the changes manually.");
            }
        } catch (Content2BeanException e) {
            ctx.error("Cannot convert filter node to map", e);
        }
    }

    private boolean hasClassChanged(final CheckAndUpdateExistingFilters.Filter30 originalFilter, final Map existingFilter) {
        return !originalFilter.clazz.equals(existingFilter.get("class"));
    }
    
    private boolean hasBypassChanged(final CheckAndUpdateExistingFilters.Filter30 originalFilter, final Map existingFilter) {
        final String bypasses = getBypasses(existingFilter);
        return !originalFilter.equalBypasses(bypasses);
    }

    private String getBypasses(final Map existingFilter) {
        String bypasses = null;
        final Map existingConfig = (Map) existingFilter.get("config");
        if (existingConfig != null && existingConfig.containsKey("bypass"))
            bypasses = (String) existingConfig.get("bypass");
        return bypasses;
    }

    private boolean hasParamsChanged(final CheckAndUpdateExistingFilters.Filter30 originalFilter, final Map existingFilter) {
        final Map existingParameters = ((Map) existingFilter.get("params"));
        return !originalFilter.equalParams(existingParameters); 
    }

    private boolean hasPriorityChanged(final CheckAndUpdateExistingFilters.Filter30 originalFilter, final Map existingFilter) {
        return !originalFilter.priority.equals(existingFilter.get("priority"));
    }

    private static final class Filter30 {
        private String clazz;
        private Long priority;
        private String bypasses;
        private Map params;
        
        public Filter30(String clazz, Long priority) {
            super();
            this.clazz = clazz;
            this.priority = priority;
        }
        
        public Filter30(String clazz, Long priority, String bypasses) {
            super();
            this.clazz = clazz;
            this.priority = priority;
            this.bypasses = bypasses;
        }

        public Filter30(String clazz, Long priority, String bypasses, Map params) {
            super();
            this.clazz = clazz;
            this.priority = priority;
            this.bypasses = bypasses;
            this.params = params;
        }

        /**
         * Compares this filters parameters to the given map of parameters.
         * @param existingFilterParams map of filter parameters
         * @return true if they are equal, otherwise false
         */
        private boolean equalParams(final Map existingFilterParams) {
            if (params == null && existingFilterParams == null)
                return true;
            if (params == null || existingFilterParams == null)
                return false;
            if (params.size() != existingFilterParams.size())
                return false;
                
            final Iterator paramIterator = params.keySet().iterator();
            while (paramIterator.hasNext()) {
                final String currentParam = (String) paramIterator.next();
                if (!existingFilterParams.containsKey(currentParam) || !existingFilterParams.get(currentParam).equals(params.get(currentParam)))
                    return false;
            }
            return true;
        }

        private boolean equalBypasses(final String existingBypasses) {
            if (bypasses != null) {
                return bypasses.equals(existingBypasses);
            } else {
                return existingBypasses == null;
            }
        }
    }
}