/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.module.admininterface.trees;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;

import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pbracher
 * @version $Id$
 *
 */
public class TemplateColumn extends TreeColumn {
    private static final Logger log = LoggerFactory.getLogger(TemplateColumn.class);

    private TemplateDefinitionRegistry templateRegistry;
    private TemplateDefinitionAssignment templateAssignment;
    Select templateSelect;

    public TemplateColumn(String javascriptTree, HttpServletRequest request) {
        super(javascriptTree, request);
        // TODO dlipp: use IoC here.
        this.templateRegistry = Components.getComponent(TemplateDefinitionRegistry.class);
        this.templateAssignment = Components.getComponent(TemplateDefinitionAssignment.class);

        templateSelect = new Select();
        templateSelect.setName(javascriptTree + TreeColumn.EDIT_NAMEADDITION);
        templateSelect.setSaveInfo(false);
        templateSelect.setCssClass(TreeColumn.EDIT_CSSCLASS_SELECT);

        // we must pass the displayValue to this function
        // templateSelect.setEvent("onblur", tree.getJavascriptTree() + TreeColumn.EDIT_JSSAVE);
        // templateSelect.setEvent("onchange", tree.getJavascriptTree() + TreeColumn.EDIT_JSSAVE);
        templateSelect.setEvent("onblur", javascriptTree //$NON-NLS-1$
            + ".saveNodeData(this.value,this.options[this.selectedIndex].text)"); //$NON-NLS-1$
        templateSelect.setEvent("onchange", javascriptTree //$NON-NLS-1$
            + ".saveNodeData(this.value,this.options[this.selectedIndex].text)"); //$NON-NLS-1$

    }

    @Override
    public String getHtml() {
        Content content = this.getWebsiteNode();
        try {
            if(MgnlNodeType.NT_FOLDER.equals(content.getNodeTypeName())) {
                return "";
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException("Could not retrieve node type name for content " + content, e);
        }
        String templateName = content.getMetaData().getTemplate();

        TemplateDefinition template = null;
        try {
            template = templateRegistry.getTemplateDefinition(templateName);
        }
        catch (RegistrationException e) {
            log.error("Template with id {} doesn't exist.", templateName);
        }
        return template != null ? getI18nTitle(template) : "Missing: " + StringUtils.defaultString(templateName);

    }

    @Override
    public String getHtmlEdit() {
        Collection<TemplateDefinition> templateDefinitons = templateAssignment.getAvailableTemplates(this.getWebsiteNode().getJCRNode());

        templateSelect.getOptions().clear();

        for (TemplateDefinition templateDefinition : templateDefinitons) {
            String title = MessagesUtil.javaScriptString(getI18nTitle(templateDefinition));
            templateSelect.setOptions(title, templateDefinition.getId());
        }
        return templateSelect.getHtml();
    }

    private String getI18nTitle(TemplateDefinition template) {
        Messages messages = MessagesManager.getMessages(template.getI18nBasename());
        String title = template.getTitle();
        return messages.getWithDefault(title, title);
    }
}
