/**
 * This file Copyright (c) 2008-2009 Magnolia International
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
package info.magnolia.module.cache;

import java.security.InvalidParameterException;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;

/**
 * Adds repository to the list of repositories observed for cache flushing upon activation.
 * Tasks checks for existence of the repository in the list before adding it so it is safe to execute multiple times.
 *
 * @author had
 *
 */
public class RegisterWorkspaceForCacheFlushingTask extends AbstractTask implements Task {

    private String workspaceName;

    public RegisterWorkspaceForCacheFlushingTask(String workspaceName) {
        super("Cache flushing", "Updates the cache flushing configuration to trigger cache flush when new or updated content is published to " + workspaceName + " workspace.");
        this.workspaceName = workspaceName;
        if (workspaceName == null) {
            throw new InvalidParameterException("Workspace name can't be empty.");
        }
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        HierarchyManager hm = ctx.getConfigHierarchyManager();
        try {
            String nodePath = "/modules/cache/config/configurations/default/flushPolicy/policies/flushAll/repositories";
            if (!ctx.getModulesNode().hasContent("cache") || !hm.isExist(nodePath)) {
                // cache is not installed or other then default flush policy is used - ignore
                return;
            }
            Content c = hm.getContent(nodePath);
            // check if the workspace is not already registered manually by user. If so, just bail out, no need to punish users who registered workspaces previously themselves with the error message.
            Iterator iter = c.getNodeDataCollection().iterator();
            boolean found = false;
            while (iter.hasNext()) {
                if (this.workspaceName.equals(((NodeData) iter.next()).getString())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                c.createNodeData(Path.getUniqueLabel(c, "0"), this.workspaceName);
            }
        } catch (RepositoryException e) {
            throw new TaskExecutionException(e.getMessage(), e);
        }
    }
}
