/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.templating.rendering;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.templating.renderer.Renderer;
import info.magnolia.templating.renderer.registry.RendererRegistrationException;
import info.magnolia.templating.renderer.registry.RendererRegistry;
import info.magnolia.templating.template.RenderableDefinition;
import info.magnolia.templating.template.TemplateDefinition;
import info.magnolia.templating.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.templating.template.registry.TemplateDefinitionRegistrationException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.jcr.Node;

/**
 * Default implementation of {@link RenderingEngine}. Maintains the {@link AggregationState}.
 * @version $Id$
 *
 */
public class DefaultRenderingEngine implements RenderingEngine {

    private static final String RENDERING_CONTEXT_ATTRIBUTE = RenderingContext.class.getName();

    private static final Map<String, Object> EMPTY_CONTEXT = Collections.emptyMap();

    private RendererRegistry rendererRegistry;
    private TemplateDefinitionAssignment templateDefinitionAssignment;

    /**
     * Used to create an observed proxy object.
     */
    protected DefaultRenderingEngine() {
    }

    public DefaultRenderingEngine(RendererRegistry rendererRegistry, TemplateDefinitionAssignment templateDefinitionAssignment) {
        this.rendererRegistry = rendererRegistry;
        this.templateDefinitionAssignment = templateDefinitionAssignment;
    }

    @Override
    public void render(Node content, Appendable out) throws RenderException {
        render(content, EMPTY_CONTEXT, out);
    }

    @Override
    public void render(Node content, Map<String, Object> contextObjects, Appendable out) throws RenderException {
        TemplateDefinition templateDefinition;
        try {
            templateDefinition = templateDefinitionAssignment.getAssignedTemplateDefinition(content);
        }
        catch (TemplateDefinitionRegistrationException e) {
            throw new RenderException("Can't render node " + content, e);
        }
        render(content, templateDefinition, contextObjects, out);
    }

    @Override
    public void render(Node content, RenderableDefinition definition, Map<String, Object> contextObjects, Appendable out) throws RenderException {

        final Renderer renderer = getRendererFor(definition);
        final RenderingContext renderingContext = getRenderingContext();
        renderingContext.push(content, definition);

        try {
            renderer.render(content, definition, contextObjects, out);
        }
        catch (IOException e) {
            throw new RenderException(e);
        }
        finally{
            renderingContext.pop();
        }
    }

    protected Renderer getRendererFor(RenderableDefinition definition) throws RenderException {
        try {
            return rendererRegistry.getRenderer(definition.getRenderType());
        }
        catch (RendererRegistrationException e) {
            throw new RenderException("Can't find renderer for type " + definition.getRenderType(), e);
        }
    }

    protected static AggregationState getAggregationStateSafely() {
        if (MgnlContext.isWebContext()) {
            return MgnlContext.getAggregationState();
        }
        return null;
    }

    @Override
    public RenderingContext getRenderingContext() {
        if(MgnlContext.hasAttribute(RENDERING_CONTEXT_ATTRIBUTE)){
            return MgnlContext.getAttribute(RENDERING_CONTEXT_ATTRIBUTE);
        }
        else {
            // FIXME don't use the concrete class here but if we do the registration in the module descriptor the context also gets created it the main container
            final RenderingContext renderingContext = Components.getComponentProvider().newInstance(AggregationStateBasedRenderingContext.class, getAggregationStateSafely());
            MgnlContext.setAttribute(RENDERING_CONTEXT_ATTRIBUTE, renderingContext);
            return renderingContext;
        }
    }

}
