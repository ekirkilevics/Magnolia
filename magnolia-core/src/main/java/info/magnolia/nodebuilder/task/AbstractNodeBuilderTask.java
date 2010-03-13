/**
 * This file Copyright (c) 2009-2010 Magnolia International
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
package info.magnolia.nodebuilder.task;

import info.magnolia.cms.core.Content;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.nodebuilder.ErrorHandler;
import info.magnolia.nodebuilder.NodeBuilder;
import info.magnolia.nodebuilder.NodeOperation;
import info.magnolia.nodebuilder.NodeOperationException;
import info.magnolia.nodebuilder.StrictErrorHandler;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractNodeBuilderTask extends AbstractRepositoryTask {
    private final ErrorHandling errorHandling;
    private final NodeOperation[] operations;

    public AbstractNodeBuilderTask(String name, String description, ErrorHandling errorHandling, NodeOperation... operations) {
        super(name, description);
        this.errorHandling = errorHandling;
        this.operations = operations;
    }

    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final Content root = getRootNode(ctx);
        final ErrorHandler errorHandler = newErrorHandler(ctx);
        final NodeBuilder nodeBuilder = new NodeBuilder(errorHandler, root, operations);
        try {
            nodeBuilder.exec();
        } catch (NodeOperationException e) {
            throw new TaskExecutionException(e.getMessage(), e.getCause());
        }
    }

    protected abstract Content getRootNode(InstallContext ctx) throws RepositoryException;


    protected ErrorHandler newErrorHandler(InstallContext ctx) {
        switch (errorHandling) {
            case logging:
                return new TaskLogErrorHandler(ctx);
            case strict:
                return new StrictErrorHandler();
        }
        throw new IllegalStateException("Improbable errorHandling: " + errorHandling);
    }
}
