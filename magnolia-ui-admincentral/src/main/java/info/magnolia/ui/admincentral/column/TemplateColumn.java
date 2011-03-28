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
package info.magnolia.ui.admincentral.column;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.ui.Component;
import info.magnolia.cms.core.Content;
import info.magnolia.jcr.util.JCRMetadataUtil;
import info.magnolia.jcr.util.TemporaryHackUtil;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.model.column.definition.TemplateColumnDefinition;

/**
 * A column that displays the currently selected template for a page and allows the editor to choose
 * from a list of available templates. Used in the website tree. Be aware that the value displayed
 * in the Widget is the I18NTitle - the value stored is the templates name.
 *
 * @author dlipp
 * @author tmattsson
 */
public class TemplateColumn extends AbstractColumn<TemplateColumnDefinition> implements Serializable {

    private static final long serialVersionUID = -4658046121169661806L;

    private EventBus eventBus;

    public TemplateColumn(TemplateColumnDefinition def, EventBus eventBus) {
        super(def);
        this.eventBus = eventBus;
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

    @Override
    public Component getComponent(Item item) throws RepositoryException {
        return new EditableSelect(item, eventBus, "MetaData/mgnl:template", getAvailableTemplates((Node) item)) {

            private static final long serialVersionUID = -8268356405124559092L;

            @Override
            protected String getValue(Item item) throws RepositoryException {
                return getInternalValue((Node)item);
            }
        };
    }

    private String getInternalValue(Node item) {
        Node node = (Node) item;
        String template = JCRMetadataUtil.getMetaData(node).getTemplate();
        TemplateManager templateManager = TemplateManager.getInstance();
        Template definition = templateManager.getTemplateDefinition(template);
        return (definition != null) ? definition.getI18NTitle() : "";
    }
}
