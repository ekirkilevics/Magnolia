/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.templating;

import java.io.IOException;
import javax.jcr.RepositoryException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.filters.OncePerRequestAbstractMgnlFilter;
import info.magnolia.cms.util.RequestDispatchUtil;
import info.magnolia.context.MgnlContext;

/**
 * Filter that executes the model for a paragraph before template rendering. Looks for a request parameter containing
 * the UUID of the paragraph to execute. The model can decide to send output by itself in which case page rendering
 * is skipped. The model can also return a URI prefixed by "redirect:", "permanent:" or "forward:" to trigger either
 * a temporary redirect, a permanent redirect or a forward respectively. For redirects the URI can be absolute or
 * relative within the web application (the context path is added automatically).
 *
 * @author tmattsson
 * @see info.magnolia.module.templating.AbstractRenderer
 * @see info.magnolia.cms.util.RequestDispatchUtil
 */
public class ModelExecutionFilter extends OncePerRequestAbstractMgnlFilter {

    public static final String MODEL_ATTRIBUTE_PREFIX = ModelExecutionFilter.class.getName() + "-model-";
    public static final String ACTION_RESULT_ATTRIBUTE_PREFIX = ModelExecutionFilter.class.getName() + "-actionResult-";
    public static final String DEFAULT_MODEL_EXECUTION_ATTRIBUTE_NAME = "mgnlModelExecutionUUID";

    private String attributeName = DEFAULT_MODEL_EXECUTION_ATTRIBUTE_NAME;

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String paragraphUuid = getUuidOfParagraphToExecute();

        if (paragraphUuid == null) {
            chain.doFilter(request, response);
            return;
        }

        Content content = getContent(paragraphUuid);

        Paragraph paragraph = getParagraph(content);

        RenderingModel renderingModel = createRenderingModel(content, paragraph);

        String actionResult = renderingModel.execute();

        // If the model rendered something on its own or sent a redirect we will not proceed with rendering.
        if (response.isCommitted())
            return;

        if (handleActionResult(actionResult, request, response))
            return;

        // Proceed with page rendering, the model will be reused later when the paragraph is rendered.
        MgnlContext.setAttribute(MODEL_ATTRIBUTE_PREFIX + paragraphUuid, renderingModel);
        MgnlContext.setAttribute(ACTION_RESULT_ATTRIBUTE_PREFIX + paragraphUuid, actionResult);
        try {
            chain.doFilter(request, response);
        } finally {
            MgnlContext.removeAttribute(MODEL_ATTRIBUTE_PREFIX + paragraphUuid);
            MgnlContext.removeAttribute(ACTION_RESULT_ATTRIBUTE_PREFIX + paragraphUuid);
        }
    }

    protected String getUuidOfParagraphToExecute() {
        return (String) MgnlContext.getInstance().getAttribute(attributeName);
    }

    /**
     * Returns the Content node for the supplied uuid. Never returns null.
     */
    protected Content getContent(String uuid) throws ServletException {

        String repository = MgnlContext.getAggregationState().getRepository();
        HierarchyManager hm = MgnlContext.getHierarchyManager(repository);

        try {
            return hm.getContentByUUID(uuid);
        } catch (RepositoryException e) {
            throw new ServletException("Can't read content for paragraph: " + uuid, e);
        }
    }

    /**
     * Returns the Paragraph for the supplied Content. Never returns null.
     */
    protected Paragraph getParagraph(Content content) throws ServletException {

        String templateName = content.getMetaData().getTemplate();

        if (StringUtils.isEmpty(templateName)) {
            throw new ServletException("No paragraph name set for paragraph with UUID: " + content.getUUID());
        }

        Paragraph paragraph = ParagraphManager.getInstance().getParagraphDefinition(templateName);

        if (paragraph == null) {
            throw new ServletException("Paragraph does not exist: " + templateName);
        }

        return paragraph;
    }

    protected RenderingModel createRenderingModel(Content content, Paragraph paragraph) throws ServletException {
        try {
            return RenderingModelFactory.getInstance().newModel(content, paragraph, null);
        }
        catch (Exception e) {
            throw new ServletException("Can't create rendering model: " + ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

    /**
     * Returns true if special handling was performed.
     */
    protected boolean handleActionResult(String actionResult, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        if (actionResult == null)
            return false;

        if (actionResult.equals(RenderingModel.SKIP_RENDERING))
            return true;

        if (RequestDispatchUtil.dispatch(actionResult, request, response))
            return true;

        return false;
    }
}
