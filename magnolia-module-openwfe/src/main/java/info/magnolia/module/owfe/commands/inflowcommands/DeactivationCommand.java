package info.magnolia.module.owfe.commands.inflowcommands;

import info.magnolia.module.owfe.commands.CommandsMap;
import info.magnolia.module.owfe.commands.MgnlCommand;
import openwfe.org.engine.workitem.WorkItem;
import org.apache.commons.chain.Context;

import java.util.HashMap;

public class DeactivationCommand extends AbstractInFlowCommand {
    static final String TARGETCMD_NAME = "realDeactivate";

    public String getTargetCommand() {
        return TARGETCMD_NAME;
    }

    public boolean exec(Context context, String flowName) {
        try {
            CommandsMap.getCommand(flowName).execute(context);
        } catch (Exception e) {
            log.error("execute command " + flowName + " failed ", e);
            return false;
        }
        return true;
    }

    public HashMap translateParam(WorkItem workItem) {
        String path = "";
        boolean recursive = false; // is initialized at false
        if (workItem != null) { // if call from flow
            path = (workItem.getAttribute(P_PATH)).toString();
            recursive = (workItem.getAttribute(P_RECURSIVE)).equals(MgnlCommand.TRUE);
        }

        HashMap params = new HashMap();
        params.put(MgnlCommand.P_PATH, path);
        params.put(MgnlCommand.P_RECURSIVE, recursive ? Boolean.TRUE : Boolean.FALSE);

        return params;

    }
}
