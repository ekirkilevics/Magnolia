package info.magnolia.cms.filters;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;
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


/**
 * A single filter that in turn executed a chain of other filters not configured in web.xml.
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class MagnoliaMainFilter extends AbstractMagnoliaFilter {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ContentTypeFilter.class);

    private MagnoliaFilter[] filters = new MagnoliaFilter[0];

    private FilterConfig filterConfig;

    /**
     * The first filter on which init(FilterConfig) is called is the root filter managed by the container
     */
    static MagnoliaMainFilter rootFilter = null;

    private static final String SERVER_FILTERS = "/server/filters";

    /**
     * We do not have an additional filters node for the main filter
     */
    private static final Content2BeanTransformer FILTER_TRANSFORMER = new Content2BeanTransformerImpl() {

        public void initBean(TransformationState state, Map values) throws Content2BeanException {
            super.initBean(state, values);

            Object bean = state.getCurrentBean();
            // we do not have a filters subnode again
            if (bean instanceof MagnoliaMainFilter) {
                for (Iterator iter = values.values().iterator(); iter.hasNext();) {
                    Object value = iter.next();
                    if (value instanceof MagnoliaFilter) {
                        ((MagnoliaMainFilter) bean).addFilter((MagnoliaFilter) value);
                    }
                }
            }
        }

        /**
         * The default class to use is MagnoliaMainFilter
         */
        protected TypeDescriptor onResolveClass(TransformationState state) {
            if(state.getCurrentContent().isNodeType(ItemType.CONTENT.getSystemName())){
                return this.getTypeMapping().getTypeDescriptor(MagnoliaMainFilter.class);
            }
            return super.onResolveClass(state);
        }
    };

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

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
        if (rootFilter == null) {
            rootFilter = this;
            setName("root");
            ObservationUtil.registerChangeListener(ContentRepository.CONFIG, SERVER_FILTERS, new EventListener() {

                public void onEvent(EventIterator arg0) {
                    MgnlContext.setInstance(MgnlContext.getSystemContext());
                    destroy();
                    createFilters();
                    initFilters();
                }
            });
            createFilters();
        }
        initFilters();
    }

    protected void createFilters() {
        Content node;

        try {
            node = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG).getContent(SERVER_FILTERS);

            Content2BeanUtil.getContent2BeanProcessor().setProperties(this, node, true, FILTER_TRANSFORMER);
        }
        catch (RepositoryException e) {
            log.error("can't read filter definitions", e);
            return;
        }
        catch (Content2BeanException e) {
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
                filter.init(filterConfig);
            }
            catch (ServletException e) {
                log.error("Error initializing filter [" + filter.getName() + "]", e);
            }

            log.info("Initializing filter [{}]", filter.getName());
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
