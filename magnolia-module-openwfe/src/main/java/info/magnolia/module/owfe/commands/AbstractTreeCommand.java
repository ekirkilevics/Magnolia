package info.magnolia.module.owfe.commands;

import org.apache.log4j.Logger;

import java.util.HashMap;

public abstract class AbstractTreeCommand {
    final static public String P_WORKITEM = "workItem";
    final static public String P_REQUEST = "request";
    final static public String P_RESULT = "__RESULT__";

    protected static Logger log = Logger.getLogger(AbstractTreeCommand.class);
    //HashMap params = new HashMap();


    public abstract boolean execute(HashMap params);

}
