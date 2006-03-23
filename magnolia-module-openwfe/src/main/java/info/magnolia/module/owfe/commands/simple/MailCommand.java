package info.magnolia.module.owfe.commands.simple;

import info.magnolia.module.owfe.commands.MgnlCommand;
import openwfe.org.engine.workitem.InFlowWorkItem;
import org.apache.commons.chain.Context;

import java.util.HashMap;

public class MailCommand implements MgnlCommand {

    public boolean execute(Context context) {
        HashMap params = (HashMap) context.get(PARAMS);
        String path;
        InFlowWorkItem if_wi = (InFlowWorkItem) params.get(MgnlCommand.P_WORKITEM);
        if (if_wi != null) { // if call from flow
            path = (if_wi.getAttribute(P_PATH)).toString();
        } else {
            path = (String) params.get(P_PATH);
        }
        return true;
    }
}
