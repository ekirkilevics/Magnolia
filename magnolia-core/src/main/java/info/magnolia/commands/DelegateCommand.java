package info.magnolia.commands;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegate to an other command at runtime
 * @author Philipp Bracher
 * @version $Id$
 *
 */
public class DelegateCommand implements Command {
    /**
     * Log
     */
    Logger log = LoggerFactory.getLogger(DelegateCommand.class);

    /**
     * The command name used to delegate to
     */
    private String commandName;

    public DelegateCommand() {
    }

    /**
     * @param commandName
     * @deprecated not used
     */
    public DelegateCommand(String commandName) {
        this.commandName = commandName;
    }

    public boolean execute(Context ctx) throws Exception {
        Command cmd = CommandsManager.getInstance().getCommand(commandName);
        if(cmd != null){
            return cmd.execute(ctx);
        }
        else{
            log.error("can't find command {}", this.commandName);
        }
        return false;
    }


    public String getCommandName() {
        return this.commandName;
    }


    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

}
