package info.magnolia.module.owfe.commands.intreecommands;

import info.magnolia.module.owfe.commands.MgnlCommand;

import java.util.HashMap;

public class DeactivationCommand {
    private static final String REAL_DEACTIVATE = "realDeactivate";

    public String getTargetCommand() {
        return REAL_DEACTIVATE;
    }

    public HashMap translateParam(HashMap param) {
        HashMap params = new HashMap();
        params.put(MgnlCommand.P_PATH, param.get(MgnlCommand.P_PATH));
        params.put(MgnlCommand.P_RECURSIVE, MgnlCommand.P_RECURSIVE);
        return params;
    }
}
