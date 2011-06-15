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
package info.magnolia.templating.rendering;

import java.util.Stack;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.templating.template.RenderableDefinition;

import javax.jcr.Node;


/**
 * Uses and updates the {@link AggregationState}.
 * FIXME we should not use the AggregationState anymore
 */
public class AggregationStateBasedRenderingContext implements RenderingContext {
    private AggregationState aggregationState;
    private Stack<Node> contentStack = new Stack<Node>();
    private Stack<RenderableDefinition> definitionStack = new Stack<RenderableDefinition>();

    public AggregationStateBasedRenderingContext(AggregationState aggregationState) {
        this.aggregationState = aggregationState;
    }

    @Override
    public Node getMainContent() {
        return aggregationState.getMainContent();
    }

    @Override
    public Node getCurrentContent() {
        return aggregationState.getCurrentContent();
    }

    @Override
    public RenderableDefinition getRenderableDefinition() {
        return definitionStack.peek();
    }

    @Override
    public void push(Node content, RenderableDefinition renderableDefinition) {
        if (aggregationState.getMainContent() == null) {
            aggregationState.setMainContent(content);
        }
        aggregationState.setCurrentContent(content);
        contentStack.push(content);
        definitionStack.push(renderableDefinition);
    }

    @Override
    public void pop() {
        aggregationState.setCurrentContent(contentStack.pop());
        definitionStack.pop();
    }

}
