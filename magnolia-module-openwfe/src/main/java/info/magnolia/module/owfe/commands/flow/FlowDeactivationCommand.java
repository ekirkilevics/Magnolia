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
import info.magnolia.commands.ContextAttributes;
import info.magnolia.module.owfe.util.WorkItemUtil;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;

import org.apache.commons.chain.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Starts the deactivation flow
 * @author jackie
 * @author nicolas
 */
public class FlowDeactivationCommand extends AbstractFlowCommand {

    protected static final String FLOWNAME = "webDetactivation";

    private static Logger log = LoggerFactory.getLogger(FlowDeactivationCommand.class);
    
    public String getFlowName() {
        return FLOWNAME;
    }

    public void prepareLaunchItem(Context context, LaunchItem li) {
        try {
            // Retrieve parameters
            String pathSelected = (String) context.get(ContextAttributes.P_PATH);

            // Parameters for the flow item
            li.addAttribute(ContextAttributes.P_PATH, new StringAttribute(pathSelected));
            li.addAttribute(ContextAttributes.P_OK, WorkItemUtil.ATT_FALSE);

        }
        catch (Exception e) {
            log.error("can't launch deactivate flow", e);
            AlertUtil.setMessage(AlertUtil.getExceptionMessage(e));
        }

    }
}
