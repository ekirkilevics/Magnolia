package info.magnolia.cms.filters;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.module.ModuleManagerUI;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleManager;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.PathNotFoundException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import freemarker.template.TemplateException;


/**
 * A single filter which in turn executes a chain of other filters not configured in web.xml.
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class MagnoliaMainFilter extends AbstractMagnoliaFilter {
    private static final Logger log = LoggerFactory.getLogger(MagnoliaMainFilter.class);

    private MagnoliaFilter[] filters = new MagnoliaFilter[0];

    private FilterConfig filterConfig;

    /**
     * The first filter on which init(FilterConfig) is called is the root filter managed by the container
     */
    static MagnoliaMainFilter rootFilter = null;

    public static final String SERVER_FILTERS = "/server/filters";

    // reusing the same instance prevents from registering multiple listeners when the filter gets reinitialized
    // TODO : but woops, this means we instanciate this for each and every filter
    private final EventListener filtersEventListener = new EventListener() {
        public void onEvent(EventIterator arg0) {
            MgnlContext.setInstance(MgnlContext.getSystemContext());
            destroy();
            createFilters();
            initFilters();
            // TODO reset MgnlContext ??
        }
    };

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        // TODO : instead, instanciate a specific (hardcoded) chain of filters, including login, etc.
        // one could imagine that only /.magnolia responds to this, while other requests are still
        // going through the original chain, hoping the pages can be displayed
        // or at least serve a "work in progress" kind of page.
        final ModuleManager moduleManager = ModuleManager.Factory.getInstance();
        if (moduleManager.getStatus().needsUpdateOrInstall()) {
            final String contextPath = request.getContextPath();
            final ModuleManagerUI ui = new ModuleManagerUI(contextPath);
            // TODO : this will be invalid the day we allow other resources (css, images) to be served through the installer
            response.setContentType("text/html");
            final Writer out = response.getWriter();
            final String uri = request.getRequestURI();
            try {
                if (uri.startsWith(contextPath + ModuleManagerUI.INSTALLER_PATH)) {
                    final Map parameterMap = request.getParameterMap();
                    final boolean shouldContinue = ui.execute(moduleManager, out, parameterMap);
                    if (!shouldContinue) {
                        return;
                    } else {
                        // reinit filter when update done.
                        this.filters = new MagnoliaFilter[0];
                        init(filterConfig);
                        // redirect to root
                        response.sendRedirect(contextPath);
                    }
                } else {
                    ui.renderTempPage(moduleManager, out);
                    return;
                }
            } catch (TemplateException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e); // TODO
            } catch (RepositoryException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e); // TODO
            }
        }

        FilterChain fullchain = new MagnoliaFilterChain(chain, filters);

        if (log.isDebugEnabled()) {
            String pathInfo = request.getPathInfo();
            String requestURI = request.getRequestURI();

            if (pathInfo == null || !requestURI.startsWith("/.")) {
                log.debug("handling: {}   path info: {}", requestURI, pathInfo);
            }
        }

        fullchain.doFilter(request, response);
    }

    public MagnoliaFilter[] getFilters() {
        return filters;
    }

    public void addFilter(MagnoliaFilter filter) {
        this.filters = (MagnoliaFilter[]) ArrayUtils.add(this.filters, filter);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        // remember this config
        this.filterConfig = filterConfig;

        // am I the root filter?
        if (rootFilter == null || rootFilter == this) {
            rootFilter = this;
            setName("root");
            ObservationUtil.registerChangeListener(ContentRepository.CONFIG, SERVER_FILTERS, filtersEventListener);
            createFilters();
        }

        initFilters();
    }

    protected void createFilters() {
        try {
            final HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG);

            final Content node = hm.getContent(SERVER_FILTERS);
            Content2BeanUtil.getContent2BeanProcessor().setProperties(this, node, true, new FilterContent2BeanTransformer());
        } catch (PathNotFoundException e) {
            log.warn("Config : no filters configured at " + SERVER_FILTERS); //$NON-NLS-1$
        } catch (RepositoryException e) {
            log.error("can't read filter definitions", e);
        } catch (Content2BeanException e) {
            log.error("can't create filter objects", e);
        }

    }

    /**
     * The first time called by the main filter.
     */
    public void initFilters() {
        for (int j = 0; j < filters.length; j++) {
            MagnoliaFilter filter = filters[j];

            try {
                log.info("Initializing filter [{}]", filter.getName());
                filter.init(filterConfig);
            }
            catch (ServletException e) {
                log.error("Error initializing filter [" + filter.getName() + "]", e);
            }
        }
    }

    public void destroy() {
        for (int j = 0; j < filters.length; j++) {
            Filter filter = filters[j];
            filter.destroy();
        }
        filters = new MagnoliaFilter[0];
    }

}
