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
package info.magnolia.module.workflow;

import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import openwfe.org.embed.impl.engine.AbstractEmbeddedParticipant;
import openwfe.org.engine.workitem.CancelItem;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.WorkItem;

import org.apache.commons.chain.Command;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Magnolia-specific workflow participant.
 * @author Jackie Ju
 * @author Philipp Bracher
 * @author Nicolas Modrzyk
 * @author John Mettraux
 */
public class MgnlParticipant extends AbstractEmbeddedParticipant {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(AbstractEmbeddedParticipant.class);

    public MgnlParticipant() throws Exception {
        super();
    }

    public MgnlParticipant(String name) throws Exception {
        super(name);
    }

    public void cancel(CancelItem cancelItem) throws Exception {
        if (log.isDebugEnabled()) {
            if (cancelItem.getId() != null) {
                log.debug("Cancelling {}", cancelItem.getId().toParseableString());
            }

        }
        String parName = cancelItem.getParticipantName();
        if (!parName.startsWith(WorkflowConstants.PARTICIPANT_PREFIX_COMMAND)) {
            //
            // remove workitem from inbox
            WorkflowUtil.getWorkItemStore().removeWorkItem(cancelItem.getId());
        }
        MgnlContext.release();
    }

    /**
     * @see openwfe.org.embed.engine.EmbeddedParticipant#consume(openwfe.org.engine.workitem.WorkItem)
     */
    public void consume(WorkItem wi) throws Exception {

        // get participant name
        log.debug("Enter consume()..");

        if (wi == null) {
            log.error("Work item is null");
            return;
        }
        String parName = ((InFlowWorkItem) (wi)).getParticipantName();

        log.debug("Participant name = {}", parName);

        Context originalContext = null;
        if (MgnlContext.hasInstance()) {
            originalContext = MgnlContext.getInstance();
        }
        try{

            if (parName.startsWith(WorkflowConstants.PARTICIPANT_PREFIX_COMMAND)){
                log.debug("Consuming command {}...", parName);

                try {
                    String name = StringUtils.removeStart(parName, WorkflowConstants.PARTICIPANT_PREFIX_COMMAND);
                    Command c = CommandsManager.getInstance().getCommand(name);
                    if (c != null) {
                        log.debug("Command has been found through the magnolia catalog: {}", c.getClass().getName());

                        // set parameters in the context
                        // precise what we're talking about here: this is forced to be a System Context :
                        // since we are processing within the workflow enviroment
                        Context context = new WorkItemContext(MgnlContext.getSystemContext(), wi);

                        // remember to reset it after execution
                        MgnlContext.setInstance(context);

                        // execute
                        c.execute(context);

                        WorkflowModule.getEngine().reply((InFlowWorkItem) wi);

                    }
                    else {
                        // not found, do in the old ways
                        log.error("No command has been found through the magnolia catalog for name: {}", parName);
                    }

                    log.debug("Consumed command {}.", parName);
                }
                catch (Exception e) {
                    log.error("Couldn't consume command " + parName, e);
                }
            }
            else {
                MgnlContext.setInstance(MgnlContext.getSystemContext());
                WorkflowUtil.getWorkItemStore().storeWorkItem(StringUtils.EMPTY, (InFlowWorkItem) wi);
            }
        }
        finally {
            MgnlContext.release();
            MgnlContext.setInstance(originalContext);
        }

        log.debug("Finished consume()..");

    }
}
