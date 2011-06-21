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
package info.magnolia.rendering.renderer.registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.util.ModuleConfigurationObservingManager;
import info.magnolia.jcr.util.NodeTypeFilter;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.module.ModuleRegistry;

/**
 * ObservedManager for {@link info.magnolia.rendering.renderer.Renderer} configured in repository.
 *
 * @version $Id$
 */
public class ConfiguredRendererManager extends ModuleConfigurationObservingManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private Set<String> registeredIds = new HashSet<String>();
    private RendererRegistry registry;

    public ConfiguredRendererManager(ModuleRegistry moduleRegistry, RendererRegistry registry) {
        super("renderers", moduleRegistry);
        this.registry = registry;
    }

    @Override
    protected void reload(List<Node> nodes) throws RepositoryException {

        final List<RendererProvider> providers = new ArrayList<RendererProvider>();

        for (Node node : nodes) {

            NodeUtil.visit(node, new NodeVisitor() {

                @Override
                public void visit(Node node) throws RepositoryException {
                    for (Node configNode : NodeUtil.getNodes(node, MgnlNodeType.NT_CONTENTNODE)) {
                        RendererProvider provider = readProvider(configNode);
                        if (provider != null) {
                            providers.add(provider);
                        }
                    }
                }
            }, new NodeTypeFilter(MgnlNodeType.NT_CONTENT));
        }

        this.registeredIds = registry.removeAndRegister(registeredIds, providers);
    }

    protected RendererProvider readProvider(Node rendererNode) throws RepositoryException {

        final String id = createId(rendererNode);

        try {
            return new ConfiguredRendererProvider(id, rendererNode);
        } catch (Exception e) {
            log.error("Unable to create provider renderer [" + id + "]", e);
            return null;
        }
    }

    private String createId(Node rendererNode) throws RepositoryException {
        return rendererNode.getName();
    }
}
