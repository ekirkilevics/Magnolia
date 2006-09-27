/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.workflow;

import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.workflow.jcr.JCRWorkItemAPI;
import openwfe.org.embed.impl.engine.AbstractEmbeddedParticipant;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.WorkItem;
import openwfe.org.engine.workitem.CancelItem;

import org.apache.commons.chain.Command;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MgnlParticipant extends AbstractEmbeddedParticipant {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(AbstractEmbeddedParticipant.class);

    JCRWorkItemAPI storage = null;

    public MgnlParticipant() throws Exception {
        super();
        this.storage = new JCRWorkItemAPI();
        log.debug("storage = {}", this.storage);
    }

    public MgnlParticipant(String name) throws Exception {
        super(name);
        this.storage = new JCRWorkItemAPI();
        log.debug("storage = {}", this.storage);
    }

    public void cancel(CancelItem cancelItem) throws Exception {
        if (log.isDebugEnabled()) {
            if (cancelItem != null && cancelItem.getId() != null) {
                log.debug("Cancelling {}", cancelItem.getId().toParseableString());
            }
            else {
                log.debug("Calling cancel on participant with null cancel item");
            }
        }
    }

    /**
     * @see openwfe.org.embed.engine.EmbeddedParticipant#consume(openwfe.org.engine.workitem.WorkItem)
     */
    public void consume(WorkItem wi) throws Exception {

        // get participant name
        log.debug("enter consume()..");

        if (wi == null) {
            log.error("work item is null");
            return;
        }
        String parName = ((InFlowWorkItem) (wi)).getParticipantName();

        log.debug("participant name = {}", parName);

        if (parName.startsWith(WorkflowConstants.PARTICIPANT_PREFIX_COMMAND)) // handle commands
        {
            log.info("consume command {}...", parName);

            log.debug("command name is {}", parName);

            try {
                String name = StringUtils.removeStart(parName, WorkflowConstants.PARTICIPANT_PREFIX_COMMAND);
                Command c = CommandsManager.getInstance().getCommand(name);
                if (c != null) {
                    log.info("Command has been found through the magnolia catalog: {}", c.getClass().getName());

                    // set parameters in the context
                    // precise what we're talking about here: this is forced to be a System Context :
                    // since we are processing within the workflow enviroment
                    // TODO: fix this by using a proper workflow context
                    Context context = MgnlContext.getSystemContext();

                    context = new WorkItemContext(context, wi);
                    MgnlContext.setInstance(context);
                    // execute
                    c.execute(context);

                    WorkflowModule.getEngine().reply((InFlowWorkItem) wi);

                }
                else {
                    // not found, do in the old ways
                    log.error("No command has been found through the magnolia catalog for name: {}", parName);
                }

                log.info("consume command {} end", parName);
            }
            catch (Exception e) {
                log.error("consume command failed", e);
            }
        }
        else {
            log.debug("storage = {}", this.storage);

            this.storage.storeWorkItem(StringUtils.EMPTY, (InFlowWorkItem) wi);
        }

        log.debug("leave consume()..");

    }

}
