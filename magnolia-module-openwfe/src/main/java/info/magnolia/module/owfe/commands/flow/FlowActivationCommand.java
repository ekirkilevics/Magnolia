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

/**
 * The activation command which will launch a flow
 * @author jackie
 * @author nicolas
 */
public class FlowActivationCommand extends AbstractFlowCommand {

    private static final String WEB_ACTIVATION = "webActivation";

    public String getFlowName() {
        return WEB_ACTIVATION;
    }

    // FIXME: remove
    /*
    public void prepareLaunchItem(Context context, LaunchItem li) {
        try {
            // Retrieve parameters
            String pathSelected = (String) context.get(ContextAttributes.P_PATH);
            String recursive = (String) context.get(ContextAttributes.P_RECURSIVE);
            boolean brecursive = (recursive != null) && Boolean.valueOf(recursive).booleanValue();

            // Parameters for the flow item
            li.addAttribute(ContextAttributes.P_RECURSIVE, brecursive ? WorkItemUtil.ATT_TRUE : WorkItemUtil.ATT_FALSE);
            li.addAttribute(ContextAttributes.P_PATH, new StringAttribute(pathSelected));
            li.addAttribute(ContextAttributes.P_OK, WorkItemUtil.ATT_FALSE);
        }
        catch (Exception e) {
            log.error("can't launch activate flow", e);
            AlertUtil.setMessage(AlertUtil.getExceptionMessage(e));
        }
    }
    */
}
