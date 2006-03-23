package info.magnolia.module.owfe.commands;

import org.apache.commons.chain.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ITreeCommand extends Command {
    final static public String PARAMS = "__params__";

    final static public String P_WORKITEM = "workItem";
    final static public String P_REQUEST = "request";
    final static public String P_RESULT = "__RESULT__";

    static Logger log = LoggerFactory.getLogger(ITreeCommand.class);


}
