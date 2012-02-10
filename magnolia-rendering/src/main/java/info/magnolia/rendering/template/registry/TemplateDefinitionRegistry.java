/**
 * This file Copyright (c) 2011 Magnolia International
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

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistryMap;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The central registry of all {@link TemplateDefinition}s.
 *
 * @version $Id$
 */
@Singleton
public class TemplateDefinitionRegistry {

    private static final Logger log = LoggerFactory.getLogger(TemplateDefinitionRegistry.class);
    //FIXME this probably should not be hardcoded.
    private static final String DELETED_PAGE_TEMPLATE = "adminInterface:mgnlDeleted";

    private final RegistryMap<String, TemplateDefinitionProvider> registry = new RegistryMap<String, TemplateDefinitionProvider>() {

        @Override
        protected String keyFromValue(TemplateDefinitionProvider provider) {
            return provider.getId();
        }
    };
    private TemplateAvailability templateAvailability;

    @Inject
    public TemplateDefinitionRegistry(TemplateAvailability templateAvailability) {
        this.templateAvailability = templateAvailability;
    }

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

    public Collection<TemplateDefinition> getAvailableTemplates(Node content) {

        try {
            if (content != null && NodeUtil.hasMixin(content, MgnlNodeType.MIX_DELETED)) {
                return Collections.singleton(getTemplateDefinition(DELETED_PAGE_TEMPLATE));
            }
        } catch (RepositoryException e) {
            log.error("Failed to check node for deletion status.", e);
        } catch (RegistrationException e) {
            log.error("Deleted content template is not correctly registered.", e);
        }

        final ArrayList<TemplateDefinition> availableTemplateDefinitions = new ArrayList<TemplateDefinition>();
        final Collection<TemplateDefinition> templateDefinitions = getTemplateDefinitions();
        for (TemplateDefinition templateDefinition : templateDefinitions) {
            if (isAvailable(templateDefinition, content)) {
                availableTemplateDefinitions.add(templateDefinition);
            }
        }
        return availableTemplateDefinitions;
    }

    protected boolean isAvailable(TemplateDefinition templateDefinition, Node content) {
        return isVisible(templateDefinition) &&
                isPageTemplate(templateDefinition, content) &&
                templateAvailability.isAvailable(content, templateDefinition);

    }

    protected boolean isPageTemplate(TemplateDefinition templateDefinition, Node content) {
        // TODO temporary fix for limiting only website to <moduleName>:pages/*
        try {
            return content.getSession().getWorkspace().getName().equals("website") &&
                    StringUtils.substringAfter(templateDefinition.getId(), ":").startsWith("pages/");
        } catch (RepositoryException e) {
            return false;
        }
    }

    protected boolean isVisible(TemplateDefinition templateDefinition) {
        return templateDefinition.getVisible() == null || templateDefinition.getVisible();
    }

    /**
     * Get the Template that could be used for the provided content as a default.
     */
    public TemplateDefinition getDefaultTemplate(Node content) {

        // try to use the same as the parent
        TemplateDefinition parentTemplate = null;
        try {
            parentTemplate = getTemplateDefinition(content.getParent());
        } catch (RepositoryException e) {
            log.error("Failed to determine template assigned to parent of node: " + NodeUtil.getNodePathIfPossible(content), e);
        }

        if (parentTemplate != null && templateAvailability.isAvailable(content, parentTemplate)) {
            return parentTemplate;
        }

        // otherwise use the first available template
        Collection<TemplateDefinition> templates = getAvailableTemplates(content);
        if (templates.isEmpty()) {
            return null;
        }

        return templates.iterator().next();
    }

    private TemplateDefinition getTemplateDefinition(Node node) throws RepositoryException {
        String templateId = MetaDataUtil.getTemplate(node);
        if (StringUtils.isEmpty(templateId)) {
            return null;
        }
        try {
            // TODO Ioc
            TemplateDefinitionAssignment templateDefinitionAssignment = Components.getComponent(TemplateDefinitionAssignment.class);
            return templateDefinitionAssignment.getAssignedTemplateDefinition(node);
        } catch (RegistrationException e) {
            return null;
        }
    }
}
