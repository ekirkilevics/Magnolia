/**
 * This file Copyright (c) 2007-2011 Magnolia International
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
package info.magnolia.setup.for3_5;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;


/**
 * Updates format of URI2Repository mappings to format used since 3.5.
 * @author vsteller
 * @version $Id$
 *
 */
public class UpdateURI2RepositoryMappings extends info.magnolia.module.delta.AllChildrenNodesOperation {

    private static final String SERVER_URI2REPOSITORY_MAPPING = "/server/URI2RepositoryMapping";
    private Content mappingsNode;
    private final ArrayDelegateTask subTasks;

    public UpdateURI2RepositoryMappings() {
        super("Update URI2Repository mappings", "The structure of the URI2Repository mappings have changed in 3.5", ContentRepository.CONFIG, SERVER_URI2REPOSITORY_MAPPING);
        subTasks = new ArrayDelegateTask("Move URI2Repository mapping nodes");
    }

    /**
     * Creates the mappings node and delegates to the super {@link #doExecute(InstallContext)} method.
     */
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager configHM = ctx.getConfigHierarchyManager();
        final Content parentNode = getParentNode(ctx);

        super.doExecute(ctx);

        mappingsNode = parentNode.createContent("mappings");
        NodeDataUtil.getOrCreate(parentNode, "class").setValue(info.magnolia.cms.beans.config.URI2RepositoryManager.class.getName());

        subTasks.execute(ctx);
    }

    /**
     * Moves every node to the mappings node.
     */
    protected void operateOnChildNode(Content node, InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final String oldHandle = node.getHandle();
        final String newHandle = SERVER_URI2REPOSITORY_MAPPING + "/mappings/" + node.getName();
        subTasks.addTask(new MoveNodeTask("Move " + oldHandle + " to " + newHandle + "", "Moves the " + oldHandle + " node to its new location", ContentRepository.CONFIG, oldHandle, newHandle, true));
    }

}
