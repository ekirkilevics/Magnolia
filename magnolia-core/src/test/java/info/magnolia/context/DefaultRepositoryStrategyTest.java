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
package info.magnolia.context;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.security.auth.PrincipalCollection;
import info.magnolia.jcr.registry.SessionProviderRegistry;
import info.magnolia.test.RepositoryTestCase;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Session;

import org.junit.Test;

/**
 * @version $Id$
 */
public class DefaultRepositoryStrategyTest extends RepositoryTestCase {

    @Test
    public void testAccessManagers() {
        UserContext context = createMock(UserContext.class);
        SessionProviderRegistry registry = createMock(SessionProviderRegistry.class);
        replay(context, registry);
        DefaultRepositoryStrategy strategy = new DefaultRepositoryStrategy(registry, context);
        // Access Manager is no longer used and supported!
        AccessManager accessManager = strategy.getAccessManager("repo1", "space1");
        assertNull(accessManager);
        verify(context, registry);
    }

    @Test
    public void testRepositorySessions() throws Exception {
        UserContext context = createMock(UserContext.class);
        SessionProviderRegistry registry = createMock(SessionProviderRegistry.class);
        DefaultRepositoryStrategy strategy = new DefaultRepositoryStrategy(registry, context);
        Session session = strategy.getSession("magnolia", "website");
        assertNotNull(session);
        strategy.release();
    }

    @Test
    public void testQueryManagers() {
        UserContext context = createMock(UserContext.class);
        SessionProviderRegistry registry = createMock(SessionProviderRegistry.class);

        User user = createMock(User.class);
        expect(context.getUser()).andReturn(user).anyTimes();
        expect(user.getName()).andReturn("admin").anyTimes();
        replay(context,user, registry);
        DefaultRepositoryStrategy strategy = new DefaultRepositoryStrategy(registry, context);
        QueryManager queryManager = strategy.getQueryManager("magnolia", "website");
        assertNotNull(queryManager);
        verify(context, registry);
    }

    @Test
    public void testHierarchyManagers() {
        UserContext context = createMock(UserContext.class);
        User user = createMock(User.class);
        SessionProviderRegistry registry = createMock(SessionProviderRegistry.class);

        Set<Principal> principalSet = new HashSet<Principal>();
        PrincipalCollection principals = createMock(PrincipalCollection.class);
        principalSet.add(principals);
        ACL acl = createMock(ACL.class);
        expect(context.getUser()).andReturn(user).anyTimes();
        expect(user.getName()).andReturn("admin").anyTimes();
        expect(principals.get("magnolia_website")).andReturn(acl).anyTimes();
        expect(acl.getList()).andReturn(new ArrayList<Permission>()).anyTimes();
        replay(context, user, registry, principals, acl);

        DefaultRepositoryStrategy strategy = new DefaultRepositoryStrategy(registry, context);
        HierarchyManager hierarchyManager = strategy.getHierarchyManager("magnolia", "website");
        assertNotNull(hierarchyManager);
        verify(context, user, registry, principals, acl);
    }
}
