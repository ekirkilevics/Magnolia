/**
 * This file Copyright (c) 2010 Magnolia International
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

import java.lang.reflect.InvocationTargetException;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.i18n.I18nContentWrapper;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;

/**
 * Creates RenderingModel instances for templates and paragraphs.
 *
 * @author tmattsson
 * @see info.magnolia.module.templating.AbstractRenderer
 * @see info.magnolia.module.templating.ModelExecutionFilter
 */
public class RenderingModelFactory {

    public static RenderingModelFactory getInstance() {
        return Components.getSingleton(RenderingModelFactory.class);
    }

    /**
     * Creates the model for this rendering process. Will set the properties
     */
    public RenderingModel newModel(Content content, RenderableDefinition definition, RenderingModel parentModel) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        final Content wrappedContent = wrapNodeForModel(content, getMainContentSafely(content));
        return definition.newModel(wrappedContent, definition, parentModel);
    }

    /**
     * Gets the current main contain and treats the situation where the context is not a web context nicely by using the current content instead.
     */
    protected Content getMainContentSafely(Content current) {
        AggregationState state = getAggregationStateSafely();
        if (state != null) {
            return state.getMainContent();
        }
        return current;
    }

    /**
     * This gets the aggregation state without throwing an exception if the current context is not a WebContext.
     */
    protected AggregationState getAggregationStateSafely() {
        if (MgnlContext.isWebContext()) {
            return MgnlContext.getAggregationState();
        }
        return null;
    }

    /**
     * Wraps the current content node before passing it to the model.
     *
     * @param currentContent the actual content
     * @param mainContent    the current "main content" or "page", which might be needed in certain wrapping situations
     */
    protected Content wrapNodeForModel(Content currentContent, Content mainContent) {
        return new I18nContentWrapper(currentContent);
    }
}
