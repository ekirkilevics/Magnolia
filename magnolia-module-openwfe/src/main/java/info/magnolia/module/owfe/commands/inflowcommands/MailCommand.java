package info.magnolia.module.owfe.commands.inflowcommands;

import info.magnolia.module.owfe.commands.CommandsMap;
import info.magnolia.module.owfe.commands.MgnlCommand;
import openwfe.org.engine.workitem.WorkItem;
import org.apache.commons.chain.Context;

import java.util.HashMap;

public class MailCommand extends AbstractInFlowCommand {
    static final String TARGETCMD_NAME = "realSendMail";

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
        String mailTo = "";
        if (workItem != null) // if call from flow
            mailTo = (workItem.getAttribute(P_MAILTO)).toString();

        if (log.isDebugEnabled())
            log.debug("mailTo = " + mailTo);
        HashMap params = new HashMap();
        params.put(MgnlCommand.P_MAILTO, mailTo);

        return params;

    }
}
