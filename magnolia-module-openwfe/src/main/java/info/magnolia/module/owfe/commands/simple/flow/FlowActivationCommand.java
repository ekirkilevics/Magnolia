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


    public String getFlowName() {
        return "webActivation";
    }

    public boolean exec(HashMap params, Context ctx) {
        return super.exec(params, ctx);
    }


    public void preLaunchFlow(Context context, HashMap params, JCRPersistedEngine engine, LaunchItem li) {
        try {

            // Retrieve parameters
            String pathSelected = (String) params.get(MgnlCommand.P_PATH);
            boolean recursive = ((Boolean) params.get(MgnlCommand.P_RECURSIVE)).booleanValue();

            // Parameters for the flow item

            li.addAttribute(MgnlCommand.P_RECURSIVE, new StringAttribute(recursive ? "true" : "false"));
            li.addAttribute(MgnlCommand.P_PATH, new StringAttribute(pathSelected));
            li.addAttribute("OK", new StringAttribute("false"));
        } catch (Exception e) {
            MgnlCommand.log.error("can't launch activate flow", e);
            AlertUtil.setMessage(AlertUtil.getExceptionMessage(e));
        }

    }


}
