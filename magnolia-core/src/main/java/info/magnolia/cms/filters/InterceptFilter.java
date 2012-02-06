/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.repository.RepositoryConstants;

import java.io.IOException;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handle intercepted administrative requests.
 * @version $Id$
 * TODO: review removal of synchronized block for all actions. See also http://jira.magnolia-cms.com/browse/MAGNOLIA-3905.
 */
public class InterceptFilter extends AbstractMgnlFilter {
    private static final Logger log = LoggerFactory.getLogger(InterceptFilter.class);

    /**
     * Request parameter: the INTERCEPT holds the name of an administrative action to perform.
     */
    public static final String INTERCEPT = "mgnlIntercept";

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
     * Attribute used for enabling the preview mode.
     * @deprecated added in 4.0 for backward compatibility but should not be public.
     */
    public static final String MGNL_PREVIEW_ATTRIBUTE = "mgnlPreview";

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException{

        if (request.getParameter(INTERCEPT) != null) {
            try {
                this.intercept(request, response);
            } catch (LoginException e) {
               throw new ServletException(e);
            } catch (RepositoryException e) {
                throw new ServletException(e);
            }
        }

        final AggregationState aggregationState = MgnlContext.getAggregationState();
        aggregationState.setPreviewMode(previewMode());

        chain.doFilter(request, response);
    }

    protected boolean previewMode() {
        // first check if its passed as a request parameter
        if (MgnlContext.getParameter(MGNL_PREVIEW_ATTRIBUTE) != null) {
            return Boolean.parseBoolean(MgnlContext.getParameter(MGNL_PREVIEW_ATTRIBUTE));
        }

        // then in attributes, i.e the session
        final Boolean value = (Boolean) MgnlContext.getAttribute(MGNL_PREVIEW_ATTRIBUTE, Context.SESSION_SCOPE);
        return BooleanUtils.toBoolean(value);
    }


    /**
     * Request and Response here is same as received by the original page so it includes all post/get data. Sub action
     * could be called from here once this action finishes, it will continue loading the requested page.
     * @throws RepositoryException
     * @throws LoginException
     */
    public void intercept(HttpServletRequest request, HttpServletResponse response) throws LoginException, RepositoryException {
        final AggregationState aggregationState = MgnlContext.getAggregationState();
        String action = request.getParameter(INTERCEPT);
        String repository = request.getParameter(PARAM_REPOSITORY);
        String nodePath = request.getParameter(PARAM_PATH);
        String handle = aggregationState.getHandle();

        if (repository == null) {
            repository = aggregationState.getRepository();
        }

        if (repository == null) {
            repository = RepositoryConstants.WEBSITE;
        }

        final Session session = MgnlContext.getJCRSession(repository);

        if (ACTION_PREVIEW.equals(action)) {
            // preview mode (button in main bar)
            String preview = request.getParameter(MGNL_PREVIEW_ATTRIBUTE);
            log.debug("preview request parameter value is {} ", preview);
            if (preview != null) {
                if (Boolean.parseBoolean(preview)) {
                    MgnlContext.setAttribute(MGNL_PREVIEW_ATTRIBUTE, Boolean.TRUE, Context.SESSION_SCOPE);
                } else {
                    MgnlContext.removeAttribute(MGNL_PREVIEW_ATTRIBUTE, Context.SESSION_SCOPE);
                }
            } else {
                MgnlContext.removeAttribute(MGNL_PREVIEW_ATTRIBUTE, Context.SESSION_SCOPE);
            }
        } else if (ACTION_NODE_DELETE.equals(action)) {
            // delete paragraph
            try {
                Node page = session.getNode(handle);
                session.removeItem(nodePath);
                MetaDataUtil.updateMetaData(page);
                session.save();
            } catch (RepositoryException e) {
                log.error("Exception caught: {}", e.getMessage(), e);
            }
        } else if (ACTION_NODE_SORT.equals(action)) {
            // sort paragraphs
            try {
                String pathSelected = request.getParameter(PARAM_PATH_SELECTED);
                String pathSortAbove = request.getParameter(PARAM_PATH_SORT_ABOVE);
                String pathParent = StringUtils.substringBeforeLast(pathSelected, "/");
                String srcName = StringUtils.substringAfterLast(pathSelected, "/");
                String destName = StringUtils.substringAfterLast(pathSortAbove, "/");
                if (StringUtils.equalsIgnoreCase(destName, "mgnlNew")) {
                    destName = null;
                }
                Node parent = session.getNode(pathParent+srcName);
                NodeUtil.orderBefore(parent, destName);
                Node page = session.getNode(handle);
                MetaDataUtil.updateMetaData(page);
                session.save();
            } catch (RepositoryException e) {
                log.error("Exception caught: {}", e.getMessage(), e);
            }
        } else {
            log.warn("Unknown action {}", action);
        }
    }
}
