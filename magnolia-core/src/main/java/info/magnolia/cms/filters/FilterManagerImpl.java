/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.filters;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.servlets.ClasspathSpool;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.module.ModuleManager;
import info.magnolia.repository.RepositoryConstants;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;


/**
 * Default {@link FilterManager} implementation; uses node2bean and observation
 * to maintain the filter chain configured at {@value #SERVER_FILTERS}.
 */
@Singleton
public class FilterManagerImpl implements FilterManager {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FilterManagerImpl.class);

    private final EventListener filtersEventListener = new EventListener() {
        @Override
        public void onEvent(EventIterator arg0) {
            MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
                @Override
                public void doExec() {
                    resetRootFilter();
                }
            }, true);
        }
    };

    private final ModuleManager moduleManager;
    private final SystemContext systemContext;
    private final MgnlFilterDispatcher filterDispatcher = new MgnlFilterDispatcher();
    private final Object resetLock = new Object();
    private FilterConfig filterConfig;
    private final Node2BeanProcessor nodeToBean;

    @Inject
    public FilterManagerImpl(ModuleManager moduleManager, SystemContext systemContext, Node2BeanProcessor nodeToBean) {
        this.moduleManager = moduleManager;
        this.systemContext = systemContext;
        this.nodeToBean = nodeToBean;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // remember this config
        this.filterConfig = filterConfig;
        MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
            @Override
            public void doExec() {
                try {
                    MgnlFilter filter = createRootFilter();
                    initRootFilter(filter, FilterManagerImpl.this.filterConfig);
                    filterDispatcher.replaceTargetFilter(filter);
                } catch (ServletException e) {
                    log.error("Error initializing filters", e);
                    return;
                }
                if (!isSystemUIMode()) {
                    startObservation();
                }
            }
        }, true);
    }

    @Override
    public void destroy() {
        MgnlFilter filter = filterDispatcher.replaceTargetFilter(null);
        destroyRootFilter(filter);
    }

    @Override
    public MgnlFilterDispatcher getFilterDispatcher() {
        return filterDispatcher;
    }

    @Override
    public void startUsingConfiguredFilters() {
        resetRootFilter();
        startObservation();
    }

    protected void resetRootFilter() {
        synchronized (resetLock) {

            MgnlFilter newFilter;
            try {
                newFilter = createRootFilter();
                initRootFilter(newFilter, filterConfig);
            } catch (ServletException e) {
                log.error("Error initializing filters", e);
                return;
            }

            final MgnlFilter oldFilter = filterDispatcher.replaceTargetFilter(newFilter);

            // This is executed asynchronously in another thread so we don't risk causing dead lock, see SafeDestroyMgnlFilterWrapper
            doInSystemContextAsync("FilterChainDisposerThread", new MgnlContext.VoidOp() {
                @Override
                public void doExec() {
                    destroyRootFilter(oldFilter);
                }
            }, true);
        }
    }

    protected MgnlFilter createRootFilter() throws ServletException {
        if (isSystemUIMode()) {
            return createSystemUIFilter();
        }
        return createConfiguredFilters();
    }

    protected void initRootFilter(MgnlFilter rootFilter, FilterConfig filterConfig) throws ServletException {
        log.info("Initializing filters");
        rootFilter.init(filterConfig);

        if (log.isDebugEnabled()) {
            printFilters(rootFilter);
        }
    }

    protected void destroyRootFilter(MgnlFilter rootFilter) {
        if (rootFilter != null) {
            rootFilter.destroy();
        }
    }

    private MgnlFilter createConfiguredFilters() throws ServletException {
        try {
            final HierarchyManager hm = systemContext.getHierarchyManager(RepositoryConstants.CONFIG);
            final Content node = hm.getContent(SERVER_FILTERS);
            MgnlFilter filter = (MgnlFilter) nodeToBean.toBean(node.getJCRNode(), MgnlFilter.class);
            if (filter == null) {
                throw new ServletException("Unable to create filter objects");
            }
            return filter;
        } catch (PathNotFoundException e) {
            throw new ServletException("No filters configured at " + SERVER_FILTERS);
        } catch (RepositoryException e) {
            throw new ServletException("Can't read filter definitions", e);
        } catch (Node2BeanException e) {
            throw new ServletException("Can't create filter objects", e);
        }
    }

    /**
     * Initializes the required filter(s) if we need to go through
     * SystemUI initialization screens.
     */
    protected MgnlFilter createSystemUIFilter() {
        final CompositeFilter systemUIFilter = new CompositeFilter();
        final ServletDispatchingFilter classpathSpoolFilter = new ServletDispatchingFilter();
        classpathSpoolFilter.setName("resources");
        classpathSpoolFilter.setServletName("ClasspathSpool Servlet");
        classpathSpoolFilter.setServletClass(ClasspathSpool.class.getName());
        classpathSpoolFilter.addMapping("/.resources/*");
        classpathSpoolFilter.addMapping("/favicon.ico");
        classpathSpoolFilter.setParameters(Collections.emptyMap());
        classpathSpoolFilter.setEnabled(true);
        systemUIFilter.addFilter(classpathSpoolFilter);

        final InstallFilter installFilter = new InstallFilter(moduleManager, this);
        installFilter.setName("install");
        systemUIFilter.addFilter(installFilter);
        return systemUIFilter;
    }

    /**
     * Checks if Magnolia is ready to operate or if we need to go through
     * SystemUI initialization screens.
     */
    protected boolean isSystemUIMode() {
        return moduleManager.getStatus().needsUpdateOrInstall();
    }

    protected void startObservation() {
        ObservationUtil.registerDeferredChangeListener(
                RepositoryConstants.CONFIG,
                SERVER_FILTERS,
                filtersEventListener,
                1000,
                5000);
    }

    private void printFilters(MgnlFilter rootFilter) {
        log.debug("Here is the root filter as configured:");
        printFilter(0, rootFilter);
    }

    private void printFilter(int indentation, MgnlFilter filter) {
        log.debug("{}{} ({})", new Object[]{StringUtils.repeat(" ", indentation), filter.getName(), filter.toString()});
        if (filter instanceof CompositeFilter) {
            for (MgnlFilter nestedFilter : ((CompositeFilter) filter).getFilters()) {
                printFilter(indentation + 2, nestedFilter);
            }
        }
    }

    private <T, E extends Throwable> void doInSystemContextAsync(final String threadName, final MgnlContext.Op<T, E> op, final boolean releaseAfterExecution) {
        new Thread() {
            {
                setName(threadName);
            }
            @Override
            public void run() {
                try {
                    MgnlContext.doInSystemContext(op, releaseAfterExecution);
                } catch (Throwable e) {
                    log.error("Exception caught when executing asynchronous operation: " + e.getMessage(), e);
                }
            }
        }.start();
    }
}
