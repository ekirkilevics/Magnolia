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
package info.magnolia.ui.model.workbench.registry;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.util.ModuleConfigurationObservingManager;
import info.magnolia.jcr.util.NodeTypeFilter;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.module.ModuleRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ObservedManager for trees configured in the repository.
 */
@Singleton
public class ConfiguredWorkbenchDefinitionManager extends ModuleConfigurationObservingManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private Set<String> registeredIds = new HashSet<String>();
    private final WorkbenchDefinitionRegistry registry;

    @Inject
    public ConfiguredWorkbenchDefinitionManager(ModuleRegistry moduleRegistry, WorkbenchDefinitionRegistry workbenchRegistry) {
        super("workbenches", moduleRegistry);
        this.registry = workbenchRegistry;
    }

    @Override
    protected void reload(List<Node> nodes) throws RepositoryException {

        final List<WorkbenchDefinitionProvider> providers = new ArrayList<WorkbenchDefinitionProvider>();

        for (Node node : nodes) {

            NodeUtil.visit(node, new NodeVisitor() {

                @Override
                public void visit(Node current) throws RepositoryException {
                    for (Node child : NodeUtil.getNodes(current, MgnlNodeType.NT_CONTENTNODE)) {
                        WorkbenchDefinitionProvider provider = readProvider(child);
                        if (provider != null) {
                            providers.add(provider);
                        }
                    }
                }
            }, new NodeTypeFilter(MgnlNodeType.NT_CONTENT));
        }

        this.registeredIds = registry.unregisterAndRegister(registeredIds, providers);
    }

    protected WorkbenchDefinitionProvider readProvider(Node workbenchNode) throws RepositoryException {

        final String id = createId(workbenchNode);

        try {
            return new ConfiguredWorkbenchDefinitionProvider(id, workbenchNode);
        } catch (Exception e) {
            log.error("Unable to create provider for workbench [" + id + "]", e);
            return null;
        }
    }

    private String createId(Node configNode) throws RepositoryException {
        return configNode.getName();
    }
}
