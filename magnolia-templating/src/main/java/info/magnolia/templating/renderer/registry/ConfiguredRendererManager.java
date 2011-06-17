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
package info.magnolia.templating.renderer.registry;

import java.util.HashSet;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.util.ModuleConfigurationObservingManager;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleRegistry;

/**
 * ObservedManager for {@link info.magnolia.templating.renderer.Renderer} configured in repository.
 *
 * @version $Id$
 */
public class ConfiguredRendererManager extends ModuleConfigurationObservingManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Set<String> registeredIds = new HashSet<String>();
    private RendererRegistry registry;

    public ConfiguredRendererManager(ModuleRegistry moduleRegistry, RendererRegistry registry) {
        super("renderers", moduleRegistry);
        this.registry = registry;
    }

    @Override
    protected void onRegister(Node node) throws RepositoryException {

        for (Node rendererNode : NodeUtil.getNodes(node, MgnlNodeType.NT_CONTENTNODE)) {

            final String id = rendererNode.getName();

            ConfiguredRendererProvider provider = null;
            try {
                provider = new ConfiguredRendererProvider(rendererNode);
            } catch (Exception e) {
                log.error("Unable to create provider for template [" + id + "]", e);
            }

            if (provider != null) {
                try {
                    synchronized (registeredIds) {
                        registry.registerRenderer(id, provider);
                        this.registeredIds.add(id);
                    }
                } catch (RendererRegistrationException e) {
                    log.error("Unable to register renderer [" + id + "]", e);
                }
            }
        }
    }

    @Override
    protected void onClear() {
        synchronized (registeredIds) {
            for (String id : registeredIds) {
                registry.unregister(id);
            }
            this.registeredIds.clear();
        }
    }
}
