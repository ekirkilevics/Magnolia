/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.rendering.renderer;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.model.EarlyExecutionAware;
import info.magnolia.rendering.model.ModelExecutionFilter;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.template.RenderableDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.exception.ExceptionUtils;


/**
 * Abstract renderer which can be used to implement paragraph or template renderers.
 * Sets up the context by providing the following objects: content, aggregationState, page, model, actionResult, mgnl
 *
 * @version $Id$
 *
 */
public abstract class AbstractRenderer implements Renderer, RenderingModelBasedRenderer {

    protected static final String MODEL_ATTRIBUTE = RenderingModel.class.getName();

    @Override
    public void render(RenderingContext renderingCtx, Map<String, Object> contextObjects) throws RenderException {

        final RenderingModel<?> parentModel = MgnlContext.getAttribute(MODEL_ATTRIBUTE);
        Node content = renderingCtx.getCurrentContent();
        RenderableDefinition definition = renderingCtx.getRenderableDefinition();

        RenderingModel<?> model;
        String actionResult;

        String uuid;
        try {
            uuid = content.getUUID();
        }
        catch (RepositoryException e) {
            throw new RenderException(e);
        }

        model = MgnlContext.getAttribute(ModelExecutionFilter.MODEL_ATTRIBUTE_PREFIX + uuid);

        if (model == null) {

            model = newModel(content, definition, parentModel);

            actionResult = model.execute();

            if (RenderingModel.SKIP_RENDERING.equals(actionResult)) {
                return;
            }
        } else {
            actionResult = (String) MgnlContext.getAttribute(ModelExecutionFilter.ACTION_RESULT_ATTRIBUTE_PREFIX + uuid);
            if (model instanceof EarlyExecutionAware) {
                ((EarlyExecutionAware)model).setParent(parentModel);
            }
        }

        String templatePath = determineTemplatePath(content, definition, model, actionResult);
        if(templatePath == null){
            throw new RenderException("No template script defined for the template definition [" + definition + "]");
        }

        final Map<String, Object> ctx = newContext();
        final Map<String, Object> savedContextState = saveContextState(ctx);
        setupContext(ctx, content, definition, model, actionResult);
        ctx.putAll(contextObjects);
        MgnlContext.setAttribute(MODEL_ATTRIBUTE, model);
        onRender(content, definition, renderingCtx, ctx, templatePath);
        MgnlContext.setAttribute(MODEL_ATTRIBUTE, parentModel);

        restoreContext(ctx, savedContextState);
    }

    protected String determineTemplatePath(Node content, RenderableDefinition definition, RenderingModel<?> model, final String actionResult) {

        // FIXME reactivate this code
        return definition.getTemplateScript();
        //        String templatePath = definition.determineTemplatePath(actionResult, model);
        //
        //        if (templatePath == null) {
        //            throw new IllegalStateException("Unable to render " + definition.getClass().getName() + " " + definition.getName() + " in page " + content.getHandle() + ": templatePath not set.");
        //        }
        //        return templatePath;
    }

    /**
     * Creates the model for this rendering process. Will set the properties
     */
    @Override
    public RenderingModel<?> newModel(Node content, RenderableDefinition definition, RenderingModel<?> parentModel) throws RenderException {
        try {
            final Node wrappedContent = wrapNodeForModel(content, getMainContentSafely(content));
            return definition.newModel(wrappedContent, definition, parentModel);
        } catch (Exception e) {
            throw new RenderException("Can't create rendering model: " + ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

    protected Map<String, Object> saveContextState(final Map<String, Object> ctx) {
        Map<String, Object> state = new HashMap<String, Object>();
        // save former values
        saveAttribute(ctx, state, "content");
        saveAttribute(ctx, state, "def");
        saveAttribute(ctx, state, "state");
        saveAttribute(ctx, state, "model");
        saveAttribute(ctx, state, "actionResult");

        saveAttribute(ctx, state, getPageAttributeName());
        return state;
    }

    protected void saveAttribute(final Map<String, Object> ctx, Map<String, Object> state, String name) {
        state.put(name, ctx.get(name));
    }

    protected void restoreContext(final Map<String, Object> ctx, Map<String, Object> state) {
        for (Entry<String, Object> entry : state.entrySet()) {
            setContextAttribute(ctx, entry.getKey(), entry.getValue());
        }
    }

    protected void setupContext(final Map<String, Object> ctx, Node content, RenderableDefinition definition, RenderingModel<?> model, Object actionResult){
        final Node mainContent = getMainContentSafely(content);

        setContextAttribute(ctx, getPageAttributeName(), wrapNodeForTemplate(mainContent, mainContent));
        setContextAttribute(ctx, "content", new ContentMap(wrapNodeForTemplate(content, mainContent)));
        setContextAttribute(ctx, "def", definition);
        setContextAttribute(ctx, "state", getAggregationStateSafely());
        setContextAttribute(ctx, "model", model);
        setContextAttribute(ctx, "actionResult", actionResult);
    }

    /**
     * Gets the current main contain and treats the situation where the context is not a web context nicely by using the current content instead.
     */
    protected Node getMainContentSafely(Node content) {
        AggregationState state = getAggregationStateSafely();
        return state == null ? content : state.getMainContent().getJCRNode();
    }

    /**
     * This gets the aggregation state without throwing an exception if the current context is not a WebContext.
     */
    protected AggregationState getAggregationStateSafely() {
        if(MgnlContext.isWebContext()){
            return MgnlContext.getAggregationState();
        }
        return null;
    }

    /**
     * Wraps the current content node before passing it to the model.
     * @param content the actual content
     * @param mainContent the current "main content" or "page", which might be needed in certain wrapping situations
     */
    protected Node wrapNodeForModel(Node content, Node mainContent) {
        //      FIXME
        return content;
        //        return new I18nContentWrapper(content);
    }

    /**
     * Wraps the current content node before exposing it to the template renderer.
     * @param content the actual content being exposed to the template
     * @param mainContent the current "main content" or "page", which might be needed in certain wrapping situations
     * @see info.magnolia.module.templating.paragraphs.JspParagraphRenderer
     * TODO : return an Object instance instead - more flexibility for the template engine ?
     */
    protected Node wrapNodeForTemplate(Node content, Node mainContent) {
        //        FIXME
        return content;
        //        return new I18nContentWrapper(content);
    }

    protected Object setContextAttribute(final Map<String, Object> ctx, final String name, Object value) {
        return ctx.put(name, value);
    }

    /**
     * Used to give JSP implementations to give the chance to use on other name than page which is a reserved name in JSPs.
     */
    protected String getPageAttributeName() {
        return "page";
    }

    /**
     * Create a new context object which is a map.
     */
    protected abstract Map<String, Object> newContext();

    /**
     * Finally execute the rendering.
     * @param content TODO
     */
    protected abstract void onRender(Node content, RenderableDefinition definition, RenderingContext renderingCtx, Map<String, Object> ctx, String templateScript) throws RenderException;

}
