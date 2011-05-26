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
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.templating.renderer.Renderer;
import info.magnolia.templating.template.RenderableDefinition;

import java.io.Writer;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;


public class DefaultRenderingEngine implements RenderingEngine {

    @Override
    public void render(Node content, RenderableDefinition definition, Map<String, Object> context, Writer out) throws RenderException {
        Content orgMainContent = null;
        Content orgCurrentContent = null;

        // TODO should we really still support the AggregationState?
        AggregationState state = null;
        try {
            state = getAggregationStateSafely();
            if (state != null) {
                orgMainContent = state.getMainContent();
                orgCurrentContent = state.getCurrentContent();

                state.setCurrentContent(ContentUtil.asContent(content));
                // if not yet set the passed content is the entry point of the rendering
                if (orgMainContent == null) {
                    state.setMainContent(ContentUtil.asContent(content));
                }
            }

            Renderer renderer = getRendererFor(definition);
            if (renderer == null) {
                throw new RenderException("Can't find renderer for type " + definition.getRenderType());
            }

            renderer.render(content, definition, context, out);
        }
        catch (RepositoryException e) {
            throw new RenderException("Can't render " + content, e);
        }

        if (state != null) {
            state.setMainContent(orgMainContent);
            state.setCurrentContent(orgCurrentContent);
        }
    }

    private Renderer getRendererFor(RenderableDefinition definition) {
        return null;
    }

    protected static AggregationState getAggregationStateSafely() {
        if (MgnlContext.isWebContext()) {
            return MgnlContext.getAggregationState();
        }
        return null;
    }

}
