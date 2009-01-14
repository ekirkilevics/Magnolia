/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.i18n.MessagesUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

/**
 * @author pbracher
 * @version $Id$
 *
 */
public class TemplateColumn extends TreeColumn {
    Select templateSelect;

    public TemplateColumn(String javascriptTree, HttpServletRequest request) {
        super(javascriptTree, request);

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

    public String getHtml() {
        Content content = this.getWebsiteNode();
        String templateName = content.getMetaData().getTemplate();
        Template template = TemplateManager.getInstance().getInfo(templateName);
        return template != null ? template.getI18NTitle() : StringUtils.defaultString(templateName);

    }

    public String getHtmlEdit() {
        Iterator templates = TemplateManager.getInstance().getAvailableTemplates(this.getWebsiteNode());

        templateSelect.getOptions().clear();

        while (templates.hasNext()) {
            Template template = (Template) templates.next();
            String title = MessagesUtil.javaScriptString(template.getI18NTitle());
            templateSelect.setOptions(title, template.getName());
        }
        return templateSelect.getHtml();
    }
}