/**
 * This file Copyright (c) 2012 Magnolia International
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import info.magnolia.cms.security.Security;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.iterator.SameChildNodeTypeIterator;
import info.magnolia.module.InstallContext;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * A task to find and remove duplicate permission from a role and a workspace.
 * 
 * @version $Id$
 */
public class RemoveDuplicatePermissionTask extends AbstractRepositoryTask {

    private final String roleName;
    private final String workspaceName;

    public RemoveDuplicatePermissionTask(String taskName, String taskDescription, String roleName, String workspaceName) {
        super(taskName, taskDescription);
        this.roleName = roleName;
        this.workspaceName = workspaceName;
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        try {
            Session session = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES);
            Collection<Node> deleteNode = new ArrayList<Node>();
            
            Node roleNode = session.getNodeByIdentifier(Security.getRoleManager().getRole(roleName).getId());
            Node aclNode = roleNode.getNode(workspaceName);
            NodeIterator childrenIter = new SameChildNodeTypeIterator(aclNode);
            for(int  i = 0; i < childrenIter.getSize(); i++){
                Node child = childrenIter.nextNode();
                NodeIterator secondChildrenIter = new SameChildNodeTypeIterator(aclNode);
                for(int j = 0; j < secondChildrenIter.getSize(); j++){
                    Node otherChild = secondChildrenIter.nextNode();
                    if(childrenIter.getPosition() < secondChildrenIter.getPosition()){
                        if(child.getProperty("path").getString().equals(otherChild.getProperty("path").getString()) && child.getProperty("permissions").getLong() == otherChild.getProperty("permissions").getLong()){
                            log.warn("Found duplicate permission. Role: " + roleName + " Workspace: " + workspaceName + " Permission: " + otherChild.getProperty("permissions").getLong() + " Path: " + otherChild.getProperty("path").getString());
                            deleteNode.add(otherChild);
                            break;                     
                        }
                    }
                }
            }
            Iterator<Node> deleteNodeIter = deleteNode.iterator();
            while(deleteNodeIter.hasNext()){
                deleteNodeIter.next().remove();
            }
            session.save();
        }
        catch (Exception e) {
            log.error("can't remove duplicate permission", e);
        }
    }
}