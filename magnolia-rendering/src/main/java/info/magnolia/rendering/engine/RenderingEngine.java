/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.rendering.engine;

import java.util.Map;
import javax.jcr.Node;

import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.template.RenderableDefinition;


/**
 * The RenderingEngine is the main entry point for rendering content. It's main task is to prepare for rendering by
 * setting up the {@link RenderingContext} and then calling on a {@link info.magnolia.rendering.renderer.Renderer} to
 * do the actual rendering.
 *
 * @version $Id$
 */
public interface RenderingEngine {

    /**
     * Renders the content with its assigned template. Uses {@link info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment}
     * to resolve the template assigned to the node.
     *
     * @param content the node to render
     * @param out     the OutputProvider to be used for output or null if the OutputProvider already set in RenderingContext should be used
     */
    void render(Node content, OutputProvider out) throws RenderException;

    /**
     * Renders the content with its assigned template and exposes the given context objects to the template script. Uses
     * {@link info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment} to resolve the template assigned
     * to the node.
     *
     * @param content        the node to render
     * @param contextObjects objects to expose to the template script
     * @param out            the OutputProvider to be used for output or null if the OutputProvider already set in RenderingContext should be used
     */
    void render(Node content, Map<String, Object> contextObjects, OutputProvider out) throws RenderException;

    /**
     * Uses a specific {@link RenderableDefinition} to render the content and exposes the given context objects to the
     * template script.
     *
     * @param content        the node to render
     * @param contextObjects objects to expose to the template script
     * @param out            the OutputProvider to be used for output or null if the OutputProvider already set in RenderingContext should be used
     */
    void render(Node content, RenderableDefinition definition, Map<String, Object> contextObjects, OutputProvider out) throws RenderException;

    /**
     * Returns the current {@link RenderingContext}.
     * FIXME is this the right place? should we use IoC
     */
    public RenderingContext getRenderingContext();
}
