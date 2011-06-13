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
package info.magnolia.templating.template.registry;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.DefaultContent;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.jcr.wrapper.LazyNodeWrapper;
import info.magnolia.templating.template.TemplateDefinition;

/**
 * TemplateDefinitionProvider that instantiates a dialog from a configuration node.
 *
 * @version $Id$
 */
public class ConfiguredTemplateDefinitionProvider implements TemplateDefinitionProvider {

    private static final Logger log = LoggerFactory.getLogger(ConfiguredTemplateDefinitionProvider.class);

    private final Node configNode;

    public ConfiguredTemplateDefinitionProvider(Node configNode) throws RepositoryException {
        // session that opened provided content might not be alive by the time we need to use this
        this.configNode = new LazyNodeWrapper(configNode);
    }

    @Override
    public TemplateDefinition getTemplateDefinition() throws TemplateDefinitionRegistrationException {
        // TODO make sure we are not building the object for every rendering
        Object obj = null;
        try {
            Content content = new DefaultContent(configNode, null);
            obj = Content2BeanUtil.toBean(content, true, TemplateDefinition.class);
            return (TemplateDefinition) obj;
        } catch (Content2BeanException e) {
            throw new TemplateDefinitionRegistrationException(e);
        } catch (ClassCastException e) {
            log.warn("Failed to read configured template with " + e.getMessage(), e);
            // not ideal, but don't want to introduce class dependency on compat modules
            if (obj != null && obj.getClass().getSuperclass().getName().equals("info.magnolia.module.templating.Template")) {
                log.warn("The template implements old interface and can be loaded in compatibility mode only.");
            }
            // callee knows how to deal with TDRE, but doesn't expect CCE
            throw new TemplateDefinitionRegistrationException(e);
        } catch (AccessDeniedException e) {
            throw new TemplateDefinitionRegistrationException(e);
        } catch (RepositoryException e) {
            throw new TemplateDefinitionRegistrationException(e);
        }
    }
}
