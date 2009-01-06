/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.delta;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;

/**
 * An abstract that will perform an operation on all modules node found in the configuration repository.
 * @see #operateOnChildNode(info.magnolia.cms.core.Content,info.magnolia.module.InstallContext)
 *
 * Warning: tasks will fail if the modules node does not exist yet. (As an incentive to force the explicit creation
 * of this node) 
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AllModulesNodeOperation extends AllChildrenNodesOperation {

    public AllModulesNodeOperation(String name, String description) {
        super(name, description, null, null);
    }

    protected HierarchyManager getHierarchyManager(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        return ctx.getConfigHierarchyManager();
    }

    protected Content getParentNode(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        if (!ctx.hasModulesNode()) {
            throw new TaskExecutionException("The main /modules node does not exist in the config repository, can not proceed.");
        }
        return ctx.getModulesNode();
    }

    protected void operateOnChildNode(Content node, InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getConfigHierarchyManager();
        operateOnModuleNode(node, hm, ctx);
    }

    protected abstract void operateOnModuleNode(Content node, HierarchyManager hm, InstallContext ctx) throws RepositoryException, TaskExecutionException;
}
