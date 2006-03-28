package info.magnolia.module.owfe.commands.flow;

import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.owfe.commands.MgnlCommand;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;
import org.apache.commons.chain.Context;

import java.util.HashMap;

public class TimeBasedFlowActivationCommand extends AbstractFlowCommand {
    private static final String WEB_SCHEDULED_ACTIVATION = "webScheduledActivation";

    static final String[] parameters = {P_START_DATE, P_END_DATE, P_PATH};

    /**
     * List of the parameters that this command needs to run
     *
     * @return a list of string describing the parameters needed. The parameters should have a  mapping in this class.
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
            String pathSelected = (String) params.get(MgnlCommand.P_PATH);

            // set parameters for lanuching the flow
            li.setWorkflowDefinitionUrl(MgnlCommand.P_WORKFLOW_DEFINITION_URL);
            li.addAttribute(MgnlCommand.P_PATH, new StringAttribute(pathSelected));
            li.addAttribute(MgnlCommand.P_OK, MgnlCommand.ATT_FALSE);


            li.addAttribute(MgnlCommand.P_START_DATE, new StringAttribute(params.get(MgnlCommand.P_START_DATE)));
            li.addAttribute(MgnlCommand.P_END_DATE, new StringAttribute(params.get(MgnlCommand.P_END_DATE)));

            // Retrieve and add the flow definition to the LaunchItem
            String flowDef = new JCRFlowDefinition().getflowDefAsString(MgnlCommand.P_DEFAULT_SCHEDULEDACTIVATION_FLOW);
            li.getAttributes().puts(MgnlCommand.P_DEFINITION, flowDef);

        } catch (Exception e) {
            MgnlCommand.log.error("can't launch deactivate flow", e);
            AlertUtil.setMessage(AlertUtil.getExceptionMessage(e));
        }
    }

}
