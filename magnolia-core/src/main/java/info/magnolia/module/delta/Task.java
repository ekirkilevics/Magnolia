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
package info.magnolia.module.delta;

import info.magnolia.module.InstallContext;

/**
 * A Task represents an atomic operation to be performed when installing,
 * updating or uninstalling a module, as part of a Delta.
 *
 * TODO : add mandatoryness ?
 *
 * @see info.magnolia.module.delta.Delta
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface Task {
    /**
     * Description of what has changed. Example: New ACL configuration
     */
    String getName();

    /**
     * Description what will happen during the execution. Example: "Bootstraps the new configuration for the ACL dialogs"
     */
    String getDescription();

    /**
     * A good-citizen task should execute itself responsibly: it should know
     * what to do in case of problems: for instance, fixable or irrelevant
     * issues should usually just be logged used the InstallContext methods,
     * when the user can fix them later on. The task could also potentially
     * do backups of nodes it needs to modify extensively, so the user could
     * refer to the backups in case of problem. In the event of an unrecoverable
     * issue, the Task could also throw a TaskExecutionException, knowing that
     * will cancel the whole module's installation, update and startup. If
     * a TaskExecutionException must be thrown, keep in mind that the exception
     * message will still be shown to the end user, so try to keep them human
     * readable.
     */
    void execute(InstallContext installContext) throws TaskExecutionException;
}
