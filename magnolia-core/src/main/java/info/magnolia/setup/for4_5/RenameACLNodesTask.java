/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.setup.for4_5;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renames ACL nodes for all roles from the old format of acl_repositoryName_workspaceName to acl_workspaceName.
 *
 * @version $Id$
 */
public class RenameACLNodesTask extends AbstractRepositoryTask {
    private final static Logger log = LoggerFactory.getLogger(RenameACLNodesTask.class);

    public RenameACLNodesTask() {
        super("Security", "Renames ACL nodes.");
    }

    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {

        Session session = installContext.getJCRSession(RepositoryConstants.USER_ROLES);

        for (Node roleNode : NodeUtil.getNodes(session.getRootNode(), MgnlNodeType.ROLE)) {

            for (Node aclNode : NodeUtil.getNodes(roleNode, MgnlNodeType.NT_CONTENTNODE)) {

                String nodeName = aclNode.getName();
                if (nodeName.startsWith("acl_") && nodeName.substring(4).contains("_")) {
                    String[] tokens = StringUtils.split(nodeName, "_");
                    if (tokens.length == 3) {
                        String newName = "acl_" + tokens[2];
                        log.info("Renaming ACL node {} to {}", aclNode.getPath(), newName);
                        NodeUtil.renameNode(aclNode, newName);
                    }
                }
            }
        }
    }
}
