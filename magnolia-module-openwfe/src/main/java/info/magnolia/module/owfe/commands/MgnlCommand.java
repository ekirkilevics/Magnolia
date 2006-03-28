package info.magnolia.module.owfe.commands;

import openwfe.org.engine.workitem.StringAttribute;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public abstract class MgnlCommand implements Command {
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
    final static public String P_OK = "OK";
    final static public String P_START_DATE = "startDate";
    final static public String P_END_DATE = "endDate";

    final static public String P_DEFAULT_ACTIVATION_FLOW = "webActivation";
    final static public String P_DEFAULT_DEACTIVATION_FLOW = "webDeactivation";
    final static public String P_DEFAULT_SCHEDULEDACTIVATION_FLOW = "webScheduledActivation";

    final static public String TRUE = "true";
    final static public String FALSE = "false";
    final static public StringAttribute ATT_TRUE = new StringAttribute(TRUE);
    final static public StringAttribute ATT_FALSE = new StringAttribute(FALSE);

    final static public String PREFIX_USER = "user-";
    final static public String PREFIX_GROUP = "group-";
    final static public String PREFIX_ROLE = "role-";
    final static public String PREFIX_COMMAND = "command-";

    final static public int PREFIX_USER_LEN = PREFIX_USER.length();
    final static public int PREFIX_GROUP_LEN = PREFIX_GROUP.length();
    final static public int PREFIX_ROLE_LEN = PREFIX_ROLE.length();
    final static public int PREFIX_COMAMND_LEN = PREFIX_COMMAND.length();

    final static public String COMMAND_DELIM = "-";


    final static public String REPOSITORY = "website";


    public static Logger log = LoggerFactory.getLogger(MgnlCommand.class);

    /**
     * List of the parameters that this command needs to run
     *
     * @return a list of string describing the parameters needed. The parameters should have a  mapping in this class.
     */
    public String[] getExpectedParameters() {
        return null;
    }

    /**
     * List of the parameters that this command is accepting
     *
     * @return a list of string describing the parameters excepted. The parameters should have a  mapping in this class.
     */
    public String[] getAcceptedParameters() {
        return null;
    }

    /**
     * Check the needed parameters are all there.
     *
     * @return true if the needed parameters are present
     */
    boolean checkParameters(Context context) {
        String[] params = this.getExpectedParameters();
        if (params == null)
            return true;
        HashMap map = (HashMap) context.get(PARAM);
        for (int i = 0; i < params.length; i++) {
            if (map.get(params[i]) == null)
                return false;
        }
        return true;
    }

    public boolean execute(Context context) {
        HashMap params = (HashMap) context.get(PARAMS);
        return exec(params, context);
    }

    public abstract boolean exec(HashMap param, Context ctx);

}
