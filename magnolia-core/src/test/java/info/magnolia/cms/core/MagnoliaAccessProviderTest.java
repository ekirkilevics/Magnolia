/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.cms.core;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.ACLImpl;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.logging.AuditLoggingManager;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.observation.ObservationManager;
import javax.jcr.security.AccessControlManager;

import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.api.security.authorization.PrivilegeManager;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.core.security.authorization.AccessControlConstants;
import org.apache.jackrabbit.core.security.authorization.CompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.PrivilegeManagerImpl;
import org.apache.jackrabbit.core.security.authorization.PrivilegeRegistry;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.Path.Element;
import org.junit.Test;

public class MagnoliaAccessProviderTest extends MgnlTestCase {

    private final Session systemSession = mock(SessionImpl.class);
    private final JackrabbitWorkspace workspace = mock(JackrabbitWorkspace.class);
    private final PrivilegeRegistry privilegeRegistry = new PrivilegeRegistry(null);
    private final PrivilegeManager privilegeManager = new PrivilegeManagerImpl(privilegeRegistry, null);
    private final NodeImpl rootNode = mock(NodeImpl.class);
    private final ObservationManager observationManager = mock(ObservationManager.class);
    private final ValueFactory valueFactory = mock(ValueFactory.class);
    private final PrincipalManager principalManager = mock(PrincipalManager.class);
    private final AccessManager accessManager = mock(AccessManager.class);
    private final AccessControlManager accessControlManager = mock(AccessControlManager.class);
    private final List<info.magnolia.cms.security.Permission> list = new LinkedList<info.magnolia.cms.security.Permission>();
    private final Element rootElement = mock(Element.class);
    private final Element pageElement = mock(Element.class);
    private final Element contentNodeElement = mock(Element.class);
    private final Element[] elements = new Element[] { pageElement, contentNodeElement };
    private final Path itemPath = mock(Path.class);
    private final Path ancestorPath = mock(Path.class);
    private final Name rootName = mock(Name.class);
    private final Name pageName = mock(Name.class);
    private final PermissionImpl permissionImpl = new PermissionImpl();
    Principal principal = new ACLImpl(RepositoryConstants.WEBSITE, list);
    Set<Principal> principals = new HashSet<Principal>();

    @Override
    public void setUp() throws Exception {
        super.setUp();

        AuditLoggingManager auditLoggingManager = new AuditLoggingManager();
        ComponentsTestUtil.setInstance(AuditLoggingManager.class, auditLoggingManager);

        HierarchyManager hm = MockUtil.createAndSetHierarchyManager(RepositoryConstants.WEBSITE, "");
        hm.createContent("/", "", MgnlNodeType.NT_PAGE);
        Content page = hm.createContent("/", "page", MgnlNodeType.NT_PAGE);
        Content contentNode = hm.createContent("/", "contentNode", MgnlNodeType.NT_CONTENTNODE);

        when(systemSession.getWorkspace()).thenReturn(workspace);
        when(systemSession.getRootNode()).thenReturn(rootNode);
        when(systemSession.getValueFactory()).thenReturn(valueFactory);
        when(systemSession.getAccessControlManager()).thenReturn(accessControlManager);
        when(systemSession.nodeExists("page/contentNode")).thenReturn(true);
        when(systemSession.getNode("/")).thenReturn(page.getJCRNode());
        when(systemSession.getNode("page")).thenReturn(page.getJCRNode());
        when(systemSession.getNode("page/contentNode")).thenReturn(contentNode.getJCRNode());
        when(((SessionImpl) systemSession).getJCRPath(null)).thenReturn("");
        when(((SessionImpl) systemSession).getPrincipalManager()).thenReturn(principalManager);
        when(((SessionImpl) systemSession).getAccessManager()).thenReturn(accessManager);

        when(workspace.getPrivilegeManager()).thenReturn(privilegeManager);
        when(workspace.getObservationManager()).thenReturn(observationManager);
        when(workspace.getName()).thenReturn(RepositoryConstants.WEBSITE);

        when(rootNode.hasNode(AccessControlConstants.N_ACCESSCONTROL)).thenReturn(true);
        when(rootNode.getNode(AccessControlConstants.N_ACCESSCONTROL)).thenReturn(rootNode);
        when(rootNode.isNodeType(AccessControlConstants.NT_REP_ACCESS_CONTROL)).thenReturn(true);
        // when(rootNode.getSession()).thenReturn(systemSession);
        // when(rootNode.sessionContext).thenReturn();

        when(principalManager.getEveryone()).thenReturn(principal);
        // when(principalManager.hasPrincipal("website")).thenReturn(true);
        // when(accessManager.isGranted((Path)anyObject(),(Name)anyObject(),anyInt())).thenReturn(true);

        UrlPattern urlPattern = mock(UrlPattern.class);
        list.add(permissionImpl);
        principals.add(principal);
        permissionImpl.setPattern(urlPattern);

        when(itemPath.getElements()).thenReturn(elements);
        when(itemPath.getAncestor(0)).thenReturn(itemPath);
        when(itemPath.getAncestor(1)).thenReturn(ancestorPath);
        when(itemPath.getElements()).thenReturn(new Element[] { pageElement, contentNodeElement });
        when(ancestorPath.getElements()).thenReturn(new Element[] { pageElement });

        when(rootElement.getName()).thenReturn(rootName);
        when(rootElement.denotesRoot()).thenReturn(true);
        when(pageElement.getName()).thenReturn(pageName);
        when(pageElement.denotesIdentifier()).thenReturn(true);
        when(pageElement.getString()).thenReturn("page");
        when(contentNodeElement.getName()).thenReturn(pageName);
        when(contentNodeElement.denotesIdentifier()).thenReturn(true);
        when(contentNodeElement.getString()).thenReturn("contentNode");
    }

    @Test
    public void testPermissionClassNotDefined() throws RepositoryException {
        // GIVEN
        MagnoliaAccessProvider provider = new MagnoliaAccessProvider();
        Map<Object, Object> configuration = new HashMap<Object, Object>();
        provider.init(systemSession, configuration);
        when(itemPath.getElements()).thenReturn(new Element[] { pageElement, contentNodeElement });
        // WHEN
        CompiledPermissions permissions = provider.compilePermissions(principals);
        // THEN
        assertTrue(permissions instanceof DefaultACLBasedPermissions);
    }

    @Test
    public void testPermissionClassDefined() throws RepositoryException {
        // GIVEN
        MagnoliaAccessProvider provider = new MagnoliaAccessProvider();
        Map<Object, Object> configuration = new HashMap<Object, Object>();
        configuration.put("permissionsClass", "info.magnolia.cms.core.NodeTypeBasedPermissions");
        // WHEN
        provider.init(systemSession, configuration);
        CompiledPermissions permissions = provider.compilePermissions(principals);
        // THEN
        assertTrue(permissions instanceof NodeTypeBasedPermissions);
    }

    @Test
    public void testPermissionNonexistingClassDefined() throws RepositoryException {
        // GIVEN
        MagnoliaAccessProvider provider = new MagnoliaAccessProvider();
        Map<Object, Object> configuration = new HashMap<Object, Object>();
        configuration.put("permissionsClass", "info.magnolia.cms.core.someNonexistingClass");
        // WHEN
        provider.init(systemSession, configuration);
        CompiledPermissions permissions = provider.compilePermissions(principals);
        // THEN
        assertTrue(permissions instanceof DefaultACLBasedPermissions);
    }

    @Test
    public void testPermissionWrongClassDefined() throws RepositoryException {
        // GIVEN
        MagnoliaAccessProvider provider = new MagnoliaAccessProvider();
        Map<Object, Object> configuration = new HashMap<Object, Object>();
        configuration.put("permissionsClass", "info.magnolia.cms.core.MagnoliaAccessProvider");
        // WHEN
        provider.init(systemSession, configuration);
        CompiledPermissions permissions = provider.compilePermissions(principals);
        // THEN
        assertTrue(permissions instanceof DefaultACLBasedPermissions);
    }

    @Test
    public void testPermissionWrongClassConstructor() throws RepositoryException {
        // GIVEN
        MagnoliaAccessProvider provider = new MagnoliaAccessProvider();
        Map<Object, Object> configuration = new HashMap<Object, Object>();
        configuration.put("permissionsClass", "info.magnolia.cms.core.RootOnlyPermissions");
        // WHEN
        provider.init(systemSession, configuration);
        CompiledPermissions permissions = provider.compilePermissions(principals);
        // THEN
        assertTrue(permissions instanceof DefaultACLBasedPermissions);
    }
}
