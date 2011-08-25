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
package info.magnolia.module.templating;

import info.magnolia.cms.core.Content;
import info.magnolia.rendering.template.RenderableDefinition;

/**
 * Deprecated.
 * @deprecated since 4.5, use {@link info.magnolia.rendering.model.RenderingModel}
 * @param <RD> the {@link RenderableDefinition} bound to the model
 */
@Deprecated
public interface RenderingModel<RD extends RenderableDefinition> {

    // TODO document that bean properties will be set with request parameters

    /**
     * A constant used in some special cases where rendering must be skipped, i.e. a redirect template. It can be used by template models as a return value for the {@link RenderingModel#execute()} method to inform {@link info.magnolia.rendering.renderer.AbstractRenderer} that it should not render anything in that particular case.
     */
    public static final String SKIP_RENDERING = "skip-rendering";

    /**
     * The model of the parent paragraph or template.
     */
    RenderingModel<?> getParent();

    /**
     * The content node tied to this model.
     */
    Content getContent();

    /**
     * The renderable (paragraph/template) tied to this model.
     */
    RD getDefinition();

    /**
     * Called after all properties were set. Can return a string which is passed to the method. {@link RenderableDefinition#determineTemplatePath(String, RenderingModel)}
     */
    String execute();

}
