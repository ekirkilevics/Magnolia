/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
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
 * Removes a property and optionally logs its absence.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RemovePropertyTask extends AbstractRepositoryTask {
    private final String workspaceName;
    private final String parentPath;
    private final String propertyToRemove;

    public RemovePropertyTask(String name, String description, String workspaceName, String parentPath, String propertyToRemove) {
        super(name, description);
        this.workspaceName = workspaceName;
        this.parentPath = parentPath;
        this.propertyToRemove = propertyToRemove;
    }

    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager(workspaceName);
        if (!hm.isExist(parentPath)) {
            ctx.warn("Was supposed to remove property " + propertyToRemove + " at " + parentPath + " but the node was not found in workspace " + workspaceName);
            return;
        }
        final Content node = hm.getContent(parentPath);
        if (node.hasNodeData(propertyToRemove)) {
            node.deleteNodeData(propertyToRemove);
        } else {
            ctx.info(parentPath + "/" + propertyToRemove + " was supposed to be removed, but wasn't found.");
        }
    }
}
