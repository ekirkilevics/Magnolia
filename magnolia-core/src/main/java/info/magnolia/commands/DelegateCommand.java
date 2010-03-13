/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
