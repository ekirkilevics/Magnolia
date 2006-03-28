package info.magnolia.module.owfe.commands.simple.flow;

import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.owfe.commands.MgnlCommand;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;
import org.apache.commons.chain.Context;

import java.util.HashMap;

/**
 * @author jackie
 * @author nicolas
 */
public class FlowActivationCommand extends AbstractFlowCommand {
    private static final String WEB_ACTIVATION = "webActivation";


    public String getFlowName() {
        return WEB_ACTIVATION;
    }

    public void preLaunchFlow(Context context, HashMap params, JCRPersistedEngine engine, LaunchItem li) {
        try {

            // Retrieve parameters
            String pathSelected = (String) params.get(MgnlCommand.P_PATH);
            boolean recursive = ((Boolean) params.get(MgnlCommand.P_RECURSIVE)).booleanValue();

            // Parameters for the flow item
            li.addAttribute(MgnlCommand.P_RECURSIVE, recursive ? MgnlCommand.ATT_TRUE : MgnlCommand.ATT_FALSE);
            li.addAttribute(MgnlCommand.P_PATH, new StringAttribute(pathSelected));
            li.addAttribute(MgnlCommand.P_OK, MgnlCommand.ATT_FALSE);
        } catch (Exception e) {
            MgnlCommand.log.error("can't launch activate flow", e);
            AlertUtil.setMessage(AlertUtil.getExceptionMessage(e));
        }

    }


}
