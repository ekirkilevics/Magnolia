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

import javax.jcr.RepositoryException;

/**
 * An abstract implementation of AbstractTask which simply
 * wraps the execute call in a try/catch block, avoiding
 * many verbose and irrelevant code lines in actual tasks,
 * leaving room for smarter and more interesting exception
 * handling, when they can actually be handled.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractRepositoryTask extends AbstractTask {

    public AbstractRepositoryTask(String name, String description) {
        super(name, description);
    }

    public void execute(InstallContext installContext) throws TaskExecutionException {
        try {
            doExecute(installContext);
        } catch (RepositoryException e) {
            throw new TaskExecutionException("Could not execute task: " + e.getMessage(), e);
        }
    }

    protected abstract void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException;
}
