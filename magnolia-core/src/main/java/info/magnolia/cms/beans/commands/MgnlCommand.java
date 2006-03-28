package info.magnolia.cms.beans.commands;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public abstract class MgnlCommand implements Command {
    final static public String PARAMS = "__params__";
    final static public String PARAM = "param";
    final static public String PREFIX_COMMAND = "command-";
    final static public int PREFIX_COMMAND_LEN = PREFIX_COMMAND.length();
    final static public String COMMAND_DELIM = "-";


    public static Logger log = LoggerFactory.getLogger(MgnlCommand.class);

    /**
     * List of the parameters that this command needs to run
     *
     * @return a list of string describing the parameters needed. The parameters should have a  mapping in this class.
     */
    public abstract String[] getExpectedParameters();

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
