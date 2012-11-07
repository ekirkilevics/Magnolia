/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.setup.for4_5;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.NewPropertyTask;
import info.magnolia.module.delta.PropertyExistsDelegateTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

/**
 * Updates user managers with properties used in account lockout feature.
 *
 * @author ochytil
 * @version $Revision: $ ($Author: $)
 */
public class UpdateUserManagers extends AbstractRepositoryTask{

    public UpdateUserManagers() {
        super("User managers setup update", "Checks for conditions to create maxFailedLoginAttempts and lockTimePeriod properties. If passes maxFailedLoginAttempts property is set to default value 5 and lockTimePeriod to 0.");
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {

        Session session = ctx.getJCRSession(RepositoryConstants.CONFIG);

        for (Node node : NodeUtil.getNodes(session.getRootNode().getNode("server").getNode("security").getNode("userManagers"), MgnlNodeType.NT_CONTENTNODE)) {
            if(node.hasProperty("class")){
                String className = node.getProperty("class").getString();
                if (className.equals("info.magnolia.cms.security.MgnlUserManager") || className.equals("info.magnolia.cms.security.SystemUserManager")){
                    PropertyExistsDelegateTask updateTask = new PropertyExistsDelegateTask("", "", RepositoryConstants.CONFIG, "/server" + StringUtils.substringAfter(node.getPath(), "/server"), "maxFailedLoginAttempts", null, new NewPropertyTask("", "", RepositoryConstants.CONFIG, "/server" + StringUtils.substringAfter(node.getPath(), "/server"), "maxFailedLoginAttempts", "5"));
                    PropertyExistsDelegateTask updateTask2 = new PropertyExistsDelegateTask("", "", RepositoryConstants.CONFIG, "/server" + StringUtils.substringAfter(node.getPath(), "/server"), "lockTimePeriod", null, new NewPropertyTask("", "", RepositoryConstants.CONFIG, "/server" + StringUtils.substringAfter(node.getPath(), "/server"), "lockTimePeriod", "0"));
                    updateTask.execute(ctx);
                    updateTask2.execute(ctx);
                }
            }
        }
    }
}
