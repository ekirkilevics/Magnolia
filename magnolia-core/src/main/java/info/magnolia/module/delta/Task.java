/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
    String getName();

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
