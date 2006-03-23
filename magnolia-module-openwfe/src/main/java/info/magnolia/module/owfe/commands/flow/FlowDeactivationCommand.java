package info.magnolia.module.owfe.commands.flow;

import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.owfe.commands.MgnlCommand;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;
import org.apache.commons.chain.Context;

import java.util.HashMap;

/**
 * Starts the deactivation flow
 *
 * @author jackie
 * @author nicolas
 */
public class FlowDeactivationCommand extends AbstractFlowCommand {

    public void onExecute(Context context, HashMap params, JCRPersistedEngine engine, LaunchItem li) {
        try {
            // Retrieve parameters
            String pathSelected = (String) params.get(MgnlCommand.P_PATH);

            // Parameters for the flow item
            li.setWorkflowDefinitionUrl(MgnlCommand.P_WORKFLOW_DEFINITION_URL);
            li.addAttribute(MgnlCommand.P_PATH, new StringAttribute(pathSelected));
            li.addAttribute("OK", new StringAttribute("false"));

            // Retrieve and add the flow definition to the LaunchItem
            String flowDef = new JCRFlowDefinition().getflowDefAsString(MgnlCommand.P_DEFAULT_DEACTIVATION_FLOW);
            li.getAttributes().puts(MgnlCommand.P_DEFINITION, flowDef);

        } catch (Exception e) {
            MgnlCommand.log.error("can't launch deactivate flow", e);
            AlertUtil.setMessage(AlertUtil.getExceptionMessage(e));
        }
    }
}
