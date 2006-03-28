package info.magnolia.module.owfe.commands.simple.flow;

import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.owfe.commands.MgnlCommand;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;
import org.apache.commons.chain.Context;

import java.util.HashMap;

public class TimeBasedFlowActivationCommand extends AbstractFlowCommand {


    public String getFlowName() {
        return "webScheduledActivation";
    }


    public boolean exec(HashMap params, Context ctx) {
        return super.exec(params, ctx);
    }


    public void preLaunchFlow(Context context, HashMap params, JCRPersistedEngine engine, LaunchItem li) {
        try {
            // Retrieve parameters from caller
            String pathSelected = (String) params.get(MgnlCommand.P_PATH);

            // set parameters for lanuching the flow
            li.setWorkflowDefinitionUrl(MgnlCommand.P_WORKFLOW_DEFINITION_URL);
            li.addAttribute(MgnlCommand.P_PATH, new StringAttribute(pathSelected));
            li.addAttribute("OK", new StringAttribute("false"));


            li.addAttribute("startDate", new StringAttribute(params.get("startDate")));
            li.addAttribute("endDate", new StringAttribute(params.get("endDate")));

            // Retrieve and add the flow definition to the LaunchItem
            String flowDef = new JCRFlowDefinition().getflowDefAsString(MgnlCommand.P_DEFAULT_SCHEDULEDACTIVATION_FLOW);
            li.getAttributes().puts(MgnlCommand.P_DEFINITION, flowDef);

        } catch (Exception e) {
            MgnlCommand.log.error("can't launch deactivate flow", e);
            AlertUtil.setMessage(AlertUtil.getExceptionMessage(e));
        }
    }

}
