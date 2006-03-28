package info.magnolia.module.owfe.commands.intreecommands;

import info.magnolia.module.owfe.commands.CommandsMap;
import info.magnolia.module.owfe.commands.MgnlCommand;
import org.apache.commons.chain.Context;

import java.util.HashMap;

/**
 * The main goal of in tree commands is to traslate the tree context to generic
 * command context
 *
 * @author jackie
 */
public abstract class AbstractInTreeCommand extends MgnlCommand {

    /**
     * List of the parameters that this command needs to run
     *
     * @return a list of string describing the parameters needed. The parameters should have a  mapping in this class.
     */
    public String[] getExpectedParameters() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean execute(Context context) {
        HashMap param = (HashMap) context.get(INTREE_PARAM);
        if (log.isDebugEnabled())
            log.debug("- In Tree command -" + this.getClass().toString() + "- Start");
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
        if (log.isDebugEnabled())
            log.debug("- In Tree command -" + this.getClass().toString() + "- End");
        return true;
    }

    public boolean exec(Context context, String command) {
        try {
            CommandsMap.getCommand(command).execute(context);
        } catch (Exception e) {
            log.error("execute command " + command + " failed ", e);
            return false;
        }
        return true;
    }

    public boolean exec(HashMap param, Context ctx) {
        throw new RuntimeException("This should not be called here");
    }

    public abstract HashMap translateParam(HashMap hashmap);

    public abstract String getTargetCommand();
}
