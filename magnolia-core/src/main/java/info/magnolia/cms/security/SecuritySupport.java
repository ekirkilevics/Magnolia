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
package info.magnolia.cms.security;

import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.login.LoginResult;
import info.magnolia.objectfactory.Components;

/**
 * Entry point to get the various managers like {@link UserManager}, {@link GroupManager} and {@link RoleManager}.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface SecuritySupport {

    /**
     * Returns a generic UserManager, either for a default realm, or an implementation which delegates to other UserManager instances.
     */
    UserManager getUserManager();

    /**
     * Returns a UserManager for the given realm.
     */
    UserManager getUserManager(String realmName);

    GroupManager getGroupManager();

    RoleManager getRoleManager();

   /**
    * Performs an authentication using the {@link CredentialsCallbackHandler} to retriev the user name and password.
    */
    LoginResult authenticate(CredentialsCallbackHandler callbackHandler, String jaasModuleName);

    /**
     * Factory to retrieve the singleton instance.
     */
    public final static class Factory {
        public static SecuritySupport getInstance() {
            return Components.getSingleton(SecuritySupport.class);
        }
    }
}
