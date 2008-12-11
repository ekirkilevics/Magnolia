/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.commands.MgnlRepositoryCatalog;
import info.magnolia.context.MgnlContext;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Handles shutdown coreTasks supplied by Providers. Can be used to cleanly shutdown repositories while stopping the web
 * application.
 * @author Fabrizio Giustina
 * @version $Id$
 *
 * @deprecated since 4.0: usage removed (modules should handle their own lifecycles since 3.5).
 */
public class ShutdownManager extends ObservedManager {
    private static final Logger log = LoggerFactory.getLogger(ShutdownManager.class);
    private static ShutdownManager instance = new ShutdownManager();

    public static ShutdownManager getInstance() {
        return instance;
    }

    /**
     * Tasks that the shutdown manager will execute.
     */
    private final List customTasks = new ArrayList();

    /**
     * List the shutdown task that the server will execute
     * @return <code>List</code> of
     */
    public List listShutdownTasks() {
        return Collections.unmodifiableList(customTasks);
    }

    /**
     * Executes the registered shutdown tasks.
     */
    public void execute() {
        log.info("Executing shutdown tasks");

        for (Iterator iter = listShutdownTasks().iterator(); iter.hasNext();) {
            Command task = (Command) iter.next();
            Context c = MgnlContext.getSystemContext();
            try {
                task.execute(c);
            }
            catch (Throwable e) {
                log.warn("Failed to execute shutdown task {0}: {1} {2}", new Object[]{task, e.getClass().getName(), e.getMessage()});
            }
        }
    }

    protected void onRegister(Content node) {
        Catalog mrc = new MgnlRepositoryCatalog(node);
        Iterator iter = mrc.getNames();
        while (iter.hasNext()) {
            Object element = iter.next();
            log.info("Adding shutdown task:" + element.toString());
            customTasks.add(0, mrc.getCommand((String) element)); // Last Registered First Executed
        }
    }

    protected void onClear() {
        customTasks.clear();
    }
}
