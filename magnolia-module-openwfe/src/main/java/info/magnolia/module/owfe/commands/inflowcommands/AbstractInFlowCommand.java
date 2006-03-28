package info.magnolia.module.owfe.commands.inflowcommands;

import info.magnolia.module.owfe.commands.CommandsMap;
import info.magnolia.module.owfe.commands.MgnlCommand;
import openwfe.org.engine.workitem.WorkItem;
import org.apache.commons.chain.Context;

import java.util.HashMap;

/**
 * The main goal of in flow commands is to traslate the flow context to generic command context
 *
 * @author jackie
 */
public abstract class AbstractInFlowCommand extends MgnlCommand {

    public boolean execute(Context context) {
        WorkItem workItem = (WorkItem) context.get(INFLOW_PARAM);
        if (log.isDebugEnabled())
            log.debug("- In Flow command -" + this.getClass().toString() + "- Start");
        try {

            // traslate parameter
            HashMap params = translateParam(workItem);
            context.put(MgnlCommand.PARAM, params);

            // execute
            return exec(context, getTargetCommand());

        } catch (Exception e) {
            log.error("Execute failed", e);
        }

        // End execution
        if (log.isDebugEnabled())
            log.debug("- In Flow command -" + this.getClass().toString() + "- End");
        return true;
    }


    public boolean exec(HashMap param, Context ctx) {
        throw new RuntimeException("This should not be called here");
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


    public abstract HashMap translateParam(WorkItem workItem);

    public abstract String getTargetCommand();


}
