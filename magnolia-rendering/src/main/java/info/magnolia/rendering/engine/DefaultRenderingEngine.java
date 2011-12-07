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
package info.magnolia.rendering.engine;

import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.renderer.Renderer;
import info.magnolia.rendering.renderer.registry.RendererRegistry;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.rendering.template.variation.RenderableVariationResolver;

import java.util.Collections;
import java.util.Map;

import javax.inject.Provider;
import javax.jcr.Node;

/**
 * Default implementation of {@link RenderingEngine}.
 *
 * @version $Id$
 */
public class DefaultRenderingEngine implements RenderingEngine {

    protected static final Map<String, Object> EMPTY_CONTEXT = Collections.emptyMap();

    private RendererRegistry rendererRegistry;
    private TemplateDefinitionAssignment templateDefinitionAssignment;
    private Provider<RenderingContext> renderingContextProvider;
    private RenderableVariationResolver variationResolver;

    /**
     * Used to create an observed proxy object.
     */
    protected DefaultRenderingEngine() {
    }

    public DefaultRenderingEngine(RendererRegistry rendererRegistry, TemplateDefinitionAssignment templateDefinitionAssignment, RenderableVariationResolver variationResolver, Provider<RenderingContext> renderingContextProvider) {
        this.rendererRegistry = rendererRegistry;
        this.templateDefinitionAssignment = templateDefinitionAssignment;
        this.variationResolver = variationResolver;
        this.renderingContextProvider = renderingContextProvider;
    }

    @Override
    public void render(Node content, OutputProvider out) throws RenderException {
        render(content, EMPTY_CONTEXT, out);
    }

    @Override
    public void render(Node content, Map<String, Object> contextObjects, OutputProvider out) throws RenderException {
        render(content, getRenderableDefinitionFor(content), contextObjects, out);
    }

    @Override
    public void render(Node content, RenderableDefinition definition, Map<String, Object> contextObjects, OutputProvider out) throws RenderException {

        final Renderer renderer = getRendererFor(definition);
        final RenderingContext renderingContext = getRenderingContext();

        RenderableDefinition variation = variationResolver.resolveVariation(definition);

        renderingContext.push(content, variation != null ? variation : definition, out);
        try {
            renderer.render(renderingContext, contextObjects);
        } finally {
            renderingContext.pop();
        }
    }

    protected RenderableDefinition getRenderableDefinitionFor(Node content) throws RenderException {
        try {
            return templateDefinitionAssignment.getAssignedTemplateDefinition(content);
        } catch (RegistrationException e) {
            throw new RenderException("Can't resolve RenderableDefinition for node [" + content + "]", e);
        }
    }

    protected Renderer getRendererFor(RenderableDefinition definition) throws RenderException {
        final String renderType = definition.getRenderType();
        if (renderType == null) {
            throw new RenderException("No renderType defined for definition [" + definition + "]");
        }
        try {
            return rendererRegistry.get(renderType);
        } catch (RegistrationException e) {
            throw new RenderException("Can't find renderer [" + renderType + "]", e);
        }
    }

    @Override
    public RenderingContext getRenderingContext() {
        return renderingContextProvider.get();
    }

}
