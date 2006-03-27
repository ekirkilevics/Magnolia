package info.magnolia.module.owfe.commands.simple;

import info.magnolia.module.owfe.commands.MgnlCommand;
import openwfe.org.engine.workitem.InFlowWorkItem;
import org.apache.commons.chain.Context;

import java.util.HashMap;


public abstract class SimpleCommand implements MgnlCommand {

    public boolean execute(Context context) {
        HashMap params = (HashMap) context.get(PARAMS);
//        String path;
//        InFlowWorkItem if_wi = (InFlowWorkItem) params.get(MgnlCommand.P_WORKITEM);
//        if (if_wi != null) { // if call from flow
//            path = (if_wi.getAttribute(P_PATH)).toString();
//        } else {
//            path = (String) params.get(P_PATH);
//        }
//        try {
//            log.info("Executing a simple command on path" + path);
//        } catch (Exception e) {
//            log.error("cannot do deactivate", e);
//            return false;
//        }
        return exec(params, context);
    }
    
    public abstract boolean  exec(HashMap param, Context ctx);

}
