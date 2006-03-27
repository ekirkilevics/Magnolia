package info.magnolia.module.owfe.commands.intreecommands;

import info.magnolia.module.owfe.commands.MgnlCommand;

import java.util.HashMap;

public class DeactivationCommand {
	public String getTargetCommand() {
		return "realDeactivate";
	}

	public HashMap translateParam(HashMap param) {
		HashMap params = new HashMap();
		params.put(MgnlCommand.P_PATH, param.get(MgnlCommand.P_PATH));
		params.put(MgnlCommand.P_RECURSIVE, MgnlCommand.P_RECURSIVE);
		return params;
	}
}
