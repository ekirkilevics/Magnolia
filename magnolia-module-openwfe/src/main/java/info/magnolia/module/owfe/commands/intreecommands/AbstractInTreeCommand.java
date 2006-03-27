package info.magnolia.module.owfe.commands.intreecommands;

import info.magnolia.module.owfe.commands.CommandsMap;
import info.magnolia.module.owfe.commands.MgnlCommand;

import java.util.HashMap;

import openwfe.org.engine.workitem.WorkItem;

import org.apache.commons.chain.Context;

/**
 * The main goal of in tree commands is to traslate the tree context to generic
 * command context
 * 
 * @author jackie
 * 
 */
public abstract class AbstractInTreeCommand implements MgnlCommand {

	public boolean execute(Context context) {
		HashMap param = (HashMap) context.get(INTREE_PARAM);
		log
				.info("- In Tree command -" + this.getClass().toString()
						+ "- Start");
		try {

			// traslate parameter
			HashMap params = translateParam(param);
			context.put(MgnlCommand.PARAM, params);

			// execute
			return exec(context, getTargetCommand());

		} catch (Exception e) {
			log.error("execute failed", e);
		}

		// End execution
		log.info("- In Tree command -" + this.getClass().toString() + "- End");
		return true;
	}

	public boolean exec(Context context, String command) {
		try {
			new CommandsMap().getCommand(command).execute(context);
		} catch (Exception e) {
			log.error("execute command " + command + " failed ", e);
			return false;
		}
		return true;
	}

	public abstract HashMap translateParam(HashMap hashmap);

	public abstract String getTargetCommand();
}
