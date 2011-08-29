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
package info.magnolia.rendering.model;

import javax.jcr.Node;

import info.magnolia.rendering.template.RenderableDefinition;


/**
 * A RenderingModel is used during rendering, it is analogous to the model part of the MVC pattern. It is associated with
 * a renderable and is executed before a content node using the renderable is rendered. The renderer instantiates the
 * RenderingModel and calls its execute method. While rendering the model is available to the template script under the
 * name <code>model</code> and the value returned by the execute method is available under the name
 * <code>actionResult</code>. It is commonly used to add backing logic to a component.
 *
 * <h3>Instantiation</h3>
 * It is created using reflection for each renderable and used only once, it <b>must</b> have a constructor that takes
 * the following constructor arguments:
 * <ul>
 *     <li>Node the node that is currently being rendered</li>
 *     <li>RenderableDefinition the renderable definition begin used to render the node</li>
 *     <li>RenderingModel the rendering model of the parent renderable</li>
 * </ul>
 *
 * After instantiation all request parameters are then mapped to the model's properties.
 *
 * <p>It can also abort the rendering by returning {@link #SKIP_RENDERING} from its execute method.</p>
 *
 * @param <RD> - an instance of {@link RenderableDefinition}
 * @version $Id$
 */
public interface RenderingModel <RD extends RenderableDefinition> {

    /**
     * A constant used in some special cases where rendering must be skipped, i.e. a redirect template.
     * It can be used by template models as a return value for the {@link RenderingModel#execute()} method to inform
     * {@link info.magnolia.rendering.renderer.AbstractRenderer} that it should not render anything in that particular
     * case.
     */
    public static final String SKIP_RENDERING = "skip-rendering";

    /**
     * The model of the parent paragraph or template.
     */
    RenderingModel< ? > getParent();

    /**
     * The content node tied to this model.
     */
    Node getContent();

    /**
     * The renderable (paragraph/template) tied to this model.
     */
    RD getDefinition();

    /**
     * Called after all properties were set. Can return a string which is passed
     * to the method.
     * {@link RenderableDefinition#determineTemplatePath(String, RenderingModel)}
     */
    String execute();

}
