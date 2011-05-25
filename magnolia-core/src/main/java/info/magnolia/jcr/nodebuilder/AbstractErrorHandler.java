/**
 * This file Copyright (c) 2009-2011 Magnolia International
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
package info.magnolia.jcr.nodebuilder;

import info.magnolia.nodebuilder.NodeOperationException;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

/**
 * Provides basic behavior for ErrorHandlers.
 *
 * @version $Id$
 */
public abstract class AbstractErrorHandler implements ErrorHandler {

    @Override
    public void handle(RepositoryException e, Node context) throws NodeOperationException, RepositoryException {
        if (e instanceof ItemExistsException) {
            report(e.getMessage() + " already exists at " + context.getPath() + ".");
        } else if (e instanceof ItemNotFoundException) {
            report(e.getMessage() + " can't be found at " + context.getPath() + ".");
        } else if (e instanceof PathNotFoundException) {
            report(e.getMessage() + " can't be found at " + context.getPath() + ".");
        } else {
            unhandledRepositoryException(e, context);
        }
    }

    /**
     * Override this method if you need finer grained control on RepositoryExceptions that haven't been handled
     * by the handle() method yet, or if you want to try and keep on proceeding anyway.
     */
    protected void unhandledRepositoryException(RepositoryException e, Node context) throws NodeOperationException, RepositoryException {
        throw new NodeOperationException("Failed to operate on " + context.getPath() + " with message: " + e.getMessage(), e);
    }
}
