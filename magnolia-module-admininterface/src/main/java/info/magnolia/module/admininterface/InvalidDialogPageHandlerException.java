/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.admininterface;

/**
 * Runtime Exception thrown when a dialog handler can't be instantiated.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class InvalidDialogPageHandlerException extends RuntimeException {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Instantiates a new exception. Use this constructor when configuration is missing.
     * @param pageName missing dialog name
     */
    public InvalidDialogPageHandlerException(String pageName) {
        super("No dialogpage handler for [" + pageName + "] found"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Instantiates a new exception. Use this constructor when the dialog handler can't be instantiated due to a
     * previous exception.
     * @param pageName dialog name
     * @param cause previous exception
     */
    public InvalidDialogPageHandlerException(String pageName, Throwable cause) {
        super("Unable to instantiate a dialogpage handler for [" //$NON-NLS-1$
            + pageName
            + "] due to a " //$NON-NLS-1$
            + cause.getClass().getName()
            + " exception", cause); //$NON-NLS-1$
    }

}
