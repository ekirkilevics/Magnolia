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
package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.cms.util.Resource;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @version 2.0
 * @version $Revision$ ($Author$)
 */
public class RequestInterceptor extends HttpServlet {

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
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(RequestInterceptor.class);

    /**
     * Request and Response here is same as receivced by the original page so it includes all post/get data. Sub action
     * could be called from here once this action finishes, it will continue loading the requested page.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        String action = request.getParameter(EntryServlet.INTERCEPT);
        String repository = request.getParameter(PARAM_REPOSITORY);
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
                    String path = request.getParameter(PARAM_PATH);
                    // deactivate
                    updatePageMetaData(request, hm);
                    hm.delete(path);
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
                    log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * @param request
     * @param hm
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    private void updatePageMetaData(HttpServletRequest request, HierarchyManager hm) throws PathNotFoundException,
        RepositoryException, AccessDeniedException {
        String pagePath = StringUtils.substringBeforeLast(Path.getURI(request), "."); //$NON-NLS-1$
        Content page = hm.getContent(pagePath);
        page.updateMetaData();
    }
}
