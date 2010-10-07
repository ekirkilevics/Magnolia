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
package info.magnolia.nodebuilder;

import info.magnolia.cms.core.Content;

import javax.jcr.RepositoryException;

/**
 * ErrorHandler implementations can decide what to do with certain conditions.
 * Specifically, they'll usually log or throw exceptions. They can only throw
 * NodeOperationException (or other RuntimeExceptions, obviously), which
 * the client code is expected to handle.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface ErrorHandler {

    /**
     * The operation calling this method is expected to pass a fully formed message;
     * it should ideally contain some context information about the operation that
     * caused an issue. The ErrorHandler implementation will decide what to do
     * with it. (log, throw a NodeOperationException, ...)
     */
    void report(String message) throws NodeOperationException;

    /**
     * The operation calling this method isn't expected to do anything here;
     * the ErrorHandler implementation will attempt to build a fully formed message,
     * then decide what to do with it. (log, throw a NodeOperationException, ...)
     */
    void handle(RepositoryException e, Content context) throws NodeOperationException;

}
