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
package info.magnolia.templating.elements;

import java.io.IOException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.template.RenderableDefinition;


/**
 * Common super class for templating elements, provides utility methods and defaults.
 *
 * @version $Id$
 */
public abstract class AbstractTemplatingElement implements TemplatingElement {

    private static final String DEFAULT_I18N_BASENAME = "info.magnolia.module.templating.messages";

    private final ServerConfiguration server;
    private final RenderingContext renderingContext;

    protected AbstractTemplatingElement(final ServerConfiguration server, final RenderingContext renderingContext) {
        this.server = server;
        this.renderingContext = renderingContext;
    }

    @Override
    public void begin(Appendable out) throws IOException, RenderException {
    }

    /**
     * Override this method if you need to "do something" once the component is rendered, i.e cleanup the context.
     */
    @Override
    public void end(Appendable out) throws IOException, RenderException {
    }

    /**
     * Returns the "current content" from the aggregation state. Override this method if your component needs a
     * different target node.
     */
    protected Node currentContent() {
        final Node currentContent = renderingContext.getCurrentContent();
        if (currentContent == null) {
            throw new IllegalStateException(
                    "Could not determine currentContent from RenderingContext, currentContent is null");
        }
        return currentContent;
    }

    protected String getDefinitionMessage(RenderableDefinition definition, String key) throws RepositoryException {
        Messages messages = MessagesUtil.chain(definition.getI18nBasename(), MessagesUtil.chainWithDefault(DEFAULT_I18N_BASENAME));
        return messages.getWithDefault(key, key);
    }

    protected String getInterfaceMessage(String key) {
        return MessagesUtil.chainWithDefault(DEFAULT_I18N_BASENAME).getWithDefault(key, key);
    }

    protected boolean isAdmin() {
        return server.isAdmin();
    }

    protected ServerConfiguration getServer() {
        return server;
    }

    protected RenderingContext getRenderingContext() {
        return renderingContext;
    }
}
