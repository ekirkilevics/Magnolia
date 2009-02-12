/**
 * This file Copyright (c) 2008-2009 Magnolia International
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

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.i18n.I18nContentWrapper;
import info.magnolia.context.MgnlContext;


/**
 * Abstract renderer which can be used to implement paragraph or template renderers.
 * Sets up the context by providing the following objects: content, aggregationState, page, model, actionResult, mgnl
 *
 * @author pbracher
 * @version $Id$
 *
 */
public abstract class AbstractRenderer {

    private static final String MODEL_ATTRIBUTE = RenderingModel.class.getName();

    public AbstractRenderer() {
    }

    protected void render(Content content, RenderableDefinition definition, Writer out) throws RenderException {

        final RenderingModel parentModel = (RenderingModel) MgnlContext.getAttribute(MODEL_ATTRIBUTE);
        RenderingModel model;
        try {
            model = newModel(content, definition, parentModel);
        }
        catch (Exception e) {
            throw new RenderException("Can't create rendering model: " + ExceptionUtils.getRootCauseMessage(e), e);
        }

        final String actionResult = model.execute();
        String templatePath = determineTemplatePath(content, definition, model, actionResult);

        final Map ctx = newContext();
        final Map savedContextState = saveContextState(ctx);
        setupContext(ctx, content, definition, model, actionResult);
        MgnlContext.setAttribute(MODEL_ATTRIBUTE, model);
        onRender(content, definition, out, ctx, templatePath);
        MgnlContext.setAttribute(MODEL_ATTRIBUTE, parentModel);

        restoreContext(ctx, savedContextState);
    }

    protected String determineTemplatePath(Content content, RenderableDefinition definition, RenderingModel model, final String actionResult) {
        String templatePath = definition.determineTemplatePath(actionResult, model);

        if (templatePath == null) {
            throw new IllegalStateException("Unable to render " + definition.getClass().getName() + " " + definition.getName() + " in page " + content.getHandle() + ": templatePath not set.");
        }
        return templatePath;
    }

    /**
     * Creates the model for this rendering process. Will set the properties
     */
    protected RenderingModel newModel(Content content, RenderableDefinition definition, RenderingModel parentModel) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return definition.newModel(content, definition, parentModel);
    }

    protected Map saveContextState(final Map ctx) {
        Map state = new HashMap();
        // save former values
        saveAttribute(ctx, state, "content");
        saveAttribute(ctx, state, "actionResult");
        saveAttribute(ctx, state, "state");
        saveAttribute(ctx, state, "def");

        saveAttribute(ctx, state, getPageAttributeName());
        return state;
    }

    protected void saveAttribute(final Map ctx, Map state, String name) {
        state.put(name, ctx.get(name));
    }

    protected void restoreContext(final Map ctx, Map state) {
        for (Iterator iterator = state.keySet().iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            setContextAttribute(ctx, name, state.get(name));
        }
    }

    protected void setupContext(final Map ctx, Content content, RenderableDefinition definition, RenderingModel model, Object actionResult){
        final AggregationState aggregationState = MgnlContext.getAggregationState();
        final Content page = aggregationState.getMainContent();

        setContextAttribute(ctx, getPageAttributeName(), wrapNodeForTemplate(page));
        setContextAttribute(ctx, "content", wrapNodeForTemplate(content));
        setContextAttribute(ctx, "def", definition);
        setContextAttribute(ctx, "state", aggregationState);
        setContextAttribute(ctx, "mgnl", getMagnoliaTemplatingUtilities());
        setContextAttribute(ctx, "model", model);
        setContextAttribute(ctx, "actionResult", actionResult);
    }

    protected MagnoliaTemplatingUtilities getMagnoliaTemplatingUtilities() {
        return MagnoliaTemplatingUtilities.getInstance();
    }

    /**
     * Wraps a node before exposing it to the template renderer.
     * @param currentContent the actual content being exposed to the template
     * @see info.magnolia.module.templating.paragraphs.JspParagraphRenderer
     * TODO : return an Object instance instead - more flexibility for the template engine ?
     */
    protected Content wrapNodeForTemplate(Content currentContent) {
        return new I18nContentWrapper(currentContent);
    }

    protected Object setContextAttribute(final Map ctx, final String name, Object value) {
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
    protected abstract Map newContext();

    /**
     * Finally execute the rendering.
     * @param content TODO
     */
    protected abstract void onRender(Content content, RenderableDefinition definition, Writer out, Map ctx, String templatePath) throws RenderException;

}