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
        if (log.isDebugEnabled()) {
            log.debug("storage = " + this.storage);
        }
    }

    public MgnlParticipant(String arg0) throws Exception {
        super(arg0);
        this.storage = new JCRWorkItemAPI();
        if (log.isDebugEnabled()) {
            log.debug("storage = " + this.storage);
        }
    }

    /**
     * @see openwfe.org.embed.engine.EmbeddedParticipant#consume(openwfe.org.engine.workitem.WorkItem)
     */
    public void consume(WorkItem wi) throws Exception {

        // get participant name
        if (log.isDebugEnabled()) {
            log.debug("enter consume()..");
        }
        if (wi == null) {
            log.error("work item is null");
            return;
        }
        String parName = ((InFlowWorkItem) (wi)).getParticipantName();
        if (log.isDebugEnabled()) {
            log.debug("participant name = " + parName);
        }
        if (parName.startsWith(WorkflowConstants.PARTICIPANT_PREFIX_COMMAND)) // handle commands
        {
            log.info("consume command " + parName + "...");
            if (log.isDebugEnabled()) {
                log.debug("command name is " + parName);
            }

            try {
                String name =  StringUtils.removeStart(parName, WorkflowConstants.PARTICIPANT_PREFIX_COMMAND);
                Command c = CommandsManager.getInstance().getCommand(name);
                if (c != null) {
                    log.info("Command has been found through the magnolia catalog:" + c.getClass().getName());

                    // set parameters in the context
                    Context context = MgnlContext.getInstance();
                    
                    context = new WorkItemContext(context, wi);

                    // execute
                    c.execute(context);

                    WorkflowModule.getEngine().reply((InFlowWorkItem) wi);

                }
                else {
                    // not found, do in the old ways
                    log.error("No command has been found through the magnolia catalog for name:" + parName);
                }

                log.info("consume command " + parName + " end.");
            }
            catch (Exception e) {
                log.error("consume command failed", e);
            }
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("storage = " + this.storage);
            }
            this.storage.storeWorkItem("", (InFlowWorkItem) wi);
        }

        if (log.isDebugEnabled()) {
            log.debug("leave consume()..");
        }

    }

}
