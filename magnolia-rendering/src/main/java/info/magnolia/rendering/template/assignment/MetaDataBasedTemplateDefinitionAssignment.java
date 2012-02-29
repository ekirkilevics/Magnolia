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
package info.magnolia.rendering.template.assignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.PermissionUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateAvailability;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;


/**
 * Uses the template id stored in the node's meta data.
 *
 * @version $Id$
 */
@Singleton
public class MetaDataBasedTemplateDefinitionAssignment implements TemplateDefinitionAssignment {

    private static final String DELETED_PAGE_TEMPLATE = "adminInterface:mgnlDeleted";

    private static final Logger log = LoggerFactory.getLogger(MetaDataBasedTemplateDefinitionAssignment.class);

    private TemplateDefinitionRegistry templateDefinitionRegistry;

    @Inject
    public MetaDataBasedTemplateDefinitionAssignment(TemplateDefinitionRegistry templateDefinitionRegistry) {
        this.templateDefinitionRegistry = templateDefinitionRegistry;
    }

    @Override
    public TemplateDefinition getAssignedTemplateDefinition(Node content) throws RegistrationException {
        final String templateId = MetaDataUtil.getMetaData(content).getTemplate();
        return templateDefinitionRegistry.getTemplateDefinition(templateId);
    }

    /**
     * Get the Template that could be used for the provided content as a default.
     */
    @Override
    public TemplateDefinition getDefaultTemplate(Node content) {

        // try to use the same as the parent
        TemplateDefinition parentTemplate = null;
        try {
            parentTemplate = getAssignedTemplateDefinition(content.getParent());
        } catch (RepositoryException e) {
            log.error("Failed to determine template assigned to parent of node: " + NodeUtil.getNodePathIfPossible(content), e);
        } catch (RegistrationException e) {
            // No template assigned or the assigned template is missing
        }

        if (parentTemplate != null && isAvailable(content, parentTemplate)) {
            return parentTemplate;
        }

        // otherwise use the first available template
        Collection<TemplateDefinition> templates = getAvailableTemplates(content);
        if (templates.isEmpty()) {
            return null;
        }

        return templates.iterator().next();
    }

    @Override
    public Collection<TemplateDefinition> getAvailableTemplates(Node content) {

        try {
            if (content != null && NodeUtil.hasMixin(content, MgnlNodeType.MIX_DELETED)) {
                return Collections.singleton(templateDefinitionRegistry.getTemplateDefinition(DELETED_PAGE_TEMPLATE));
            }
        } catch (RepositoryException e) {
            log.error("Failed to check node for deletion status.", e);
        } catch (RegistrationException e) {
            log.error("Deleted content template is not correctly registered.", e);
        }

        final ArrayList<TemplateDefinition> availableTemplateDefinitions = new ArrayList<TemplateDefinition>();
        final Collection<TemplateDefinition> templateDefinitions = templateDefinitionRegistry.getTemplateDefinitions();
        for (TemplateDefinition templateDefinition : templateDefinitions) {
            if (isTemplateAvailable(content, templateDefinition)) {
                availableTemplateDefinitions.add(templateDefinition);
            }
        }

        Collections.sort(availableTemplateDefinitions, new Comparator<TemplateDefinition>() {

            @Override
            public int compare(TemplateDefinition lhs, TemplateDefinition rhs) {
                return getI18nTitle(lhs).compareTo(getI18nTitle(rhs));
            }

            private String getI18nTitle(TemplateDefinition templateDefinition) {
                Messages messages = MessagesManager.getMessages(templateDefinition.getI18nBasename());
                return messages.getWithDefault(templateDefinition.getTitle(), templateDefinition.getTitle());
            }
        });

        return availableTemplateDefinitions;
    }

    protected boolean isTemplateAvailable(Node content, TemplateDefinition templateDefinition) {
        return hasReadAccess(content) &&
                isVisible(templateDefinition) &&
                isPageTemplate(content, templateDefinition) &&
                isAvailable(content, templateDefinition);
    }

    protected boolean isPageTemplate(Node content, TemplateDefinition templateDefinition) {
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

    protected boolean isAvailable(Node content, TemplateDefinition templateDefinition) {
        TemplateAvailability templateAvailability = templateDefinition.getTemplateAvailability();
        return templateAvailability == null || templateAvailability.isAvailable(content, templateDefinition);
    }

    protected boolean hasReadAccess(Node content) {
        try {
            // should not fact that we are able to get path already show that we can read this node???
            // ... unless of course this "content" was created with system session ... so make sure we check using user session and not the node session
            return PermissionUtil.isGranted(
                    MgnlContext.getJCRSession(content.getSession().getWorkspace().getName()),
                    content.getPath(),
                    Session.ACTION_READ);
        } catch (RepositoryException e) {
            return false;
        }
    }
}
