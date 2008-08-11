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
package info.magnolia.cms.util;

import javax.servlet.http.HttpServletRequest;


/**
 * A simple generic interface that can be used to configure anything that can be selected based on some conditions (i.e.
 * a different login page based on the request URI or host).
 * @author fgiust
 * @version $Id$
 */
public interface PatternDelegate {

    /**
     * Does the current request match the expected condition?
     * @param request HttpServletRequest
     * @return <code>true</code> if the current request matches the expected conditions
     */
    boolean match(HttpServletRequest request);

    /**
     * Returns the delegate that this bean holds. This method is usually called to retrieve the delegate object after
     * match() returns true.
     * @return delegate object
     */
    Object getDelegate();
}
