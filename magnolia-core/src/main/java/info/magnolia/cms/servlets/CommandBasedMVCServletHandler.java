/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.runtime.Context;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.commands.CommandsManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.chain.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This implementation tries first to get a command form it's command catalogue. If none is found it will call the
 * execute method of the default MVCServletHandlerImpl, which tries to execute through reflection a related method.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public abstract class CommandBasedMVCServletHandler extends MVCServletHandlerImpl {

    /**
     * @param name
     * @param request
     * @param response
     */
    protected CommandBasedMVCServletHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        this.setCatalogueName(name);
    }

    /**
     * Try to get the command from this catalogue
     */
    private String catalogueName;

    /**
     * The logger use in this class
     */
    private static Logger log = LoggerFactory.getLogger(CommandBasedMVCServletHandler.class);

    /**
     * Try to get the command from the catalogue
     */
    public String execute(String commandName) {
        // get command from command map in JCR repository
        Command command = findCommand(commandName);
        if (command == null) { // not found, do in the old ways
            if (log.isDebugEnabled()) {
                log.debug("can not find command named " + commandName + " in tree command map");
            }
            return super.execute(commandName);
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("found command for " + commandName + ": " + command);
            }

            // now prepare the context
            Context ctx = getCommandContext(commandName);
            
            // execute the command
            try{
                command.execute(ctx);
            }
            catch(Exception e){
                log.error("can't execute command", e);
            }
            return getViewNameAfterExecution(commandName, ctx);
        }
    }

    /**
     * Default implemenation returns the commandName itself
     * @param commandName
     * @param ctx
     * @return the view name returned by this execution
     */
    private String getViewNameAfterExecution(String commandName, Context ctx) {
        return commandName;
    }

    /**
     * Used to get the command object
     * @param commandName
     * @return the callable command object
     */
    protected Command findCommand(String commandName) {
        return CommandsManager.getInstance().getCommand(this.getCatalogueName(), commandName);
    }

    /**
     * The default implementation returns the current context
     * @param commandName the name of the command to be called
     * @return the context to pass to the command
     */
    protected Context getCommandContext(String commandName) {
        return MgnlContext.getInstance();
    }

    /**
     * @return Returns the catalogueName.
     */
    public String getCatalogueName() {
        return this.catalogueName;
    }

    /**
     * @param catalogueName The catalogueName to set.
     */
    public void setCatalogueName(String catalogueName) {
        this.catalogueName = catalogueName;
    }
}
