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
package info.magnolia.context;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.security.auth.PrincipalCollection;
import info.magnolia.test.RepositoryTestCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Session;
import javax.security.auth.Subject;

/**
 *
 * @author ashapochka
 * @version $Revision: $ ($Author: $)
 */
public class DefaultRepositoryStrategyTest extends RepositoryTestCase {
    public void testAccessManagers() {
        UserContext context = createMock(UserContext.class);
        User user = createMock(User.class);        
        Set principalSet = new HashSet();
        PrincipalCollection principals = createMock(PrincipalCollection.class);
        principalSet.add(principals);
        ACL acl = createMock(ACL.class);        
        Subject subject = new Subject(false, principalSet, new HashSet(), new HashSet());                                
        expect(context.getUser()).andReturn(user);
        expect(user.getSubject()).andReturn(subject);        
        expect(principals.get("repo1_space1")).andReturn(acl);
        expect(acl.getList()).andReturn(new ArrayList());
        replay(context, user, principals, acl);
        DefaultRepositoryStrategy strategy = new DefaultRepositoryStrategy(context);		
        AccessManager accessManager = strategy.getAccessManager("repo1", "space1");
        assertNotNull(accessManager);
        assertSame(accessManager, strategy.getAccessManager("repo1", "space1"));
        verify(context, user, principals, acl);
    }
    
    public void testRepositorySessions() throws Exception {
        UserContext context = createMock(UserContext.class);
        DefaultRepositoryStrategy strategy = new DefaultRepositoryStrategy(context);
        Session session = strategy.getRepositorySession("magnolia", "website");
        assertNotNull(session);
        strategy.release();
    }
    
    public void testQueryManagers() {
        UserContext context = createMock(UserContext.class);
        User user = createMock(User.class);        
        Set principalSet = new HashSet();
        PrincipalCollection principals = createMock(PrincipalCollection.class);
        principalSet.add(principals);
        ACL acl = createMock(ACL.class);        
        Subject subject = new Subject(false, principalSet, new HashSet(), new HashSet());                                
        expect(context.getUser()).andReturn(user);        
        expect(user.getSubject()).andReturn(subject);        
        expect(principals.get("magnolia_website")).andReturn(acl);
        expect(acl.getList()).andReturn(new ArrayList());
        replay(context, user, principals, acl);
        DefaultRepositoryStrategy strategy = new DefaultRepositoryStrategy(context);		
        QueryManager queryManager = strategy.getQueryManager("magnolia", "website");
        assertNotNull(queryManager);        
        verify(context, user, principals, acl);
    }
    
    public void testHierarchyManagers() {
        UserContext context = createMock(UserContext.class);
        User user = createMock(User.class);        
        Set principalSet = new HashSet();
        PrincipalCollection principals = createMock(PrincipalCollection.class);
        principalSet.add(principals);
        ACL acl = createMock(ACL.class);        
        Subject subject = new Subject(false, principalSet, new HashSet(), new HashSet());                                
        expect(context.getUser()).andReturn(user).anyTimes();
        expect(user.getName()).andReturn("admin").anyTimes();
        expect(user.getSubject()).andReturn(subject).anyTimes();        
        expect(principals.get("magnolia_website")).andReturn(acl).anyTimes();
        expect(acl.getList()).andReturn(new ArrayList()).anyTimes();
        replay(context, user, principals, acl);
        DefaultRepositoryStrategy strategy = new DefaultRepositoryStrategy(context);		
        HierarchyManager hierarchyManager = strategy.getHierarchyManager("magnolia", "website");
        assertNotNull(hierarchyManager);        
        verify(context, user, principals, acl);
    }
}
