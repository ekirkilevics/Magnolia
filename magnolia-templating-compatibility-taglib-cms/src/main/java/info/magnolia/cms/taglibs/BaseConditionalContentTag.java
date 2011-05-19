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
package info.magnolia.cms.taglibs;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * Base class for implementing conditional tags (e.g. {@link IfEmpty}). Subclasses need to implement the {@link BaseConditionalContentTag#condition()} method.
 * @author philipp
 * @version $Id$
 *
 */
public abstract class BaseConditionalContentTag extends BaseContentTag {

    /**
     * <p>Subclasses implement this method to compute the boolean result
     * of the conditional action. This method is invoked once per tag invocation
     * by <tt>doStartTag()</tt>.
     *
     * @return a boolean representing the condition that a particular subclass
     *   uses to drive its conditional logic.
     */
    protected abstract boolean condition() throws JspTagException;

    /**
     * Includes its body if <tt>condition()</tt> evaluates to true.
     */
    @Override
    public int doStartTag() throws JspException {

        // execute our condition() method once per invocation
        boolean result = condition();

        // handle conditional behavior
        if (result) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

}
