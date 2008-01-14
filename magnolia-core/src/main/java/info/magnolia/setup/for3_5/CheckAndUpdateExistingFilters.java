/**
 * This file Copyright (c) 2007-2008 Magnolia International
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
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.setup.AddFilterBypassTask;
import info.magnolia.voting.voters.URIStartsWithVoter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Checks for modifications between current filter configuration and the 3.0 default configuration.
 * If there are some, a warning is displayed.
 * 
 * Bypass configurations are transformed to the new format if that's the only change.
 * 
 * TODO deletion of filters is not detected.
 * 
 * @author vsteller
 * @version $Id$
 *
 */
public final class CheckAndUpdateExistingFilters extends AllChildrenNodesOperation {
    private static final String FILTER_INTERCEPT = "intercept";
    private static final String FILTER_SECURITY = "security";
    private static final String FILTER_CMS = "cms";
    private static final String FILTER_CONTEXT = "context";
    private static final String FILTER_MULTIPART_REQUEST = "multipartRequest";
    private static final String FILTER_VIRTUAL_URI = "virtualURI";
    private static final String FILTER_CONTENT_TYPE = "contentType";
    
    private final LinkedHashMap filterChain30 = new LinkedHashMap();
    private final String existingFiltersPath;
    private final String[] migratedFilters = new String[] { FILTER_CONTENT_TYPE, FILTER_VIRTUAL_URI, FILTER_MULTIPART_REQUEST, FILTER_CONTEXT, FILTER_CMS };
    
    private final ArrayDelegateTask subtasks;
    
    public CheckAndUpdateExistingFilters(String existingFiltersPath) {
        super("Filters", "Installs or updates the new filter configuration.", ContentRepository.CONFIG, existingFiltersPath);
        this.subtasks = new ArrayDelegateTask("Filter updates");
        this.existingFiltersPath = existingFiltersPath;

        // filter chain that is bootstrapped with latest Magnolia 3.0.x
        filterChain30.put(FILTER_CONTENT_TYPE,
            new Filter30("info.magnolia.cms.filters.ContentTypeFilter",
                new Long(100)));
        filterChain30.put(FILTER_SECURITY,
            new Filter30("info.magnolia.cms.security.SecurityFilter",
                new Long(200)));
        filterChain30.put(FILTER_VIRTUAL_URI,
            new Filter30("info.magnolia.cms.filters.MgnlVirtualUriFilter",
                new Long(300)));
        filterChain30.put(FILTER_MULTIPART_REQUEST,
            new Filter30("info.magnolia.cms.filters.MultipartRequestFilter",
                new Long(400)));
        filterChain30.put(FILTER_CONTEXT,
            new Filter30("info.magnolia.cms.filters.MgnlContextFilter",
                new Long(500)));
        final HashMap interceptFilterParams = new HashMap();
        interceptFilterParams.put("test", "true");
        filterChain30.put(FILTER_INTERCEPT,
            new Filter30("info.magnolia.cms.filters.MgnlInterceptFilter",
                new Long(600),
                null,
                interceptFilterParams));
        filterChain30.put(FILTER_CMS,
            new Filter30("info.magnolia.cms.filters.MgnlCmsFilter",
                new Long(800),
                "/.,/docroot/,/admindocroot/,/tmp/fckeditor/,/ActivationHandler"));
    }
    
    /**
     * Executes the AllChildrenNodesOperation and possibly added subtasks to update the configuration.
     */
    public void execute(InstallContext installContext) throws TaskExecutionException {
        super.execute(installContext);
        subtasks.execute(installContext);
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
                if (ArrayUtils.contains(migratedFilters, currentFilter)) {
                    ctx.info("Existing configuration of filter '" + currentFilter + "' has different bypass definitions. Magnolia put a backup in " + existingFiltersPath + "/" + currentFilter + ". Will update the bypasses to the new configuration automatically.");
                    migrateBypasses(existingFilter, currentFilter);
                } else {
                    ctx.warn("Existing configuration of filter '" + currentFilter + "' has different bypass definitions. Magnolia put a backup in " + existingFiltersPath + "/" + currentFilter + ". Please review the changes manually.");
                }
            }
        } catch (Content2BeanException e) {
            ctx.error("Cannot convert filter node to map", e);
        }
    }

    private void migrateBypasses(final Map existingFilter, final String newFilterName) {
        final String filterPath = "/server/filters/" + newFilterName ;
        final String existingBypassesList = getBypasses(existingFilter);
        final String[] existingBypasses = StringUtils.split(existingBypassesList, ",");
        for (int i = 0; i < existingBypasses.length; i++) {
            final String bypassPattern = StringUtils.trim(existingBypasses[i]);
            String bypassName = StringUtils.replaceChars(bypassPattern, "/* ", "");
            if (bypassName.equals(".")) {
                bypassName = "dot";
            }
            if (StringUtils.isEmpty(bypassName)) {
                bypassName = "default";
            }
            final Class bypassClass = URIStartsWithVoter.class;
            
            subtasks.addTask(new AddFilterBypassTask(filterPath, bypassName , bypassClass , bypassPattern));
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