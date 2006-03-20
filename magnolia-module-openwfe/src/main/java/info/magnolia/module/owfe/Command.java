package info.magnolia.module.owfe;

import java.util.HashMap;

public interface Command {
	final static public String P_WORKITEM = "workItem";
	final static public String P_REQUEST = "request";
	final static public String P_RESULT = "__RESULT__";
	//public String execute();
	public boolean execute(HashMap params);
}
