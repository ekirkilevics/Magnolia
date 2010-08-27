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
package info.magnolia.module.admincentral.tree;

import com.vaadin.ui.Field;
import com.vaadin.ui.Select;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A column that displays the currently selected template for a node and allows the editor to choose from a list of
 * available templates. Used in the website tree.
 */
public class TemplateColumn extends TreeColumn {

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object getValue(Content content) {

        String template = content.getMetaData().getTemplate();

        TemplateManager templateManager = TemplateManager.getInstance();
        Template definition = templateManager.getTemplateDefinition(template);
        if (definition != null) {
            return definition.getI18NTitle();
        }

        return "";
    }

    @Override
    public Object getValue(Content content, NodeData nodeData) {
        return "";
    }

    public Field getEditField(Content content) {

        // TODO the actual item isn't selected, dont know why yet, also the cell gets filled in with the template name, not its label

        Select select = new Select();
        select.setNullSelectionAllowed(false);
        select.setNewItemsAllowed(false);
        Map<String, String> availableTemplates = getAvailableTemplates(content);
        for (Map.Entry<String, String> entry : availableTemplates.entrySet()) {
            select.addItem(entry.getKey());
            select.setItemCaption(entry.getKey(), entry.getValue());
        }
        String template = content.getMetaData().getTemplate();
        select.setValue(template);
        return select;
    }

    private Map<String, String> getAvailableTemplates(Content content) {
        TemplateManager templateManager = TemplateManager.getInstance();
        Iterator<Template> templates = templateManager.getAvailableTemplates(content);
        Map<String, String> map = new LinkedHashMap<String, String>();
        while (templates.hasNext()) {
            Template template = templates.next();
            map.put(template.getName(), template.getI18NTitle());
        }
        return map;
    }
}
