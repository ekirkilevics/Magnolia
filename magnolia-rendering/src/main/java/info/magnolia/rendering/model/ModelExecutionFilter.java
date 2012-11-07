/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.rendering.model;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.filters.OncePerRequestAbstractMgnlFilter;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.RequestDispatchUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.renderer.Renderer;
import info.magnolia.rendering.renderer.RenderingModelBasedRenderer;
import info.magnolia.rendering.renderer.registry.RendererRegistry;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;

import java.io.IOException;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * Filter that executes the model for a renderable before template rendering. Looks for a request parameter containing
 * the identifier of the renderable to execute. The model can decide to send output by itself in which case page rendering
 * is skipped. The model can also return a URI prefixed by "redirect:", "permanent:" or "forward:" to trigger either
 * a temporary redirect, a permanent redirect or a forward respectively. For redirects the URI can be absolute or
 * relative within the web application (the context path is added automatically).
 * <p/>
 * By implementing the {@link EarlyExecutionAware} interface the callback will instead be made to a dedicated method
 * making it easier to separate functionality for the two scenarios.
 * <p/>
 * To provide proper semantics this class mirrors functionality in RenderingEngine and AbstractRender, specifically in
 * how it sets up the current content in aggregation state and creation and execution of the model.
 *
 * @version $Id$
 * @see info.magnolia.rendering.renderer.AbstractRenderer
 * @see info.magnolia.cms.util.RequestDispatchUtil
 * @see EarlyExecutionAware
 */
public class ModelExecutionFilter extends OncePerRequestAbstractMgnlFilter {

    public static final String MODEL_ATTRIBUTE_PREFIX = ModelExecutionFilter.class.getName() + "-model-";
    public static final String ACTION_RESULT_ATTRIBUTE_PREFIX = ModelExecutionFilter.class.getName() + "-actionResult-";
    public static final String DEFAULT_MODEL_EXECUTION_ATTRIBUTE_NAME = "mgnlModelExecutionUUID";

    @Inject
    private RendererRegistry rendererRegistry;
    @Inject
    private TemplateDefinitionRegistry templateDefinitionRegistry;

    private String attributeName = DEFAULT_MODEL_EXECUTION_ATTRIBUTE_NAME;

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String nodeIdentifier = getIdentifierOfNodeToExecute();

        if (nodeIdentifier == null) {
            chain.doFilter(request, response);
            return;
        }

        Content content = ContentUtil.asContent(getContent(nodeIdentifier));

        Content orgMainContent = null;
        Content orgCurrentContent = null;

        AggregationState state = getAggregationStateSafely();
        if (state != null) {
            orgMainContent = state.getMainContent();
            orgCurrentContent = state.getCurrentContent();

            state.setCurrentContent(content);
            // if not yet set the passed content is the entry point of the rendering
            if (orgMainContent == null) {
                state.setMainContent(content);
            }
        }
        try {

            TemplateDefinition templateDefinition = getTemplateDefinition(content.getJCRNode());

            RenderingModelBasedRenderer renderingModelBasedRenderer = getRenderingModelBasedRenderer(templateDefinition);

            RenderingModel renderingModel;
            try {
                renderingModel = renderingModelBasedRenderer.newModel(content.getJCRNode(), templateDefinition, null);
            }
            catch (RenderException e) {
                throw new ServletException(e.getMessage(), e);
            }

            String actionResult;
            if (renderingModel instanceof EarlyExecutionAware) {
                actionResult = ((EarlyExecutionAware)renderingModel).executeEarly();
            } else {
                actionResult = renderingModel.execute();
            }

            if (handleExecutionResult(renderingModel, actionResult, templateDefinition, request, response)) {
                return;
            }

            // Proceed with page rendering, the model will be reused later when the renderable is rendered.
            MgnlContext.setAttribute(MODEL_ATTRIBUTE_PREFIX + nodeIdentifier, renderingModel);
            MgnlContext.setAttribute(ACTION_RESULT_ATTRIBUTE_PREFIX + nodeIdentifier, actionResult);
            try {
                chain.doFilter(request, response);
            } finally {
                MgnlContext.removeAttribute(MODEL_ATTRIBUTE_PREFIX + nodeIdentifier);
                MgnlContext.removeAttribute(ACTION_RESULT_ATTRIBUTE_PREFIX + nodeIdentifier);
            }

        } finally {
            if (state != null) {
                state.setMainContent(orgMainContent);
                state.setCurrentContent(orgCurrentContent);
            }
        }
    }

    protected static AggregationState getAggregationStateSafely() {
        if (MgnlContext.isWebContext()) {
            return MgnlContext.getAggregationState();
        }
        return null;
    }

    protected String getIdentifierOfNodeToExecute() {
        return (String) MgnlContext.getInstance().getAttribute(attributeName);
    }

    /**
     * Returns the content node for the supplied node identifier. Never returns null.
     */
    protected Node getContent(String nodeIdentifier) throws ServletException {

        String workspace = MgnlContext.getAggregationState().getRepository();

        try {
            return MgnlContext.getJCRSession(workspace).getNodeByIdentifier(nodeIdentifier);
        } catch (RepositoryException e) {
            throw new ServletException("Can't read content for early execution, node: " + nodeIdentifier, e);
        }
    }

    /**
     * Returns the TemplateDefinition for the supplied content. Never returns null.
     */
    protected TemplateDefinition getTemplateDefinition(Node content) throws ServletException {
        MetaData metaData = MetaDataUtil.getMetaData(content);

        if (metaData == null || StringUtils.isEmpty(metaData.getTemplate())) {
            throw new ServletException("No template name set for node with identifier: " + NodeUtil.getNodeIdentifierIfPossible(content));
        }

        TemplateDefinition templateDefinition;
        try {
            templateDefinition = templateDefinitionRegistry.getTemplateDefinition(metaData.getTemplate());
        } catch (RegistrationException e) {
            throw new ServletException(e);
        }

        return templateDefinition;
    }

    /**
     * Returns the Renderer for the supplied renderable if it supports RenderingModel. Never returns null.
     * @throws IllegalArgumentException if there is no renderer registered for the renderable
     * @throws ServletException if the renderer does not support RenderingModel
     */
    protected RenderingModelBasedRenderer getRenderingModelBasedRenderer(RenderableDefinition renderableDefinition) throws ServletException {

        Renderer renderer;
        try {
            renderer = rendererRegistry.getRenderer(renderableDefinition.getRenderType());
        } catch (RegistrationException e) {
            throw new ServletException(e);
        }

        if (!(renderer instanceof RenderingModelBasedRenderer)) {
            throw new ServletException("Renderer [" + renderableDefinition.getRenderType() + "] does not support RenderingModel");
        }

        return (RenderingModelBasedRenderer) renderer;
    }

    protected boolean handleExecutionResult(RenderingModel renderingModel, String actionResult, TemplateDefinition templateDefinition, HttpServletRequest request, HttpServletResponse response) {

        // If the model rendered something on its own or sent a redirect we will not proceed with rendering.
        if (response.isCommitted()) {
            return false;
        }

        if (actionResult == null) {
            return false;
        }

        if (actionResult.equals(RenderingModel.SKIP_RENDERING)) {
            return true;
        }

        if (RequestDispatchUtil.dispatch(actionResult, request, response)) {
            return true;
        }

        return false;
    }

}
