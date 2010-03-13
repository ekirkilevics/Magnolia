/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.MgnlContext;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * Set the current user (info.magnolia.cms.security.User) into a pageContext variable.
 * @jsp.tag name="user" body-content="empty"
 *
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class UserTag extends TagSupport {

    /**
     * Name of the pagecontext variable where the user will be set.
     */
    private String var;

    /**
     * Determines if anonymous users should be taken into account.
     */
    private boolean anonymous;

    /**
     * Display anonymous users as "anonymous". Default to false (variable will not be set for anonymous users)
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    /**
     * The current user will be set to the pageContext variable with this name.
     * @jsp.attribute required="true" rtexprvalue="true"
     */
    public void setVar(String var) {
        this.var = var;
    }

    public int doEndTag() throws JspException {

        User user = MgnlContext.getUser();
        if (user != null && (anonymous || !UserManager.ANONYMOUS_USER.equals(user.getName()))) {
            pageContext.setAttribute(var, user);
        }
        else {
            pageContext.removeAttribute(var);
        }

        return super.doEndTag();
    }

    public void release() {
        super.release();
        this.var = null;
        this.anonymous = false;
    }

}
