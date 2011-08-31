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
package info.magnolia.rendering.context;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.rendering.engine.OutputProvider;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.util.AppendableWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;

import com.google.inject.servlet.RequestScoped;

/**
 * RenderingContext implementation that uses AggregationState.
 *
 * @version $Id$
 */
@RequestScoped
public class AggregationStateBasedRenderingContext implements RenderingContext {

    // TODO dlipp: add reasonable javadoc! Uses and updates the {@link AggregationState}.
    // FIXME we should not use the AggregationState anymore

    private final AggregationState aggregationState;
    private RenderableDefinition currentRenderableDefinition;
    private final Stack<Node> contentStack = new Stack<Node>();
    private final Stack<RenderableDefinition> definitionStack = new Stack<RenderableDefinition>();
    private OutputProvider out;

    @Inject
    public AggregationStateBasedRenderingContext(Provider<AggregationState> aggregationStateProvider) {
        this.aggregationState = aggregationStateProvider.get();
    }

    public AggregationStateBasedRenderingContext(AggregationState aggregationState) {
        this.aggregationState = aggregationState;
    }

    @Override
    public Node getMainContent() {
        // there is still a possiblity of call to this method before push!
        if (aggregationState.getMainContent() == null) {
            return null;
        }
        return aggregationState.getMainContent().getJCRNode();
    }

    @Override
    public Node getCurrentContent() {
        return aggregationState.getCurrentContent().getJCRNode();
    }

    @Override
    public RenderableDefinition getRenderableDefinition() {
        return currentRenderableDefinition;
    }

    @Override
    public void push(Node content, RenderableDefinition renderableDefinition, OutputProvider out) {
        if (aggregationState.getMainContent() == null) {
            aggregationState.setMainContent(ContentUtil.asContent(content));
        }

        contentStack.push(aggregationState.getCurrentContent().getJCRNode());
        definitionStack.push(currentRenderableDefinition);

        aggregationState.setCurrentContent(ContentUtil.asContent(content));
        currentRenderableDefinition = renderableDefinition;
        this.out = out;
    }

    @Override
    public void pop() {
        currentRenderableDefinition = definitionStack.pop();
        aggregationState.setCurrentContent(ContentUtil.asContent(contentStack.pop()));
        // Note that we do not restore main content
    }

    @Override
    public AppendableWriter getAppendable() throws RenderException, IOException {
        return new AppendableWriter(this.out.getAppendable());
    }

    @Override
    public OutputStream getOutputStream() throws RenderException, IOException {
        return this.out.getOutputStream();
    }

}
