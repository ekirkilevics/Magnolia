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
package info.magnolia.module.templatingcomponents.components;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.module.templatingcomponents.AuthoringUiComponent;
import info.magnolia.objectfactory.Components;
import info.magnolia.templating.rendering.RenderException;
import info.magnolia.templating.rendering.RenderingContext;
import info.magnolia.templating.template.TemplateDefinition;
import info.magnolia.templating.template.registry.TemplateDefinitionRegistrationException;
import info.magnolia.templating.template.registry.TemplateDefinitionRegistry;

import java.io.IOException;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Common superclass for ui components, provides utility methods and defaults. Implementations should expose setter
 * methods for their specific parameters (so that template-specific wrappers can set parameters). (no need to clutter
 * things up with getters). Implementation might also expose static factory methods, which can take care of default
 * values, i.e for labels.
 *
 * @version $Id$
 */
public abstract class AbstractAuthoringUiComponent implements AuthoringUiComponent {

    public static final String EQUALS = "=";
    public static final String SPACE = " ";
    public static final String QUOTE = "\"";

    private static final String DEFAULT_I18N_BASENAME = "info.magnolia.module.templatingcomponents.messages";

    private final ServerConfiguration server;
    private final TemplateDefinitionRegistry templateDefinitionRegistry;
    private final RenderingContext renderingContext;

    protected AbstractAuthoringUiComponent(final ServerConfiguration server, final RenderingContext renderingContext) {
        this.server = server;
        this.renderingContext = renderingContext;

        this.templateDefinitionRegistry = Components.getComponent(TemplateDefinitionRegistry.class);
    }

    protected ServerConfiguration getServer() {
        return server;
    }

    @Override
    public void render(Appendable out) throws IOException, RenderException {
        if (!shouldRender()) {
            return;
        }
        doRender(out);
    }

    protected abstract void doRender(Appendable out) throws IOException, RenderException;

    /**
     * Override this method if you need to "do something" once the component is rendered, i.e cleanup the context.
     */
    @Override
    public void postRender(Appendable out) throws IOException, RenderException {
        // hook for subclasses
    }

    /**
     * Returns the "current content" from the aggregation state. Override this method if your component needs a
     * different target node.
     */
    // TODO rename to currentNode()
    protected Node currentContent() {
        final Node currentContent = renderingContext.getCurrentContent();
        if (currentContent == null) {
            throw new IllegalStateException(
                    "Could not determine currentContent from RenderingContext, currentContent is null");
        }
        return currentContent;
    }

    /**
     * Override this method if the component needs to be rendered under different conditions.
     */
    protected boolean shouldRender() {
        return server.isAdmin();
        // FIXME implement proper security
        // return (server.isAdmin() && aggregationState.getMainContent().isGranted(Permission.SET));
    }

    /**
     * The given node, if it has a mgnl:template property in it's metadata, will be used in conjunction with
     * TemplateManager and ParagraphManager to figure out the i18nBasename to use to translate this key.
     */
    protected String getMessage(Node content, String key) throws RepositoryException {
        final String i18Basename = getI18BasenameFor(content);
        return getMessage(i18Basename, key);
    }

    protected String getI18BasenameFor(Node node) {
        final String templateName = MetaDataUtil.getMetaData(node).getTemplate();
        TemplateDefinition renderable;
        try {
            renderable = templateDefinitionRegistry.getTemplateDefinition(templateName);
        } catch (TemplateDefinitionRegistrationException e) {
            // TODO dlipp: define how to react here...
            throw new RuntimeException(e);
        }
        if (renderable != null && renderable.getI18nBasename() != null) {
            return renderable.getI18nBasename();
        }
        return DEFAULT_I18N_BASENAME;
    }

    protected String getMessage(String basename, String key) {
        String s = MessagesManager.getMessages(basename).getWithDefault(key, key);
        if (key.equals(s)) {
            // fallback to our default bundle if the specific one did not contain the key - and working around
            // DefaultMessagesImpl.get()'s behaviour of adding ??? around unknown keys
            s = MessagesManager.getMessages(DEFAULT_I18N_BASENAME).getWithDefault(key, key);
        }
        return s;
    }

    /**
     * Utility method - our current gui components (magnolia-gui) expect comma separated strings.
     */
    protected String asString(List<String> strings) {
        return StringUtils.join(strings, ',');
    }

    protected void param(Appendable out, String name, String value) throws IOException {
        // TODO we need to do html attribute escaping on the value
        if (value != null) {
            out.append(SPACE).append(name).append(EQUALS).append(QUOTE).append(value).append(QUOTE);
        }
    }


    protected RenderingContext getRenderingContext() {
        return renderingContext;
    }
}
