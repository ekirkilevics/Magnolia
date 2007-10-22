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
 * one single filter whih is either the filter chain configured in the config repository or the primitive system UI due
 * to a installation or update process
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
            MgnlContext.setInstance(MgnlContext.getSystemContext());
            reset();
        }
    };

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        if (log.isDebugEnabled()) {
            String pathInfo = request.getPathInfo();
            String requestURI = request.getRequestURI();

            if (pathInfo == null || !requestURI.startsWith("/.")) {
                log.debug("handling: {}   path info: {}", requestURI, pathInfo);
            }
        }

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
        if (!isSystemUIMode()) {
            startObservation();
        }
        createRootFilter();
        initRootFilter();
    }

    protected void startObservation() {
        ObservationUtil.registerDefferedChangeListener(
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
                final HierarchyManager hm = MgnlContext
                    .getSystemContext()
                    .getHierarchyManager(ContentRepository.CONFIG);
                final Content node = hm.getContent(SERVER_FILTERS);
                rootFilter = (MgnlFilter) Content2BeanUtil.toBean(node, true, new FilterContent2BeanTransformer());
            }
            catch (PathNotFoundException e) {
                log.warn("Config : no filters configured at " + SERVER_FILTERS); //$NON-NLS-1$
            }
            catch (RepositoryException e) {
                log.error("can't read filter definitions", e);
            }
            catch (Content2BeanException e) {
                log.error("can't create filter objects", e);
            }
        }
    }

    private InstallFilter createSystemUIFilter() {
        return new InstallFilter();
    }

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
        rootFilter.destroy();
        rootFilter = null;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {
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
