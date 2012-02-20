/**
 * This file Copyright (c) 2011 Magnolia International
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

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.jcr.inheritance.InheritanceNodeWrapper;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.AppendableOnlyOutputProvider;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.templating.freemarker.AreaDirective;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;

/**
 * Renders a piece of content.
 *
 * @version $Id$
 */
public class ComponentElement extends AbstractContentTemplatingElement {

    private Map<String, Object> contextAttributes = new HashMap<String, Object>();
    private final RenderingEngine renderingEngine;
    private Node content;
    private final TemplateDefinitionAssignment templateDefinitionAssignment;

    private String dialog;

    @Inject
    public ComponentElement(ServerConfiguration server, RenderingContext renderingContext, RenderingEngine renderingEngine, TemplateDefinitionAssignment templateDefinitionAssignment ) {
        super(server, renderingContext);
        this.renderingEngine = renderingEngine;
        this.templateDefinitionAssignment = templateDefinitionAssignment;
    }

    @Override
    public void begin(Appendable out) throws IOException, RenderException {

        TemplateDefinition componentDefinition = null;
        content = getPassedContent();

        if(content == null) {
            throw new RenderException("The 'content' or 'workspace' and 'path' attribute have to be set to rendre a component.");
        }

        if(isAdmin()){
            MarkupHelper helper = new MarkupHelper(out);

            helper.openComment("cms:component");


            helper.attribute(AreaDirective.CONTENT_ATTRIBUTE, getNodePath(content));

            if(content instanceof InheritanceNodeWrapper) {
                if (((InheritanceNodeWrapper) content).isInherited()) {
                    helper.attribute("inherited", "true");
                }
            }

            try {
                componentDefinition = templateDefinitionAssignment.getAssignedTemplateDefinition(content);
            } catch (RegistrationException e) {
                throw new RenderException("No template definition found for the current content", e);
            }

            final Messages messages = MessagesManager.getMessages(componentDefinition.getI18nBasename());

            if(StringUtils.isEmpty(dialog)) {
                dialog = resolveDialog(componentDefinition);
            }
            helper.attribute("dialog", dialog);

            String label = StringUtils.defaultIfEmpty(componentDefinition.getTitle(),componentDefinition.getName());
            helper.attribute("label", messages.getWithDefault(label, label));

            if(StringUtils.isNotEmpty(componentDefinition.getDescription())){
                helper.attribute("description", componentDefinition.getDescription());
            }
            helper.append(" -->\n");
        }

        // TODO not sure how to pass editable

        WebContext webContext = MgnlContext.getWebContext();
        webContext.push(webContext.getRequest(), webContext.getResponse());
        setAttributesInWebContext(contextAttributes, WebContext.LOCAL_SCOPE);


        try {
            if(componentDefinition != null) {
                renderingEngine.render(content, componentDefinition, new HashMap<String, Object>(), new AppendableOnlyOutputProvider(out));
            } else {
                renderingEngine.render(content, new AppendableOnlyOutputProvider(out));
            }
        } finally {
            webContext.pop();
            webContext.setPageContext(null);
            restoreAttributesInWebContext(contextAttributes, WebContext.LOCAL_SCOPE);
        }
    }

    @Override
    public void end(Appendable out) throws IOException, RenderException {
        if(isAdmin()){
            new MarkupHelper(out).closeComment("cms:component");
        }
    }

    public Map<String, Object> getContextAttributes() {
        return contextAttributes;
    }

    public void setContextAttributes(Map<String, Object> contextAttributes) {
        this.contextAttributes = contextAttributes;
    }

    private String resolveDialog(TemplateDefinition component) {
        if (StringUtils.isNotEmpty(this.dialog)) {
            return this.dialog;
        }
        String dialog = component.getDialog();
        if (StringUtils.isNotEmpty(dialog)) {
            return dialog;
        }
        return null;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }
}
