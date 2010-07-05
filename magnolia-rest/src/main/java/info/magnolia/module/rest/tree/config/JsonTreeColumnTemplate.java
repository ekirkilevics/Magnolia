/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.rest.tree.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;

import javax.jcr.RepositoryException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonTreeColumnTemplate extends JsonTreeColumn {

    private String title;

    @Override
    public String getType() {
        return "template";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Object getValue(Content storageNode) {

        TemplateColumnValue c = new TemplateColumnValue();

        String template = storageNode.getMetaData().getTemplate();

        TemplateManager templateManager = TemplateManager.getInstance();
        Template definition = templateManager.getTemplateDefinition(template);
        if (definition != null) {
            c.setTemplate(definition.getName());
            c.setTitle(definition.getI18NTitle());
        }
        c.setAvailableTemplates(getAvailableTemplates(storageNode));

        return c;
    }

    @Override
    public Object getValue(Content storageNode, NodeData nodeData) throws RepositoryException {
        return "";
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

    public static class TemplateColumnValue {

        private String template;
        private String title;
        private Map<String, String> availableTemplates;

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Map<String, String> getAvailableTemplates() {
            return availableTemplates;
        }

        public void setAvailableTemplates(Map<String, String> availableTemplates) {
            this.availableTemplates = availableTemplates;
        }
    }
}
