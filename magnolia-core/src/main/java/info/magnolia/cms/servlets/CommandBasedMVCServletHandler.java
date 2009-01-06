/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.servlets;

import info.magnolia.cms.util.AlertUtil;
import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

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

        if (log.isDebugEnabled()) {
            log.debug("found command for " + commandName + ": " + command);
        }

        // now prepare the context
        Context ctx = getCommandContext(commandName);

        // execute the command
        try {
            command.execute(ctx);
        }
        catch (Exception e) {
            log.error("can't execute command", e);
            AlertUtil.setException(e);
        }
        return getViewNameAfterExecution(commandName, ctx);
    }

    /**
     * Default implemenation returns the commandName itself
     * @param commandName
     * @param ctx
     * @return the view name returned by this execution
     */
    protected String getViewNameAfterExecution(String commandName, Context ctx) {
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
