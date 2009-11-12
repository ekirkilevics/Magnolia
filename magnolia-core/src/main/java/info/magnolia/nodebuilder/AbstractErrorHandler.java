/**
 * This file Copyright (c) 2009 Magnolia International
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
package info.magnolia.nodebuilder;

import info.magnolia.cms.core.Content;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractErrorHandler implements ErrorHandler {

    // We currently can't differentiate between properties and nodes removal,
    // since we have a single operation for removal. Likewise, we can't assume
    // ItemExistsException is only thrown when a property already exists
    // (we throw it ourselves in info.magnolia.nodebuilder.Ops#addProperty,
    // but it might be thrown on other occasions too.
    // PathNotFoundException and ItemNotFoundException are also not clearly
    // distinguished, for similar reasons.
    // Moreover, while we're consistent in the Ops code, there's no guarantee
    // (neither spec nor api) that, for instance, an ItemExistsException is
    // thrown with the item's name as it's sole message (and unfortunately we
    // rely on this here below)
    public void handle(RepositoryException e, Content context) throws NodeOperationException {
        if (e instanceof ItemExistsException) {
            report(e.getMessage()  + " already exists at " + context.getHandle() + ".");
        } else if (e instanceof ItemNotFoundException) {
            report(e.getMessage()  + " can't be found at " + context.getHandle() + ".");
        } else if (e instanceof PathNotFoundException) {
            report(e.getMessage()  + " can't be found at " + context.getHandle() + ".");
        } else {
            unhandledRepositoryException(e, context);
        }
    }

    /**
     * Override this method if you need finer grained control on RepositoryExceptions that haven't been handled
     * by the handle() method yet, or if you want to try and keep on proceeding anyway.
     */
    protected void unhandledRepositoryException(RepositoryException e, Content context) throws NodeOperationException{
        throw new NodeOperationException("Failed to operate on " + context.getHandle() + " with message: " + e.getMessage(), e);
    }
}