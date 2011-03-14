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
package info.magnolia.ui.admincentral.tree.column;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.jcr.util.JCRMetadataUtil;
import info.magnolia.jcr.util.TemporaryHackUtil;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;
import info.magnolia.ui.admincentral.tree.container.JcrContainer;
import info.magnolia.ui.model.tree.definition.TemplateColumnDefinition;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.ui.Field;
import com.vaadin.ui.NativeSelect;

/**
 * A column that displays the currently selected template for a page and allows the editor to choose
 * from a list of available templates. Used in the website tree. Be aware that the value displayed
 * in the Widget is the I18NTitle - the value stored is the templates name.
 *
 * @author dlipp
 * @author tmattsson
 */
public class TemplateColumn extends TreeColumn<String,TemplateColumnDefinition> implements Serializable {

    public static final String PROPERTY_NAME = ContentRepository.NAMESPACE_PREFIX + ":template";

    private static final long serialVersionUID = -4658046121169661806L;

    public TemplateColumn(TemplateColumnDefinition def) {
        super(def);
    }

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

    public Field getEditField(Item item) {
        if (item instanceof Node) {
            Node node = (Node) item;
            NativeSelect select = new NativeSelect();
            select.setNullSelectionAllowed(false);
            select.setNewItemsAllowed(false);
            Map<String, String> availableTemplates = getAvailableTemplates(node);

            for (Map.Entry<String, String> entry : availableTemplates.entrySet()) {
                select.addItem(entry.getValue());
                select.setItemCaption(entry.getValue(), entry.getKey());
            }

            String template = JCRMetadataUtil.getMetaData(node).getTemplate();
            select.setValue(template); // TODO Doesn't render this choice as selected
            select.focus(); // TODO isn't focused in gui
            select.setImmediate(true);
            select.setInvalidAllowed(false);
            return select;
        }
        return null;
    }

    @Override
    public String getValue(Item item) throws RepositoryException {
        if (item instanceof Node) {
            Node node = (Node) item;
            String template = JCRMetadataUtil.getMetaData(node).getTemplate();
            TemplateManager templateManager = TemplateManager.getInstance();
            Template definition = templateManager.getTemplateDefinition(template);
            return (definition != null) ? definition.getI18NTitle() : "";
        }
        return "";
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public void setValue(JcrContainer jcrContainer, Item item, Object newValue) throws RepositoryException {

        if (item instanceof Node) {
            Node node = (Node) item;
            JCRMetadataUtil.getMetaData(node).setTemplate((String) newValue);
            JCRMetadataUtil.updateMetaData(node);
            node.getSession().save();
        }
    }
}
