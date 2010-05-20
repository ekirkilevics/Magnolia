/**
 * This file Copyright (c) 2010 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.templating.engine;

import java.io.IOException;
import java.io.Writer;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;
import info.magnolia.module.templating.ParagraphRenderer;
import info.magnolia.module.templating.ParagraphRendererManager;
import info.magnolia.module.templating.RenderException;
import info.magnolia.module.templating.RenderableDefinition;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;
import info.magnolia.module.templating.TemplateRenderer;
import info.magnolia.module.templating.TemplateRendererManager;


/**
 * Default implementation which determines the definition (template/paragraph) from the content's
 * meta data. Based on the node type (mgln:contentNode for paragraphs) a paragraph rendering or a
 * template rendering is performed.
 * @author pbaerfuss
 * @version $Id$
 *
 */
public class DefaultRenderingEngine implements RenderingEngine {

    protected enum RenderingHelper {
        PARAGRAPH {

            public RenderableDefinition getDefinition(String definitionName) {
                return ParagraphManager.getInstance().getParagraphDefinition(definitionName);
            }

            public Object getRenderer(RenderableDefinition definition) {
                return ParagraphRendererManager.getInstance().getRenderer(definition.getType());
            }

            void render(Content content, RenderableDefinition definition, Object renderer, Writer out) throws RenderException, IOException {
                ((ParagraphRenderer) renderer).render(content, (Paragraph) definition, out);
            }
        },

        TEMPLATE {

            public RenderableDefinition getDefinition(String definitionName) {
                AggregationState state = getAggregationStateSafely();
                String extension = null;
                if (state != null) {
                    extension = state.getExtension();
                }
                Template template = TemplateManager.getInstance().getTemplateDefinition(definitionName);
                if (template != null && extension != null) {
                    Template subTemplate = template.getSubTemplate(extension);
                    if (subTemplate != null) {
                        template = subTemplate;
                    }
                }
                return template;

            }

            public Object getRenderer(RenderableDefinition definition) {
                return TemplateRendererManager.getInstance().getRenderer(definition.getType());
            }

            public void render(Content content, RenderableDefinition definition, Object renderer, Writer out) throws RenderException, IOException {
                ((TemplateRenderer) renderer).renderTemplate(content, (Template) definition, out);
            }
        };

        abstract RenderableDefinition getDefinition(String definitionName);

        abstract Object getRenderer(RenderableDefinition definition);

        abstract void render(Content content, RenderableDefinition definition, Object renderer, Writer out) throws RenderException, IOException;
    }

    public void render(Content content, Writer out) throws RenderException  {
        render(content, determineAssignedDefinitionName(content), out);
    }

    public void render(Content content, String definitionName, Writer out) throws RenderException {
        // FIXME content can be null in case of a request to a node date having a template attribute set for the binary
        // this is probably not used anymore and should not be supported
        if (content != null && content.isNodeType(ItemType.CONTENTNODE.getSystemName())) {
            render(content, definitionName, RenderingHelper.PARAGRAPH, out);
        }
        else {
            render(content, definitionName, RenderingHelper.TEMPLATE, out);
        }
    }

    /**
     * Reads the template name from the meta data.
     */
    protected String determineAssignedDefinitionName(Content content) {
        return content.getMetaData().getTemplate();
    }

    /**
     * Will update the aggregation state and perform the rendering by using the helper.
     */
    protected void render(Content content, String definitionName, RenderingHelper helper, Writer out) throws RenderException {
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

        RenderableDefinition definition = helper.getDefinition(definitionName);
        if (definition == null) {
            throw new RenderException("Can't find renderable definition " + definitionName);
        }

        Object renderer = helper.getRenderer(definition);
        if (renderer == null) {
            throw new RenderException("Can't find renderer for type " + definition.getType());
        }

        try {
            helper.render(content, definition, renderer, out);
        }
        catch (IOException e) {
            throw new RenderException("Can't render " + content.getHandle(), e);
        }

        if (state != null) {
            state.setMainContent(orgMainContent);
            state.setCurrentContent(orgCurrentContent);
        }
    }

    protected static AggregationState getAggregationStateSafely() {
        if (MgnlContext.isWebContext()) {
            return MgnlContext.getAggregationState();
        }
        return null;
    }

}
