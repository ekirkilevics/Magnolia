/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.security;

/**
 * To be used as a replacement of /server/security or SecuritySupportImpl in mgnl-beans.properties
 * in case the configuration is messed up. For instance, edit
 * <code>WEB-INF/config/default/magnolia.properties</code> and add
 * <pre>info.magnolia.cms.security.SecuritySupport=info.magnolia.cms.security.RescueSecuritySupport</pre>
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RescueSecuritySupport extends SecuritySupportBase {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RescueSecuritySupport.class);

    public RescueSecuritySupport() {
        super();
        log.warn("Using RescueSecuritySupport !");
    }

    public UserManager getUserManager() {
        log.warn("Using RescueSecuritySupport, will instanciate SystemUserManager, please fix your configuration !");
        return new SystemUserManager();
    }

    public UserManager getUserManager(String realmName) {
        log.warn("Using RescueSecuritySupport, will instanciate SystemUserManager, please fix your configuration !");
        return new SystemUserManager();
    }

    public GroupManager getGroupManager() {
        log.warn("Using RescueSecuritySupport, will instanciate MgnlGroupManager, please fix your configuration !");
        return new MgnlGroupManager();
    }

    public RoleManager getRoleManager() {
        log.warn("Using RescueSecuritySupport, will instanciate MgnlRoleManager, please fix your configuration !");
        return new MgnlRoleManager();
    }
}
