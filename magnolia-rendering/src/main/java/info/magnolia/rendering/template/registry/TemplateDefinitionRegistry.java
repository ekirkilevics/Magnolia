/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.rendering.template.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.registry.RegistrationException;
import info.magnolia.registry.RegistryMap;
import info.magnolia.rendering.template.TemplateDefinition;


/**
 * The central registry of all {@link TemplateDefinition}s.
 *
 * @version $Id$
 */
@Singleton
public class TemplateDefinitionRegistry {

    private static final Logger log = LoggerFactory.getLogger(TemplateDefinitionRegistry.class);

    private final RegistryMap<String, TemplateDefinitionProvider> registry = new RegistryMap<String, TemplateDefinitionProvider>() {

        @Override
        protected String keyFromValue(TemplateDefinitionProvider provider) {
            return provider.getId();
        }
    };

    public TemplateDefinition getTemplateDefinition(String id) throws RegistrationException {

        TemplateDefinitionProvider templateDefinitionProvider;
        try {
            templateDefinitionProvider = registry.getRequired(id);
        } catch (RegistrationException e) {
            throw new RegistrationException("No template definition registered for id: " + id, e);
        }

        return templateDefinitionProvider.getTemplateDefinition();
    }

    /**
     * @return all TemplateDefinitions - in case of errors it'll just deliver the ones that are properly registered and logs error's for the others.
     */
    public Collection<TemplateDefinition> getTemplateDefinitions() {
        final Collection<TemplateDefinition> templateDefinitions = new ArrayList<TemplateDefinition>();
        for (TemplateDefinitionProvider provider : registry.values()) {
            try {
                final TemplateDefinition templateDefinition = provider.getTemplateDefinition();
                if (templateDefinition == null) {
                    log.error("Provider's TemplateDefinition is null: " + provider);
                } else {
                    templateDefinitions.add(templateDefinition);
                }
            } catch (RegistrationException e) {
                log.error("Failed to read template definition from " + provider + ".", e);
            }
        }
        return templateDefinitions;
    }

    public void register(TemplateDefinitionProvider provider) {
        registry.put(provider);
    }

    public void unregister(String id) {
        registry.remove(id);
    }

    public Set<String> unregisterAndRegister(Collection<String> registeredIds, Collection<TemplateDefinitionProvider> providers) {
        return registry.removeAndPutAll(registeredIds, providers);
    }
}
