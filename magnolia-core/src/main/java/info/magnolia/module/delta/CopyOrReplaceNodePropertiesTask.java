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
import info.magnolia.cms.core.NodeData;
import info.magnolia.module.InstallContext;

import java.util.Collection;

import javax.jcr.RepositoryException;

/**
 * Copies a node's properties to another node. Existing properties are overwritten and
 * extra properties on the target node are left untouched.
 * Only works with properties of type String!
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CopyOrReplaceNodePropertiesTask extends AbstractRepositoryTask {
    private final String workspaceName;
    private final String sourceNodePath;
    private final String targetNodePath;

    public CopyOrReplaceNodePropertiesTask(String name, String description, String workspaceName, String sourceNodePath, String targetNodePath) {
        super(name, description);
        this.workspaceName = workspaceName;
        this.sourceNodePath = sourceNodePath;
        this.targetNodePath = targetNodePath;
    }

    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager(workspaceName);
        final Content source = hm.getContent(sourceNodePath);
        final Content target = hm.getContent(targetNodePath);
        final Collection<NodeData> props = source.getNodeDataCollection();
        for (NodeData prop : props) {
            final String name = prop.getName();
            final String value = prop.getString();
            if (target.hasNodeData(name)) {
                target.getNodeData(name).setValue(value);
            } else {
                target.createNodeData(name, value);
            }
        }

    }
}
