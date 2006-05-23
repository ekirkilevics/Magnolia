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
package info.magnolia.module.owfe.commands.flow;

import info.magnolia.commands.ContextAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The activation command which will launch a flow to do scheduled activation by "sleep" functionality of owfe
 * @author jackie
 */
public class TimeBasedFlowActivationCommand extends AbstractFlowCommand {

    private static final String WEB_SCHEDULED_ACTIVATION = "webScheduledActivation";

    private static Logger log = LoggerFactory.getLogger(TimeBasedFlowActivationCommand.class);

    public String getFlowName() {
        return WEB_SCHEDULED_ACTIVATION;
    }

    //FIXME remove as much as possible
    /*

    public void prepareLaunchItem(Context context, LaunchItem li) {
        try {

            // Retrieve parameters from caller
            String pathSelected = (String) context.get(ContextAttributes.P_PATH);
            String startDate = null;
            String endDate = null;
            String s = (String) context.get(ContextAttributes.P_START_DATE);
            if (s != null) {
                startDate = context.get(ContextAttributes.P_START_DATE).toString();
            }

            s = (String) context.get(ContextAttributes.P_END_DATE);
            if (s != null) {
                endDate = context.get(ContextAttributes.P_END_DATE).toString();
            }

            // set parameters for lanuching the flow
            li.setWorkflowDefinitionUrl(ContextAttributes.P_WORKFLOW_DEFINITION_URL);
            li.addAttribute(ContextAttributes.P_PATH, new StringAttribute(pathSelected));
            li.addAttribute(ContextAttributes.P_OK, WorkItemUtil.ATT_FALSE);
            li.addAttribute(ContextAttributes.P_RECURSIVE, new StringAttribute((context.get(ContextAttributes.P_RECURSIVE))
                .toString()));

            if (startDate != null) {
                li.getAttributes().puts(ContextAttributes.P_START_DATE, startDate);
            }
            if (endDate != null) {
                li.getAttributes().puts(ContextAttributes.P_END_DATE, endDate);
            }

            // Retrieve and add the flow definition to the LaunchItem
            String flowDef = new JCRFlowDefinition()
                .getflowDefAsString(ContextAttributes.P_DEFAULT_SCHEDULEDACTIVATION_FLOW);

            // log.info(flowDef);
            li.getAttributes().puts(ContextAttributes.P_DEFINITION, flowDef);

        }
        catch (Exception e) {
            log.error("can't launch flow", e);
            AlertUtil.setMessage(AlertUtil.getExceptionMessage(e));
        }
    }
    */

}
