/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.rendering.context;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.objectfactory.annotation.LocalScoped;
import info.magnolia.rendering.engine.OutputProvider;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderExceptionHandler;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.util.AppendableWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EmptyStackException;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;

/**
 * RenderingContext implementation that uses AggregationState.
 *
 * @version $Id$
 */
@LocalScoped
public class AggregationStateBasedRenderingContext implements RenderingContext {

    // TODO dlipp: add reasonable javadoc! Uses and updates the {@link AggregationState}.
    // FIXME we should not use the AggregationState anymore

    private static class StackState {

        RenderableDefinition renderableDefinition;
        OutputProvider outputProvider;
        Content legacyContent;

        private StackState(RenderableDefinition renderableDefinition, OutputProvider outputProvider, Content legacyContent) {
            this.renderableDefinition = renderableDefinition;
            this.outputProvider = outputProvider;
            this.legacyContent = legacyContent;
        }
    }

    private final AggregationState aggregationState;
    private final Stack<StackState> stack = new Stack<StackState>();
    private RenderableDefinition currentRenderableDefinition;
    private OutputProvider currentOutputProvider;
    private RenderExceptionHandler exceptionHandler;

    /**
     * We keep the current state in local variables and start using the stack only for the second push operation. This
     * variable is 0 before the first push, 1 when we have local variables set, and greater than 1 when we have things
     * on stack.
     */
    private int depth = 0;

    @Inject
    public AggregationStateBasedRenderingContext(Provider<AggregationState> aggregationStateProvider, RenderExceptionHandler exceptionHandler) {
        this(aggregationStateProvider.get(), exceptionHandler);
    }

    public AggregationStateBasedRenderingContext(AggregationState aggregationState, RenderExceptionHandler exceptionHandler) {
        this.aggregationState = aggregationState;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public Node getMainContent() {
        // there is still a possibility of call to this method before push!
        Content mainContent = aggregationState.getMainContent();
        return mainContent != null ? mainContent.getJCRNode() : null;
    }

    @Override
    public Node getCurrentContent() {
        Content currentContent = aggregationState.getCurrentContent();
        return currentContent != null ? currentContent.getJCRNode() : null;
    }

    @Override
    public RenderableDefinition getRenderableDefinition() {
        return currentRenderableDefinition;
    }

    @Override
    public void push(Node content, RenderableDefinition renderableDefinition) {
        push(content, renderableDefinition, null);
    }

    @Override
    public void push(Node content, RenderableDefinition renderableDefinition, OutputProvider outputProvider) {

        // Creating the Content object can fail with an exception, by doing it before anything else we don't risk ending
        // up having inconsistent state due to a partially completed push.
        Content legacyContent = ContentUtil.asContent(content);

        if (aggregationState.getMainContent() == null) {
            aggregationState.setMainContent(legacyContent);
        }

        if (depth > 0) {
            stack.push(new StackState(currentRenderableDefinition, currentOutputProvider, aggregationState.getCurrentContent()));
        }
        aggregationState.setCurrentContent(legacyContent);
        currentRenderableDefinition = renderableDefinition;
        currentOutputProvider = outputProvider != null ? outputProvider : currentOutputProvider;
        depth++;
    }

    @Override
    public void pop() {
        if (depth == 0) {
            throw new EmptyStackException();
        } else if (depth == 1) {
            aggregationState.setCurrentContent(null);
            currentRenderableDefinition = null;
            currentOutputProvider = null;
        } else {
            StackState state = stack.pop();
            aggregationState.setCurrentContent(state.legacyContent);
            currentRenderableDefinition = state.renderableDefinition;
            currentOutputProvider = state.outputProvider;
        }
        depth--;
        // Note that we do not restore main content
    }

    @Override
    public OutputProvider getOutputProvider() {
        return currentOutputProvider;
    }

    @Override
    public AppendableWriter getAppendable() throws IOException {
        return new AppendableWriter(this.currentOutputProvider.getAppendable());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.currentOutputProvider.getOutputStream();
    }

    @Override
    public void handleException(RenderException renderException) {
        exceptionHandler.handleException(renderException, this);
    }
}
