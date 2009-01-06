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
package info.magnolia.module.workflow.setup.for3_5;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.module.workflow.commands.ActivationFlowCommand;

/**
 * @author philipp
 * @version $Id$
 *
 */
public class SetDefaultWorkflowForActivationFlowCommands extends AbstractRepositoryTask {

    public SetDefaultWorkflowForActivationFlowCommands() {
        super("Workflow activation", "Sets default values on workflow activation commands");
    }

    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        Collection nodes = QueryUtil.query(ContentRepository.CONFIG, "select * from nt:base where class='" + ActivationFlowCommand.class.getName() + "'");
        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            Content node = (Content) iter.next();
            if(!node.hasNodeData("workflowName")){
                node.createNodeData("workflowName").setValue(WorkflowConstants.DEFAULT_ACTIVATION_WORKFLOW);
            }
            if(!node.hasNodeData("dialogName")){
                node.createNodeData("dialogName").setValue(WorkflowConstants.DEFAULT_ACTIVATION_EDIT_DIALOG);
            }
        }
    }
}
