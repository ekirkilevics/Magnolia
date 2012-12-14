/**
 * This file Copyright (c) 2008-2012 Magnolia International
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
import info.magnolia.jcr.decoration.ContentDecoratorUtil;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.wrapper.ChannelVisibilityContentDecorator;
import info.magnolia.jcr.wrapper.HTMLEscapingNodeWrapper;
import info.magnolia.jcr.wrapper.I18nNodeWrapper;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.MgnlInstantiationException;
import info.magnolia.objectfactory.ParameterInfo;
import info.magnolia.objectfactory.ParameterResolver;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.model.EarlyExecutionAware;
import info.magnolia.rendering.model.ModelExecutionFilter;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.RenderableDefinition;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;


/**
 * Abstract renderer with support for typical functionality such as setting context attributes and executing a
 * rendering model. Sets up the context by providing the following objects:
 * <ul>
 *     <li>content</li>
 *     <li>def</li>
 *     <li>state</li>
 *     <li>model</li>
 *     <li>actionResult</li>
 * </ul>
 *
 * @version $Id$
 */
public abstract class AbstractRenderer implements Renderer, RenderingModelBasedRenderer {

    protected static final String MODEL_ATTRIBUTE = RenderingModel.class.getName();

    private Map<String, ContextAttributeConfiguration> contextAttributes = new HashMap<String, ContextAttributeConfiguration>();

    @Override
    public void render(RenderingContext renderingCtx, Map<String, Object> contextObjects) throws RenderException {

        final RenderingModel<?> parentModel = MgnlContext.getAttribute(MODEL_ATTRIBUTE);
        Node content = renderingCtx.getCurrentContent();
        RenderableDefinition definition = renderingCtx.getRenderableDefinition();

        RenderingModel<?> model = null;
        String actionResult = null;

        if (content != null) {
            String uuid;
            try {
                uuid = content.getIdentifier();
            }
            catch (RepositoryException e) {
                throw new RenderException(e);
            }

            model = MgnlContext.getAttribute(ModelExecutionFilter.MODEL_ATTRIBUTE_PREFIX + uuid);
            if (model != null) {
                actionResult = (String) MgnlContext.getAttribute(ModelExecutionFilter.ACTION_RESULT_ATTRIBUTE_PREFIX + uuid);
                if (model instanceof EarlyExecutionAware) {
                    ((EarlyExecutionAware)model).setParent(parentModel);
                }
            }
        }

        if (model == null) {
            model = newModel(content, definition, parentModel);
            if (model != null) {
                actionResult = model.execute();
                if (RenderingModel.SKIP_RENDERING.equals(actionResult)) {
                    return;
                }
            }
        }

        String templatePath = resolveTemplateScript(content, definition, model, actionResult);
        if(templatePath == null){
            throw new RenderException("No template script defined for the template definition [" + definition + "]");
        }

        final Map<String, Object> ctx = newContext();
        final Map<String, Object> savedContextState = saveContextState(ctx);
        setupContext(ctx, content, definition, model, actionResult);
        ctx.putAll(contextObjects);
        MgnlContext.setAttribute(MODEL_ATTRIBUTE, model);
        content = wrapNodeForModel(content);
        renderingCtx.push(content, definition);
        try {
            onRender(content, definition, renderingCtx, ctx, templatePath);
        } finally {
            renderingCtx.pop();
        }
        MgnlContext.setAttribute(MODEL_ATTRIBUTE, parentModel);

        restoreContext(ctx, savedContextState);
    }

    /**
     * Hook-method to be overriden when required. Default implementation ignores all arguments except definition.
     *
     * @param definition reference templateScript is retrieved from
     *
     * @return the templateScript to use
     */
    protected String resolveTemplateScript(Node content, RenderableDefinition definition, RenderingModel<?> model, final String actionResult) {
        return definition.getTemplateScript();
    }

    /**
     * Instantiates the model based on the class defined by the
     * {@link info.magnolia.rendering.template.RenderableDefinition#getModelClass()} property. All the request
     * parameters are then mapped to the model's properties.
     */
    @Override
    public RenderingModel<?> newModel(final Node content, final RenderableDefinition definition, final RenderingModel<?> parentModel) throws RenderException {

        Class clazz = definition.getModelClass();

        // if none is set we default to RenderingModelImpl, so there will always be a model available in templates
        if (clazz == null) {
            clazz = RenderingModelImpl.class;
        }

        final Node wrappedContent = wrapNodeForModel(content);

        return newModel(clazz, wrappedContent, definition, parentModel);
    }

    protected <T extends RenderingModel<?>> T newModel(Class<T> modelClass, final Node content, final RenderableDefinition definition, final RenderingModel<?> parentModel) throws RenderException {

        try {

            T model = Components.getComponentProvider().newInstanceWithParameterResolvers(modelClass,
                    new ParameterResolver() {
                        @Override
                        public Object resolveParameter(ParameterInfo parameter) {
                            if (parameter.getParameterType().equals(Node.class)) {
                                return content;
                            }
                            if (parameter.getParameterType().isAssignableFrom(definition.getClass())) {
                                return definition;
                            }
                            if (parameter.getParameterType().equals(RenderingModel.class)) {
                                return parentModel;
                            }
                            return UNRESOLVED;
                        }
                    }
            );

            // populate the instance with values given as request parameters
            Map<String, String[]> params = MgnlContext.getWebContext().getRequest().getParameterMap();
            Map<String, Object> filtered = new HashMap<String, Object>();
            if (params != null) {
                for (Entry<String, String[]> entry : params.entrySet()) {
                    String key = entry.getKey();
                    String[] value = entry.getValue();
                    if (StringUtils.contains(key, "[")) {
                        key = StringUtils.substringBefore(key, "[");
                    }
                    filtered.put(key, value);
                }
            }

            BeanUtils.populate(model, filtered);

            return model;

        } catch (MgnlInstantiationException e) {
            throw new RenderException("Can't instantiate model: " + modelClass, e);
        } catch (InvocationTargetException e) {
            throw new RenderException("Can't create rendering model: " + ExceptionUtils.getRootCauseMessage(e), e);
        } catch (IllegalAccessException e) {
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

        setContextAttribute(ctx, "content", content != null ? new ContentMap(wrapNodeForTemplate(content)) : null);
        setContextAttribute(ctx, "def", definition);
        setContextAttribute(ctx, "state", getAggregationStateSafely());
        setContextAttribute(ctx, "model", model);
        setContextAttribute(ctx, "actionResult", actionResult);

        for (Entry<String, ContextAttributeConfiguration> entry : contextAttributes.entrySet()) {
            setContextAttribute(ctx, entry.getKey(), Components.getComponent(entry.getValue().getComponentClass()));
        }

    }

    /**
     * Gets the current main content or returns null if aggregation state is not set.
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
     * @return the wrapped content
     */
    protected Node wrapNodeForModel(Node content) {
        NodeUtil.deepUnwrap(content, HTMLEscapingNodeWrapper.class);
        content = wrapWithChannelVisibilityWrapper(content);
        content = wrapWithI18NWrapper(content);
        return content;
    }

    /**
     * Wraps the current content node for exposing it to the template script as a context attribute.
     *
     * @param content the actual content
     * @return the wrapped content
     */
    protected Node wrapNodeForTemplate(Node content) {
        content = wrapWithChannelVisibilityWrapper(content);
        content = wrapWithI18NWrapper(content);
        content = wrapWithHTMLEscapingWrapper(content);
        return content;
    }

    private Node wrapWithHTMLEscapingWrapper(Node content) {
        if(!NodeUtil.isWrappedWith(content, HTMLEscapingNodeWrapper.class)){
            content = new HTMLEscapingNodeWrapper(content, true);
        }
        return content;
    }

    private Node wrapWithI18NWrapper(Node content) {
        if(!NodeUtil.isWrappedWith(content, I18nNodeWrapper.class)){
            content = new I18nNodeWrapper(content);
        }
        return content;
    }

    private Node wrapWithChannelVisibilityWrapper(Node content) {
        // If it's already wrapped then we don't need to add a new one
        if (ContentDecoratorUtil.isDecoratedWith(content, ChannelVisibilityContentDecorator.class)) {
            return content;
        }
        AggregationState aggregationState = getAggregationStateSafely();
        if (aggregationState == null) {
            return content;
        }
        String channel = aggregationState.getChannel().getName();
        if (StringUtils.isEmpty(channel) || channel.equalsIgnoreCase("all")) {
            return content;
        }
        return new ChannelVisibilityContentDecorator(channel).wrapNode(content);
    }

    protected Object setContextAttribute(final Map<String, Object> ctx, final String name, Object value) {
        return ctx.put(name, value);
    }

    public Map<String, ContextAttributeConfiguration> getContextAttributes() {
        return contextAttributes;
    }

    public void setContextAttributes(Map<String, ContextAttributeConfiguration> contextAttributes) {
        if(this.contextAttributes!=null) {
            this.contextAttributes.putAll(contextAttributes);
        } else {
            this.contextAttributes = contextAttributes;
        }
    }

    public void addContextAttribute(String name, ContextAttributeConfiguration contextAttributeConfiguration){
        this.contextAttributes.put(name, contextAttributeConfiguration);
    }

    /**
     * Create a new context object which is a map.
     */
    protected abstract Map<String, Object> newContext();

    /**
     * Finally execute the rendering.
     */
    protected abstract void onRender(Node content, RenderableDefinition definition, RenderingContext renderingCtx, Map<String, Object> ctx, String templateScript) throws RenderException;

}
