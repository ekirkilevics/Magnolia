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
package info.magnolia.cms.util;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.registry.SessionProviderRegistry;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;

import javax.inject.Singleton;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

/**
 * This class replaces SessionStore and provide generic methods to create Magnolia specific JCR-workspace access objects.
 * @see HierarchyManager
 * @see javax.jcr.Session
 * @see AccessManager
 * @see QueryManager
 * @author Sameer Charles
 * $Id$
 */
@Singleton
public class WorkspaceAccessUtil {

    public WorkspaceAccessUtil() {
    }

    /**
     * @deprecated since 4.5, use IoC.
     */
    @Deprecated
    public static WorkspaceAccessUtil getInstance() {
        return Components.getSingleton(WorkspaceAccessUtil.class);
    }

    /**
     * @return Default SimpleCredentials as configured in magnolia.properties
     * */
    public SimpleCredentials getDefaultCredentials() {
        User user = MgnlContext.getUser();
        if (user == null) {
            // there is no user logged in, so this is just a system call. Returned credentials are used only to access repository, but do not allow any access over Magnolia.
            return getAnonymousUserCredentials();
        }
        return new SimpleCredentials(user.getName(),user.getPassword().toCharArray());
    }

    public SimpleCredentials getCredentials(User user) {
        return new SimpleCredentials(user.getName(),user.getPassword().toCharArray());
    }

    public Session createRepositorySession(SimpleCredentials sc, String workspaceName) throws RepositoryException {
    	try {
			return Components.getComponent(SessionProviderRegistry.class).get(workspaceName).createSession(getAdminUserCredentials());
		} catch (RegistrationException e) {
			throw new RepositoryException(e);
		}
    }

    public Session createAdminRepositorySession(String workspaceName) throws RepositoryException {
    	return createRepositorySession(getAdminUserCredentials(), workspaceName);
    }

    protected SimpleCredentials getAdminUserCredentials() {
        // FIXME: stop using SystemProperty, but IoC is not ready yet when this is called (config loader calls repo.init() which results in authentication calls being made and this method being invoked
        String user = SystemProperty.getProperty("magnolia.connection.jcr.admin.userId", SystemProperty.getProperty("magnolia.connection.jcr.userId", "admin"));
        String pwd = SystemProperty.getProperty("magnolia.connection.jcr.admin.password", SystemProperty.getProperty("magnolia.connection.jcr.password", "admin"));
        return new SimpleCredentials(user, pwd.toCharArray());
    }

    protected SimpleCredentials getAnonymousUserCredentials() {
        // FIXME: stop using SystemProperty, but IoC is not ready yet when this is called (config loader calls repo.init() which results in authentication calls being made and this method being invoked
        // TODO: can also read it from the Login Module properties ... but WAU has no access to that
        String user = SystemProperty.getProperty("magnolia.connection.jcr.anonymous.userId", "anonymous");
        String pwd = SystemProperty.getProperty("magnolia.connection.jcr.anonymous.password", "anonymous");
        return new SimpleCredentials(user, pwd.toCharArray());
    }

}
