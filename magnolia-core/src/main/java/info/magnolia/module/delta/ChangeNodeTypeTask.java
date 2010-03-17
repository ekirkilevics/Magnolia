/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import javax.jcr.RepositoryException;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.InstallContext;

/**
 * Changes a node type by delegating to {@link ContentUtil#changeNodeType(Content, ItemType, boolean)} with the last parameter set to <code>false</code>.
 * @author fgrilli
 * @version $Id$
 */
public class ChangeNodeTypeTask extends AbstractRepositoryTask {

    private final String nodePath;
    private final String workspace;
    private final ItemType newType;

    /**
     * @param nodePath - String the path to the node 
     * @param workspace - String the workspace where the node is stored
     * @param newType - {@link ItemType} the new type
     */
    public ChangeNodeTypeTask(String nodePath, String workspace, ItemType newType) {
        super("Change node type for " + nodePath, "Changes nodeType for " + nodePath + " to " + newType.getSystemName());
        this.nodePath = nodePath;
        this.workspace = workspace;
        this.newType = newType;
    }

    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager(workspace);
        try {
            final Content node = hm.getContent(nodePath);
            ContentUtil.changeNodeType(node, newType, false);
        } catch (RepositoryException e) {
            ctx.error("Could not change node type: ", e);
        }
    }
}
