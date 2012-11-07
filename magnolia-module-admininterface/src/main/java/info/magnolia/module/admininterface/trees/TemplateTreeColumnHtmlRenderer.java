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
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.gui.control.TreeColumnHtmlRenderer;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;


/**
 * @author philipp
 */
public class TemplateTreeColumnHtmlRenderer implements TreeColumnHtmlRenderer {
    private final TemplateDefinitionRegistry templateManager;

    public TemplateTreeColumnHtmlRenderer() {
        templateManager = Components.getComponent(TemplateDefinitionRegistry.class);
    }

    /**
     * @see info.magnolia.cms.gui.control.TreeColumnHtmlRenderer#renderHtml(TreeColumn, Content)
     */
    @Override
    public String renderHtml(TreeColumn treeColumn, Content content) {
        String templateName = content.getMetaData().getTemplate();
        TemplateDefinition template;
        try {
            template = templateManager.getTemplateDefinition(templateName);
        }
        catch (RegistrationException e) {
            throw new UnhandledException(e);
        }
        return template != null ? template.getTitle() : StringUtils.defaultString(templateName);
    }

}
