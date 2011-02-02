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
package info.magnolia.module.admincentral.tree;

import com.vaadin.ui.Field;
import com.vaadin.ui.Select;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.module.admincentral.jcr.JCRMetadataUtil;
import info.magnolia.module.admincentral.jcr.TemporaryHackUtil;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A column that displays the currently selected template for a node and allows the editor to choose
 * from a list of available templates. Used in the website tree. Be aware that the value displayed
 * in the Widget is the I18NTitle - the value stored is the templates name.
 */
public class TemplateColumn extends TreeColumn<String> implements Serializable {

    public static final String PROPERTY_NAME = ContentRepository.NAMESPACE_PREFIX + ":template";

    private static final long serialVersionUID = -4658046121169661806L;

    private Map<String, String> getAvailableTemplates(Node node) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        TemplateManager templateManager = TemplateManager.getInstance();

        // Temp: as long as TemplateManager cannot operate on pure JCR
        Content content = TemporaryHackUtil.createHackContentFrom(node);

        Iterator<Template> templates = templateManager.getAvailableTemplates(content);
        while (templates.hasNext()) {
            Template template = templates.next();
            map.put(template.getI18NTitle(), template.getName());
        }
        return map;
    }

    public Field getEditField(Node node) {
        Select select = new Select();
        select.setNullSelectionAllowed(false);
        select.setNewItemsAllowed(false);
        Map<String, String> availableTemplates = getAvailableTemplates(node);

        for (String key : availableTemplates.keySet()) {
            select.addItem(key);
            select.setItemCaption(key, availableTemplates.get(key));
        }

//        try {
//            Property template = JCRMetadataUtil.getMetaDataProperty(node, JCRMetadataUtil.TEMPLATE);
//            select.setValue(template.getString());
//        }
//        catch (RepositoryException e) {
//            select = null;
//        }
       return select;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public Object getValue(Node node) throws RepositoryException {
        Property template = JCRMetadataUtil.getMetaDataProperty(node, PROPERTY_NAME);
        TemplateManager templateManager = TemplateManager.getInstance();
        Template definition = templateManager.getTemplateDefinition(template.getString());
        return (definition != null) ? definition.getI18NTitle() : "";
    }

    @Override
    public void setValue(Node node, Object newValue) throws RepositoryException {
        Property prop = JCRMetadataUtil.getMetaDataProperty(node, PROPERTY_NAME);
        Map<String, String> availableTemplates = getAvailableTemplates(node);
        String templateName = availableTemplates.get(newValue);
        prop.setValue(templateName);
    }
}
