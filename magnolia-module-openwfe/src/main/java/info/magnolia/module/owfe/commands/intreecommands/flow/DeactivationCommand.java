package info.magnolia.module.owfe.commands.intreecommands.flow;

import info.magnolia.module.owfe.commands.MgnlCommand;
import info.magnolia.module.owfe.commands.intreecommands.AbstractInTreeCommand;

import java.util.HashMap;

public class DeactivationCommand extends AbstractInTreeCommand {
    private static final String FLOW_DACTIVATE = "flowDactivate";

    public String getTargetCommand() {
        return FLOW_DACTIVATE;
    }

    public HashMap translateParam(HashMap param) {
        HashMap params = new HashMap();
        params.put(MgnlCommand.P_PATH, param.get(MgnlCommand.P_PATH));
        params.put(MgnlCommand.P_RECURSIVE, MgnlCommand.P_RECURSIVE);
        return params;
    }

}