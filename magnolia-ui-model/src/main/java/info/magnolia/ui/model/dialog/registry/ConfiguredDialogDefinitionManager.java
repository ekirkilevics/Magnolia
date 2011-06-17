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
package info.magnolia.ui.model.dialog.registry;

import java.util.HashSet;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.util.ModuleConfigurationObservingManager;
import info.magnolia.jcr.util.NodeTypeFilter;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.module.ModuleRegistry;


/**
 * ObservedManager for dialogs configured in repository.
 *
 * @version $Id$
 */
public class ConfiguredDialogDefinitionManager extends ModuleConfigurationObservingManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Set<String> registeredDialogs = new HashSet<String>();
    private final DialogDefinitionRegistry dialogDefinitionRegistry;

    public ConfiguredDialogDefinitionManager(ModuleRegistry moduleRegistry, DialogDefinitionRegistry dialogDefinitionRegistry) {
        super("dialogs", moduleRegistry);
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
    }

    @Override
    protected void onRegister(Node node) {

        try {
            NodeUtil.visit(node, new NodeVisitor() {

                @Override
                public void visit(Node node) throws RepositoryException {
                    for (Node dialogNode : NodeUtil.getNodes(node, MgnlNodeType.NT_CONTENTNODE)) {
                        registerDialog(dialogNode);
                    }
                }
            }, new NodeTypeFilter(MgnlNodeType.NT_CONTENT));
        } catch (Exception e) {
            throw new RuntimeException("Can't register dialog defined at " + node, e);
        }
    }

    private void registerDialog(Node dialogNode) throws RepositoryException {

        final String id = createId(dialogNode);

        ConfiguredDialogDefinitionProvider dialogProvider = null;
        try {
            dialogProvider = new ConfiguredDialogDefinitionProvider(id, dialogNode);
        } catch (Exception e) {
            log.error("Unable to create provider for dialog [" + id + "]", e);
        }

        if (dialogProvider != null) {
            try {
                synchronized (registeredDialogs) {
                    dialogDefinitionRegistry.registerDialog(dialogProvider);
                    this.registeredDialogs.add(id);
                }
            } catch (DialogDefinitionRegistrationException e) {
                log.error("Unable to register dialog [" + id + "]", e);
            }
        }
    }

    protected String createId(Node templateDefinitionNode) throws RepositoryException {
        final String path = templateDefinitionNode.getPath();
        final String[] pathElements = path.split("/");
        final String moduleName = pathElements[2];
        return moduleName + ":" + StringUtils.removeStart(path, "/modules/" + moduleName + "/dialogs/");
    }

    @Override
    protected void onClear() {
        synchronized (registeredDialogs) {
            for (String dialogName : registeredDialogs) {
                dialogDefinitionRegistry.unregisterDialog(dialogName);
            }
            this.registeredDialogs.clear();
        }
    }
}
