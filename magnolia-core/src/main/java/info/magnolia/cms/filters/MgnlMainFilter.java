/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.filters;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleManager;

import java.io.IOException;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single filter which in turn executes a chain of other filters not configured in web.xml. This filters delegates to
 * one single filter which is either the filter chain configured in the config repository or the primitive system UI when
 * a system/module installation or update is needed.
 *
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class MgnlMainFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(MgnlMainFilter.class);

    private static MgnlMainFilter instance;

    private MgnlFilter rootFilter;

    private FilterConfig filterConfig;

    public static final String SERVER_FILTERS = "/server/filters";

    private final EventListener filtersEventListener = new EventListener() {
        public void onEvent(EventIterator arg0) {
            MgnlContext.doInSystemContext(new MgnlContext.SystemContextOperation() {
                public void exec() {
                    reset();
                }
            }, true);
        }
    };

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("Handling URI: {} - Path info: {}", request.getRequestURI(), request.getPathInfo());

        if (!rootFilter.bypasses(request)) {
            rootFilter.doFilter(request, response, chain);
        } else {
            // pass request to next filter in web.xml
            chain.doFilter(request, response);
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        instance = this;
        // remember this config
        this.filterConfig = filterConfig;
        MgnlContext.doInSystemContext(new MgnlContext.SystemContextOperation(){
            public void exec() {
                if (!isSystemUIMode()) {
                    startObservation();
                }
                createRootFilter();
                initRootFilter();
            }
        }, true);
    }

    protected void startObservation() {
        ObservationUtil.registerDeferredChangeListener(
            ContentRepository.CONFIG,
            SERVER_FILTERS,
            filtersEventListener,
            1000,
            5000);
    }

    protected void createRootFilter() {
        if (isSystemUIMode()) {
            rootFilter = createSystemUIFilter();
        }
        else {
            try {
                final HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG);
                final Content node = hm.getContent(SERVER_FILTERS);
                rootFilter = (MgnlFilter) Content2BeanUtil.toBean(node, true, MgnlFilter.class);
            }
            catch (PathNotFoundException e) {
                log.warn("No filters configured at {}", SERVER_FILTERS); //$NON-NLS-1$
            }
            catch (RepositoryException e) {
                log.error("Can't read filter definitions", e);
            }
            catch (Content2BeanException e) {
                log.error("Can't create filter objects", e);
            }
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
        classpathSpoolFilter.setServletClass(info.magnolia.cms.servlets.ClasspathSpool.class.getName());
        classpathSpoolFilter.addMapping("/.resources/*");
        classpathSpoolFilter.setEnabled(true);
        systemUIFilter.addFilter(classpathSpoolFilter);

        final InstallFilter installFilter = new InstallFilter(ModuleManager.Factory.getInstance(), this);
        installFilter.setName("install");
        systemUIFilter.addFilter(installFilter);
        return systemUIFilter;
    }

    /**
     * Checks if Magnolia is ready to operate or if we need to go through
     * SystemUI initialization screens.
     */
    protected boolean isSystemUIMode() {
        ModuleManager moduleManager = ModuleManager.Factory.getInstance();
        return moduleManager.getStatus().needsUpdateOrInstall();
    }

    /**
     * The first time called by the main filter.
     */
    public void initRootFilter() {
        try {
            log.info("Initializing filters");
            rootFilter.init(filterConfig);
        }
        catch (ServletException e) {
            log.error("Error initializing filters", e);
        }
    }

    public void destroy() {
        if(rootFilter != null){
            rootFilter.destroy();
            rootFilter = null;
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    public void reset() {
        destroy();
        createRootFilter();
        initRootFilter();
    }

    public static MgnlMainFilter getInstance() {
        return instance;
    }

    public MgnlFilter getRootFilter() {
        return this.rootFilter;
    }

}
