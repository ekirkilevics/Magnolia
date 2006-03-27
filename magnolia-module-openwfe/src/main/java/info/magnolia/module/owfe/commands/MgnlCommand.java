package info.magnolia.module.owfe.commands;

import org.apache.commons.chain.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface MgnlCommand extends Command {
    final static public String PARAMS = "__params__";

    final static public String INFLOW_PARAM = "workItem";
    final static public String INTREE_PARAM = "treeParam";
    final static public String PARAM = "param";
    
    //final static public String P_WORKITEM = "workItem";
    final static public String P_REQUEST = "request";
    final static public String P_RESULT = "__RESULT__";
    final static public String P_PATH = "pathSelected";
    final static public String P_RECURSIVE = "recursive";
    final static public String P_ACTION = "action";
    final static public String P_WORKFLOW_DEFINITION_URL = "field:__definition__";
    final static public String P_DEFINITION = "__definition__";
    final static public String P_MAILTO = "mailTo";

    final static public String P_DEFAULT_ACTIVATION_FLOW = "webActivation";
    final static public String P_DEFAULT_DEACTIVATION_FLOW = "webDeactivation";
    final static public String P_DEFAULT_SCHEDULEDACTIVATION_FLOW = "webScheduledActivation";
    

    final static public String REPOSITORY = "website";


    static Logger log = LoggerFactory.getLogger(MgnlCommand.class);

}
