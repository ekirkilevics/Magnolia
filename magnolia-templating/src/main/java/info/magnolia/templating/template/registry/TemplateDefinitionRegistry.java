/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.templating.template.registry;

import info.magnolia.cms.core.ItemType;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.templating.template.TemplateDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @version $Id$
 */
public class TemplateDefinitionRegistry {

    private static final Logger log = LoggerFactory.getLogger(TemplateDefinitionRegistry.class);

    private static final String DELETED_PAGE_TEMPLATE = "mgnlDeleted";

    private final Map<String, TemplateDefinitionProvider> providers = new HashMap<String, TemplateDefinitionProvider>();

    public void registerTemplateDefinition(String id, TemplateDefinitionProvider provider) {
        synchronized (providers) {
            if (providers.containsKey(id)) {
                throw new IllegalStateException("Template definition already registered for the id [" + id + "]");
            }
            providers.put(id, provider);
        }
    }

    public void unregister(String id) {
        synchronized (providers) {
            providers.remove(id);
        }
    }

    public TemplateDefinition getTemplateDefinition(String id) throws TemplateDefinitionRegistrationException {

        TemplateDefinitionProvider templateDefinitionProvider;
        synchronized (providers) {
            templateDefinitionProvider = providers.get(id);
        }
        if (templateDefinitionProvider == null) {
            throw new TemplateDefinitionRegistrationException("No TemplateDefinition registered for id: " + id);
        }
        return templateDefinitionProvider.getTemplateDefinition();
    }

    // TODO move this to an independent template availability component
    public Iterator<TemplateDefinition> getAvailableTemplates(Node content) {
        List<TemplateDefinition> templateList = new ArrayList<TemplateDefinition>();

        try {
            if (content != null && NodeUtil.hasMixin(content, ItemType.DELETED_NODE_MIXIN)) {
                templateList.add(getTemplateDefinition(DELETED_PAGE_TEMPLATE));
                return templateList.iterator();
            }
        } catch (RepositoryException e) {
            log.error("Failed to check node for deletion status.", e);
        } catch (TemplateDefinitionRegistrationException e) {
            log.error("Deleted content template is not correctly registered.", e);
        }
        for (TemplateDefinitionProvider provider : providers.values()) {
            try {
                TemplateDefinition definition = provider.getTemplateDefinition();
                if (definition.isAvailable(content)) {
                    templateList.add(definition);
                }
            } catch (TemplateDefinitionRegistrationException e) {
                // one failing provider is no reason to not show any templates
                log.error("Failed to read template definition from " + provider + ".", e);
            }
        }
        return templateList.iterator();
    }

    /**
     * Get the Template that could be used for the provided Content as a default.
     */
    // TODO move this to an independent template availability component
    public TemplateDefinition getDefaultTemplate(Node content) {
        TemplateDefinition tmpl;
            try {
                // try to use the same as the parent
                tmpl = this.getTemplateDefinition(MetaDataUtil.getTemplate(content));
                if(tmpl != null && tmpl.isAvailable(content)){
                    return tmpl;
                }
                // otherwise use the first available template
                else{
                    Iterator<TemplateDefinition> templates = getAvailableTemplates(content);
                    if (templates.hasNext()) {
                        return templates.next();
                    }
                }
            }
            catch (TemplateDefinitionRegistrationException e) {
                log.error("Can't resolve default template for node " + content, e);
            }
            return null;
    }

}
