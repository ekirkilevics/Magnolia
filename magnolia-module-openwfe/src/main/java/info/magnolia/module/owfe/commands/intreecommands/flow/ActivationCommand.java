package info.magnolia.module.owfe.commands.intreecommands.flow;

import info.magnolia.module.owfe.commands.MgnlCommand;
import info.magnolia.module.owfe.commands.intreecommands.AbstractInTreeCommand;

import java.util.HashMap;

public class ActivationCommand extends AbstractInTreeCommand {

	public String getTargetCommand() {
		return "flowActivate";
	}

	public HashMap translateParam(HashMap param) {
		HashMap params = new HashMap();
		log.info("param = " + param);
		log.info("params = " + params);
		log.info("param.get(MgnlCommand.P_PATH)=" + param.get(MgnlCommand.P_PATH));
		params.put(MgnlCommand.P_PATH, param.get(MgnlCommand.P_PATH));
		params.put(MgnlCommand.P_RECURSIVE, MgnlCommand.P_RECURSIVE);
		return params;
	}

}