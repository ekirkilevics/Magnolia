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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.servlets.ClasspathSpool;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.module.ModuleManager;
import org.apache.commons.lang.StringUtils;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.util.Collections;

/**
 * Default {@link FilterManager} implementation; uses content2bean and observation
 * to maintain the filter chain configured at {@value #SERVER_FILTERS}.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FilterManagerImpl implements FilterManager {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FilterManagerImpl.class);

    private final EventListener filtersEventListener = new EventListener() {
        public void onEvent(EventIterator arg0) {
            MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
                public void doExec() {
                    resetRootFilter();
                }
            }, true);
        }
    };

    private final ModuleManager moduleManager;
    private final SystemContext systemContext;
    private MgnlFilter rootFilter;
    private FilterConfig filterConfig;

    public FilterManagerImpl(ModuleManager moduleManager, SystemContext systemContext) {
        this.moduleManager = moduleManager;
        this.systemContext = systemContext;
    }

    public void init(final FilterConfig filterConfig) throws ServletException {
        // remember this config
        this.filterConfig = filterConfig;
        MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
            public void doExec() {
                if (!isSystemUIMode()) {
                    startObservation();
                }
                createRootFilter();
                initRootFilter();
            }
        }, true);
    }

    public MgnlFilter getRootFilter() {
        if (rootFilter == null) {
            // TODO - lock while rootFilter is being reconstructed ?
            throw new IllegalStateException("rootFilter is null, init() has probably not been called, or failed.");
        }
        return rootFilter;
    }

    protected void initRootFilter() {
        try {
            log.info("Initializing filters");
            rootFilter.init(filterConfig);

            if (log.isDebugEnabled()) {
                printFilters(rootFilter);
            }
        } catch (ServletException e) {
            log.error("Error initializing filters", e);
        }
    }

    protected void createRootFilter() {
        if (isSystemUIMode()) {
            rootFilter = createSystemUIFilter();
        } else {
            rootFilter = createConfiguredFilters();
        }
    }

    private MgnlFilter createConfiguredFilters() {
        try {
            final HierarchyManager hm = systemContext.getHierarchyManager(ContentRepository.CONFIG);
            final Content node = hm.getContent(SERVER_FILTERS);
            return (MgnlFilter) Content2BeanUtil.toBean(node, true, MgnlFilter.class);
        } catch (PathNotFoundException e) {
            log.warn("No filters configured at {}", SERVER_FILTERS);
        } catch (RepositoryException e) {
            log.error("Can't read filter definitions", e);
        } catch (Content2BeanException e) {
            log.error("Can't create filter objects", e);
        }
        return null;
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
                ContentRepository.CONFIG,
                SERVER_FILTERS,
                filtersEventListener,
                1000,
                5000);
    }

    public void resetRootFilter() {
        destroyRootFilter();
        createRootFilter();
        initRootFilter();
    }

    public void destroyRootFilter() {
        if (rootFilter != null) {
            rootFilter.destroy();
            rootFilter = null; // probably not a good idea, let it be replaced later on
        }
    }

    private void printFilters(MgnlFilter rootFilter) {
        log.debug("Here is the root filter as configured:");
        printFilter(0, rootFilter);
    }

    private void printFilter(int i, MgnlFilter f) {
        log.debug("{}{} ({})", new Object[]{StringUtils.repeat(" ", i), f.getName(), f.toString()});
        if (f instanceof CompositeFilter) {
            for (MgnlFilter f1 : ((CompositeFilter) f).getFilters()) {
                printFilter(i + 2, f1);
            }
        }
    }
}
