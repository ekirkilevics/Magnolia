package info.magnolia.module.owfe.commands.intreecommands;

import info.magnolia.module.owfe.commands.MgnlCommand;

import java.util.HashMap;

public class ActivationCommand extends AbstractInTreeCommand {
    private static final String REAL_ACTIVATE = "realActivate";

    public String getTargetCommand() {
        return REAL_ACTIVATE;
    }

    public HashMap translateParam(HashMap param) {
        HashMap params = new HashMap();
        params.put(MgnlCommand.P_PATH, param.get(MgnlCommand.P_PATH));
        params.put(MgnlCommand.P_RECURSIVE, MgnlCommand.P_RECURSIVE);
        return params;
    }

}
