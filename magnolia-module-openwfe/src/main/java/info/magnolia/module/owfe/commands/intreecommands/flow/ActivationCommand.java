package info.magnolia.module.owfe.commands.intreecommands.flow;

import info.magnolia.module.owfe.commands.MgnlCommand;
import info.magnolia.module.owfe.commands.intreecommands.AbstractInTreeCommand;

import java.util.HashMap;

public class ActivationCommand extends AbstractInTreeCommand {
    private static final String FLOW_ACTIVATE = "flowActivate";

    public String getTargetCommand() {
        return FLOW_ACTIVATE;
    }

    public HashMap translateParam(HashMap param) {
        HashMap params = new HashMap();
        if (log.isDebugEnabled()) {
            log.debug("param = " + param);
            log.debug("params = " + params);
            log.debug("param.get(MgnlCommand.P_PATH)=" + param.get(MgnlCommand.P_PATH));
        }
        params.put(MgnlCommand.P_PATH, param.get(MgnlCommand.P_PATH));
        params.put(MgnlCommand.P_RECURSIVE, MgnlCommand.P_RECURSIVE);
        return params;
    }

}