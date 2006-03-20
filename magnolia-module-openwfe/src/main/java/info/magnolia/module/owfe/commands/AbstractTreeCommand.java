package info.magnolia.module.owfe.commands;

import org.apache.log4j.Logger;

import java.util.HashMap;

public abstract class AbstractTreeCommand implements Command {
    private static Logger log = Logger.getLogger(AbstractTreeCommand.class);
    HashMap params = new HashMap();

}
