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

/**
 * A RuntimeException thrown by ErrorHandler implementations;
 * clients of NodeBuilder should expect and handle this.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class NodeOperationException extends RuntimeException {
    // Implementation note: the choice of making this a RuntimeException is arguable,
    // but driven by the fact that NodeOperation implementations should *not* throw
    // this exception directly (it should only be thrown by ErrorHandlers.
    // While they obviously still can, here's hoping that they won't be tempted to do
    // so. If it were a checked exception, having this exception in the throws clause
    // might make it more tempting to throw them directly.
    // TODO : scratch the above and solve the problem !
    public NodeOperationException(String message) {
        super(message);
    }

    public NodeOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeOperationException(Throwable cause) {
        super(cause);
    }
}
