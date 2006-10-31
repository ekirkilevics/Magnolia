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
package info.magnolia.cms.filters;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.config.URI2RepositoryMapping;
import info.magnolia.cms.core.Aggregator;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.cms.util.Resource;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handle intercepted administrative requests.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class MgnlInterceptFilter implements Filter {

    /**
     * Request parameter: the INTERCEPT holds the name of an administrative action to perform.
     */
    public static final String INTERCEPT = "mgnlIntercept"; //$NON-NLS-1$

    /**
     * Action: sort a paragraph.
     */
    private static final String ACTION_NODE_SORT = "NODE_SORT"; //$NON-NLS-1$

    /**
     * Action: delete a paragraph.
     */
    private static final String ACTION_NODE_DELETE = "NODE_DELETE"; //$NON-NLS-1$

    /**
     * Action: preview a page.
     */
    private static final String ACTION_PREVIEW = "PREVIEW"; //$NON-NLS-1$

    /**
     * request parameter: repository name.
     */
    private static final String PARAM_REPOSITORY = "mgnlRepository"; //$NON-NLS-1$

    /**
     * request parameter: node path, used for paragraph deletion.
     */
    private static final String PARAM_PATH = "mgnlPath"; //$NON-NLS-1$

    /**
     * request parameter: sort-above paragraph.
     */
    private static final String PARAM_PATH_SORT_ABOVE = "mgnlPathSortAbove"; //$NON-NLS-1$

    /**
     * request parameter: selected paragraph.
     */
    private static final String PARAM_PATH_SELECTED = "mgnlPathSelected"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MgnlInterceptFilter.class);

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        // unused
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // unused
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException,
        ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        // @todo the isAuthorized method is duplicated from EntryServlet!
        if (isAuthorized(request, response)) {
            if (req.getParameter(INTERCEPT) != null) {
            	MgnlInterceptFilter.setHandleAndMapping(request);
                this.intercept(request, response);
            }
        }

        filterChain.doFilter(req, resp);
    }

    private static void setHandleAndMapping(HttpServletRequest request) {
        String uri = Path.getURI(request);
        int firstDotPos = StringUtils.indexOf(uri, '.', StringUtils.lastIndexOf(uri, '/'));
        String handle;
        String selector;
        String extension;
        if (firstDotPos > -1) {
            int lastDotPos = StringUtils.lastIndexOf(uri, '.');
            handle = StringUtils.substring(uri, 0, firstDotPos);
            selector = StringUtils.substring(uri, firstDotPos + 1, lastDotPos);
            extension = StringUtils.substring(uri, lastDotPos + 1);
        }
        else {
            // no dots (and no extension)
            handle = uri;
            selector = "";
            extension = "";
        }

        URI2RepositoryMapping mapping = URI2RepositoryManager.getInstance().getMapping(uri);

        // remove prefix if any
        handle = mapping.getHandle(handle);

        request.setAttribute(Aggregator.REPOSITORY, mapping.getRepository());
        request.setAttribute(Aggregator.MAPPING, mapping);
        request.setAttribute(Aggregator.HANDLE, handle);
        request.setAttribute(Aggregator.SELECTOR, selector);
        request.setAttribute(Aggregator.EXTENSION, extension);
    }

    /**
     * Request and Response here is same as receivced by the original page so it includes all post/get data. Sub action
     * could be called from here once this action finishes, it will continue loading the requested page.
     */
    public void intercept(HttpServletRequest request, HttpServletResponse response) {
        String action = request.getParameter(INTERCEPT);
        String repository = request.getParameter(PARAM_REPOSITORY);
        String nodePath = request.getParameter(PARAM_PATH);
        String handle = (String) request.getAttribute(Aggregator.HANDLE);
        
        if (repository == null) {
        	repository = (String) request.getAttribute(Aggregator.REPOSITORY);
        }

        if (repository == null) {
            repository = ContentRepository.WEBSITE;
        }

        HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
        synchronized (ExclusiveWrite.getInstance()) {
            if (action.equals(ACTION_PREVIEW)) {
                // preview mode (button in main bar)
                String preview = request.getParameter(Resource.MGNL_PREVIEW_ATTRIBUTE);
                if (preview != null) {
                	
                    // @todo IMPORTANT remove use of http session
                    HttpSession httpsession = request.getSession(true);
                    if (BooleanUtils.toBoolean(preview)) {
                        httpsession.setAttribute(Resource.MGNL_PREVIEW_ATTRIBUTE, Boolean.TRUE);
                    }
                    else {
                        httpsession.removeAttribute(Resource.MGNL_PREVIEW_ATTRIBUTE);
                    }
                }
            }
            else if (action.equals(ACTION_NODE_DELETE)) {
                // delete paragraph
                try {
					Content page = hm.getContent(handle);
					page.updateMetaData();
                    hm.delete(nodePath);
                    hm.save();
                }
                catch (RepositoryException e) {
                    log.error("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
                }
            }
            else if (action.equals(ACTION_NODE_SORT)) {
                // sort paragrpahs
                try {
                    String pathSelected = request.getParameter(PARAM_PATH_SELECTED);
                    String pathSortAbove = request.getParameter(PARAM_PATH_SORT_ABOVE);
                    String pathParent = StringUtils.substringBeforeLast(pathSelected, "/"); //$NON-NLS-1$
                    String srcName = StringUtils.substringAfterLast(pathSelected, "/");
                    String destName = StringUtils.substringAfterLast(pathSortAbove, "/");
                    if (StringUtils.equalsIgnoreCase(destName, "mgnlNew")) {
                        destName = null;
                    }
                    hm.getContent(pathParent).orderBefore(srcName, destName);
                    hm.save();
                }
                catch (RepositoryException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
                    }
                }
            }
        }
    }

    /**
     * Uses access manager to authorise this request.
     * @param req HttpServletRequest as received by the service method
     * @param res HttpServletResponse as received by the service method
     * @return boolean true if read access is granted
     * @throws IOException can be thrown when the servlet is unable to write to the response stream
     */
    protected boolean isAuthorized(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (MgnlContext.getAccessManager(ContentRepository.WEBSITE) != null) {
            String path = StringUtils.substringBefore(Path.getURI(req), "."); //$NON-NLS-1$
            if (!MgnlContext.getAccessManager(ContentRepository.WEBSITE).isGranted(path, Permission.READ)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        }
        return true;
    }

}