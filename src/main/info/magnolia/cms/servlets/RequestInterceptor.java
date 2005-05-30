/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.util.Resource;

import java.util.Iterator;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @version 2.0
 */
public class RequestInterceptor extends HttpServlet {

    /**
     * Action: sort a paragraph.
     */
    private static final String ACTION_NODE_SORT = "NODE_SORT";

    /**
     * Action: delete a paragraph.
     */
    private static final String ACTION_NODE_DELETE = "NODE_DELETE";

    /**
     * Action: preview a page.
     */
    private static final String ACTION_PREVIEW = "PREVIEW";

    /**
     * request parameter: repository name.
     */
    private static final String PARAM_REPOSITORY = "mgnlRepository";

    /**
     * request parameter: node path, used for paragraph deletion.
     */
    private static final String PARAM_PATH = "mgnlPath";

    /**
     * request parameter: sort-above paragraph.
     */
    private static final String PARAM_PATH_SORT_ABOVE = "mgnlPathSortAbove";

    /**
     * request parameter: selected paragraph.
     */
    private static final String PARAM_PATH_SELECTED = "mgnlPathSelected";

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(RequestInterceptor.class);

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
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(request, repository);
        if (action.equals(ACTION_PREVIEW)) {
            // preview mode (button in main bar)
            String preview = request.getParameter(Resource.MGNL_PREVIEW_ATTRIBUTE);
            if (preview != null) {
                if (BooleanUtils.toBoolean(preview)) {
                    request.getSession().setAttribute(Resource.MGNL_PREVIEW_ATTRIBUTE, Boolean.TRUE);
                }
                else {
                    request.getSession().removeAttribute(Resource.MGNL_PREVIEW_ATTRIBUTE);
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
                log.error("Exception caught: " + e.getMessage(), e);
            }
        }
        else if (action.equals(ACTION_NODE_SORT)) {
            // sort paragrpahs
            try {
                String pathSelected = request.getParameter(PARAM_PATH_SELECTED);
                String pathSortAbove = request.getParameter(PARAM_PATH_SORT_ABOVE);
                String pathParent = pathSelected.substring(0, pathSelected.lastIndexOf("/"));
                Iterator it = hm.getContent(pathParent).getChildren(ItemType.CONTENTNODE.getSystemName(), Content.SORT_BY_SEQUENCE).iterator();
                long seqPos0 = 0;
                long seqPos1 = 0;
                while (it.hasNext()) {
                    Content c = (Content) it.next();
                    if (c.getHandle().equals(pathSortAbove)) {
                        seqPos1 = c.getMetaData().getSequencePosition();
                        break;
                    }
                    seqPos0 = c.getMetaData().getSequencePosition();
                }
                Content nodeSelected = hm.getContent(pathSelected);
                if (seqPos0 == 0) {
                    // move to first position -> 1000*coefficient above seqPos1 (old first)
                    nodeSelected
                        .getMetaData()
                        .setSequencePosition(seqPos1 - (MetaData.SEQUENCE_POS_COEFFICIENT * 1000));
                }
                else if (seqPos1 == 0) {
                    // move to last position (pathSortAbove not found)
                    nodeSelected.getMetaData().setSequencePosition();
                }
                else {
                    // move between two paragraphs
                    nodeSelected.getMetaData().setSequencePosition((seqPos0 + seqPos1) / 2);
                }
                
                this.updatePageMetaData(request, hm);
                hm.save();
            }
            catch (RepositoryException e) {
                log.debug("Exception caught: " + e.getMessage(), e);
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
    private void updatePageMetaData(HttpServletRequest request, HierarchyManager hm) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        String pagePath = StringUtils.substringBeforeLast(Path.getURI(request),".");
        Content page = hm.getContent(pagePath);
        page.updateMetaData(request);
    }
}
