package info.magnolia.module.owfe.commands.inflowcommands;

import info.magnolia.module.owfe.commands.MgnlCommand;
import openwfe.org.engine.workitem.WorkItem;

import java.util.HashMap;

public class ActivationCommand extends AbstractInFlowCommand {


    static final String TARGETCMD_NAME = "realActivate";

    public String getTargetCommand() {
        return TARGETCMD_NAME;
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
        params.put(MgnlCommand.P_RECURSIVE, new Boolean(recursive));
        return params;

    }

}
