/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.commands.chain;

import info.magnolia.context.Context;

import java.util.Collection;
import java.util.Iterator;


/**
 * Replacement for org.apache.commons.chain.impl.ChainBase.
 * 
 */
public class ChainBase implements Chain {

    // ----------------------------------------------------------- Constructors

    public ChainBase() {
    }

    public ChainBase(Command command) {
        addCommand(command);
    }

    public ChainBase(Command[] commands) {
        if (commands == null) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < commands.length; i++) {
            addCommand(commands[i]);
        }
    }

    public ChainBase(Collection commands) {
        if (commands == null) {
            throw new IllegalArgumentException();
        }
        Iterator elements = commands.iterator();
        while (elements.hasNext()) {
            addCommand((Command) elements.next());
        }
    }

    // ----------------------------------------------------- Instance Variables

    protected Command[] commands = new Command[0];

    protected boolean frozen = false;

    // ---------------------------------------------------------- Chain Methods

    public void addCommand(Command command) {
        if (command == null) {
            throw new IllegalArgumentException();
        }
        if (frozen) {
            throw new IllegalStateException();
        }
        Command[] results = new Command[commands.length + 1];
        System.arraycopy(commands, 0, results, 0, commands.length);
        results[commands.length] = command;
        commands = results;
    }

    public boolean execute(Context context) throws Exception {
        // Verify our parameters
        if (context == null) {
            throw new IllegalArgumentException();
        }
        // Freeze the configuration of the command list
        frozen = true;
        // Execute the commands in this list until one returns true
        // or throws an exception
        boolean saveResult = false;
        Exception saveException = null;
        int i = 0;
        int n = commands.length;
        for (i = 0; i < n; i++) {
            try {
                saveResult = commands[i].execute(context);
                if (saveResult) {
                    break;
                }
            } catch (Exception e) {
                saveException = e;
                break;
            }
        }
        // Call postprocess methods on Filters in reverse order
        if (i >= n) { // Fell off the end of the chain
            i--;
        }
        boolean handled = false;
        boolean result = false;
        for (int j = i; j >= 0; j--) {
            if (commands[j] instanceof Filter) {
                try {
                    result = ((Filter) commands[j]).postprocess(context, saveException);
                    if (result) {
                        handled = true;
                    }
                } catch (Exception e) {
                    // Silently ignore
                }
            }
        }
        // Return the exception or result state from the last execute()
        if ((saveException != null) && !handled) {
            throw saveException;
        } else {
            return (saveResult);
        }
    }

    // -------------------------------------------------------- Package Methods

    Command[] getCommands() {
        return (commands);
    }
}
