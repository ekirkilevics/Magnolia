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
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renders a piece of content.
 *
 * @version $Id$
 */
public class ComponentElement extends AbstractContentTemplatingElement {

    private static final Logger log = LoggerFactory.getLogger(ComponentElement.class);

    private Map<String, Object> contextAttributes = new HashMap<String, Object>();
    private final RenderingEngine renderingEngine;
    private Node content;
    private final TemplateDefinitionAssignment templateDefinitionAssignment;
    private TemplateDefinition componentDefinition;

    private String dialog;
    private Boolean editable;

    @Inject
    public ComponentElement(ServerConfiguration server, RenderingContext renderingContext, RenderingEngine renderingEngine, TemplateDefinitionAssignment templateDefinitionAssignment ) {
        super(server, renderingContext);
        this.renderingEngine = renderingEngine;
        this.templateDefinitionAssignment = templateDefinitionAssignment;
    }

    @Override
    public void begin(Appendable out) throws IOException, RenderException {

        content = getPassedContent();

        if(content == null) {
            throw new RenderException("The 'content' or 'workspace' and 'path' attribute have to be set to render a component.");
        }

        if(isAdmin() && hasPermission(content)){
            MarkupHelper helper = new MarkupHelper(out);

            helper.openComment("cms:component");


            helper.attribute(AreaDirective.CONTENT_ATTRIBUTE, getNodePath(content));

            if(content instanceof InheritanceNodeWrapper) {
                if (((InheritanceNodeWrapper) content).isInherited()) {
                    helper.attribute("inherited", "true");
                }
            }
            try {
                this.componentDefinition = templateDefinitionAssignment.getAssignedTemplateDefinition(content);
            } catch (RegistrationException e) {
                throw new RenderException("No template definition found for the current content", e);
            }

            final Messages messages = MessagesManager.getMessages(componentDefinition.getI18nBasename());

            this.editable = resolveEditable();
            if (this.editable != null) {
                helper.attribute("editable", String.valueOf(this.editable));
            }

            if(StringUtils.isEmpty(dialog)) {
                dialog = resolveDialog();
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

    private boolean hasPermission(Node node) {
        try {
            return node.getSession().hasPermission(node.getPath(), Session.ACTION_SET_PROPERTY);
        } catch (RepositoryException e) {
            log.error("Could not determine permission for node {}", node);
        }
        return false;
    }

    private Boolean resolveEditable() {
        return editable != null ? editable : componentDefinition != null && componentDefinition.getEditable() != null ? componentDefinition.getEditable() : null;
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

    private String resolveDialog() {
        if (StringUtils.isNotEmpty(this.dialog)) {
            return this.dialog;
        }
        String dialog = componentDefinition.getDialog();
        if (StringUtils.isNotEmpty(dialog)) {
            return dialog;
        }
        return null;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public Boolean getEditable() {
        return editable;
    }
}
