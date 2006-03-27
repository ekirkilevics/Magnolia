package info.magnolia.module.owfe.commands.inflowcommands;

import info.magnolia.module.owfe.commands.CommandsMap;
import info.magnolia.module.owfe.commands.MgnlCommand;

import java.util.HashMap;

import openwfe.org.engine.workitem.WorkItem;

import org.apache.commons.chain.Context;

/**
 * The main goal of in flow commands is to traslate the flow context to generic command context
 * @author jackie
 *
 */
public abstract class AbstractInFlowCommand  implements MgnlCommand  {

//	String targetCommand = "";	

	
    public boolean execute(Context context) {
    	WorkItem workItem = (WorkItem) context.get(INFLOW_PARAM);
        log.info("- In Flow command -" + this.getClass().toString() + "- Start");
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
        log.info("- In Flow command -" + this.getClass().toString() + "- End");
        return true;
    }


    	public boolean exec(Context context, String flowName) {
    		try{
    			new CommandsMap().getCommand(flowName).execute(context);
    		}catch (Exception e){
    			log.error("execute command " + flowName + " failed ", e);
    			return false;
    		}
    		return true;
    	}
    
    
    public abstract HashMap translateParam(WorkItem workItem);

    public abstract String getTargetCommand();


}
