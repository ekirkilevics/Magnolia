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

import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.owfe.MgnlConstants;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;

import java.util.HashMap;

import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;

import org.apache.commons.chain.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The activation command which will launch a flow to do scheduled activation by "sleep" functionality of owfe
 * @author jackie
 */
public class TimeBasedFlowActivationCommand extends AbstractFlowCommand {

    private static final String WEB_SCHEDULED_ACTIVATION = "webScheduledActivation";

    private static Logger log = LoggerFactory.getLogger(TimeBasedFlowActivationCommand.class);

    static final String[] parameters = {
        MgnlConstants.P_RECURSIVE,
        MgnlConstants.P_START_DATE,
        MgnlConstants.P_END_DATE,
        MgnlConstants.P_PATH};

    /**
     * List of the parameters that this command needs to run
     * @return a list of string describing the parameters needed. The parameters should have a mapping in this class.
     */
    public String[] getExpectedParameters() {
        return parameters;
    }

    public String getFlowName() {
        return WEB_SCHEDULED_ACTIVATION;
    }

    public void preLaunchFlow(Context context, HashMap params, JCRPersistedEngine engine, LaunchItem li) {

        try {

            // Retrieve parameters from caller
            String pathSelected = (String) params.get(MgnlConstants.P_PATH);
            String startDate = null;
            String endDate = null;
            String s = (String) params.get(MgnlConstants.P_START_DATE);
            if (s != null) {
                startDate = params.get(MgnlConstants.P_START_DATE).toString();
            }

            s = (String) params.get(MgnlConstants.P_END_DATE);
            if (s != null) {
                endDate = params.get(MgnlConstants.P_END_DATE).toString();
            }

            log.info("start date = " + startDate);
            log.info("end date = " + endDate);

            // set parameters for lanuching the flow
            li.setWorkflowDefinitionUrl(MgnlConstants.P_WORKFLOW_DEFINITION_URL);
            li.addAttribute(MgnlConstants.P_PATH, new StringAttribute(pathSelected));
            li.addAttribute(MgnlConstants.P_OK, MgnlConstants.ATT_FALSE);
            li.addAttribute(MgnlConstants.P_RECURSIVE, new StringAttribute((params.get(MgnlConstants.P_RECURSIVE))
                .toString()));

            if (startDate != null) {
                li.getAttributes().puts(MgnlConstants.P_START_DATE, startDate);
            }
            if (endDate != null) {
                li.getAttributes().puts(MgnlConstants.P_END_DATE, endDate);
            }

            // Retrieve and add the flow definition to the LaunchItem
            String flowDef = new JCRFlowDefinition()
                .getflowDefAsString(MgnlConstants.P_DEFAULT_SCHEDULEDACTIVATION_FLOW);

            // log.info(flowDef);
            li.getAttributes().puts(MgnlConstants.P_DEFINITION, flowDef);

        }
        catch (Exception e) {
            log.error("can't launch flow", e);
            AlertUtil.setMessage(AlertUtil.getExceptionMessage(e));
        }
    }

}
