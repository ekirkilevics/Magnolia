/**
 * This file Copyright (c) 2011 Magnolia International
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

import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Renames all nodes having a certain name and nodeType to the provided new name.
 *
 * @version $Id$
 */
public class RenameNodesTask extends AbstractRepositoryTask {
    private final String workspace;
    private final String path;
    private final String oldName;
    private final String newName;
    private final String nodeType;

    public RenameNodesTask(String name, String description, String workspace, String path, String oldName,
            String newName, String nodeType) {
        super(name, description);
        this.workspace = workspace;
        this.path = path;
        this.oldName = oldName;
        this.newName = newName;
        this.nodeType = nodeType;
    }

    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        final Session session = installContext.getJCRSession(workspace);
        final Node rootOfOperation = session.getNode(path);
        Iterator<Node> iterator = NodeUtil.collectAllChildren(rootOfOperation, new NodeTypePredicate(nodeType)).iterator();
        while (iterator.hasNext()) {
            Node childNode = iterator.next();
            if (oldName.equals(childNode.getName())) {
                NodeUtil.renameNode(childNode, newName);
            }
        }
    }
}
