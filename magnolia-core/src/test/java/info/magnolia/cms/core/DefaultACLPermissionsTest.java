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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.logging.AuditLoggingManager;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.Path.Element;
import org.junit.Test;

public class DefaultACLPermissionsTest extends MgnlTestCase {

    private final SessionImpl session = mock(SessionImpl.class);
    private final List<info.magnolia.cms.security.Permission> list = new LinkedList<info.magnolia.cms.security.Permission>();
    private final Element rootElement = mock(Element.class);
    private final Element pageElement = mock(Element.class);
    private final Element contentNodeElement = mock(Element.class);
    private final Element[] elements = new Element[]{pageElement,contentNodeElement};
    private final Path itemPath = mock(Path.class);
    private final Path ancestorPath = mock(Path.class);
    private final Name rootName = mock(Name.class);
    private final Name pageName = mock(Name.class);
    private final ItemId itemId = mock(ItemId.class);
    private final PermissionImpl permissionImpl = new PermissionImpl();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        AuditLoggingManager auditLoggingManager = new AuditLoggingManager();
        ComponentsTestUtil.setInstance(AuditLoggingManager.class, auditLoggingManager);
        UrlPattern urlPattern = mock(UrlPattern.class);
        list.add(permissionImpl);
        permissionImpl.setPattern(urlPattern);

        HierarchyManager hm = MockUtil.createAndSetHierarchyManager(RepositoryConstants.WEBSITE, "");
        hm.createContent("/", "", MgnlNodeType.NT_PAGE);
        Content page = hm.createContent("/", "page", MgnlNodeType.NT_PAGE);
        Content contentNode = hm.createContent("/page", "contentNode", MgnlNodeType.NT_CONTENTNODE);

        when(session.nodeExists("page/contentNode")).thenReturn(true);
        when(session.getNode("/")).thenReturn(page.getJCRNode());
        when(session.getNode("page")).thenReturn(page.getJCRNode());
        when(session.getNode("page/contentNode")).thenReturn(contentNode.getJCRNode());

        when(itemPath.getAncestor(0)).thenReturn(itemPath);
        when(itemPath.getAncestor(1)).thenReturn(ancestorPath);

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
    public void testCanReadOnPageNode() throws RepositoryException {
        //GIVEN
        UrlPattern urlPattern = new SimpleUrlPattern("page");
        permissionImpl.setPattern(urlPattern);
        permissionImpl.setPermissions(info.magnolia.cms.security.Permission.READ);

        Map<Object, Object> configuration = new HashMap<Object, Object>();
        configuration.put("permissionsClass", "info.magnolia.cms.core.NodeTypeBasedPermissions");
        configuration.put("nodeType", MgnlNodeType.NT_PAGE);

        when(itemPath.getElements()).thenReturn(new Element[]{pageElement});

        DefaultACLBasedPermissions permissions = new DefaultACLBasedPermissions(list, session, configuration);

        //WHEN
        boolean access = permissions.canRead(itemPath, itemId);
        //THEN
        assertTrue(access);
    }

    @Test
    public void testCanReadOnContentNode() throws RepositoryException {
        //GIVEN
        UrlPattern urlPattern = new SimpleUrlPattern("page");
        permissionImpl.setPattern(urlPattern);
        permissionImpl.setPermissions(info.magnolia.cms.security.Permission.READ);

        Map<Object, Object> configuration = new HashMap<Object, Object>();
        configuration.put("permissionsClass", "info.magnolia.cms.core.NodeTypeBasedPermissions");
        configuration.put("nodeType", MgnlNodeType.NT_PAGE);

        when(itemPath.getElements()).thenReturn(new Element[]{pageElement, contentNodeElement});

        DefaultACLBasedPermissions permissions = new DefaultACLBasedPermissions(list, session, configuration);

        //WHEN
        boolean access = permissions.canRead(itemPath, itemId);
        //THEN
        assertFalse(access);
    }

    @Test
    public void testCanReadRoot() throws RepositoryException {
        //GIVEN
        UrlPattern urlPattern = new SimpleUrlPattern("/*");
        permissionImpl.setPattern(urlPattern);
        permissionImpl.setPermissions(info.magnolia.cms.security.Permission.READ);

        Map<Object, Object> configuration = new HashMap<Object, Object>();
        configuration.put("permissionsClass", "info.magnolia.cms.core.NodeTypeBasedPermissions");
        configuration.put("nodeType", MgnlNodeType.NT_PAGE);

        when(itemPath.getElements()).thenReturn(new Element[]{rootElement});

        DefaultACLBasedPermissions permissions = new DefaultACLBasedPermissions(list, session, configuration);

        //WHEN
        boolean access = permissions.canRead(itemPath, itemId);
        //THEN
        assertTrue(access);
    }

    @Test
    public void testGrantsOnPageNode() throws RepositoryException {
        //GIVEN
        UrlPattern urlPattern = new SimpleUrlPattern("page");
        Map<Object, Object> configuration = new HashMap<Object, Object>();
        configuration.put("permissionsClass", "info.magnolia.cms.core.NodeTypeBasedPermissions");
        configuration.put("nodeType", MgnlNodeType.NT_PAGE);

        when(itemPath.getElements()).thenReturn(new Element[]{pageElement});

        DefaultACLBasedPermissions permissions = new DefaultACLBasedPermissions(list, session, configuration);
        long convertedPermissions = permissions.convertJackrabbitPermissionsToMagnoliaPermissions(org.apache.jackrabbit.core.security.authorization.Permission.READ);
        permissionImpl.setPattern(urlPattern);
        permissionImpl.setPermissions(convertedPermissions);

        //WHEN
        boolean access = permissions.grants(itemPath, org.apache.jackrabbit.core.security.authorization.Permission.READ);
        //THEN
        assertTrue(access);
    }

    @Test
    public void testGrantsOnContentNode() throws RepositoryException {
        //GIVEN
        UrlPattern urlPattern = new SimpleUrlPattern("page");
        Map<Object, Object> configuration = new HashMap<Object, Object>();
        configuration.put("permissionsClass", "info.magnolia.cms.core.NodeTypeBasedPermissions");
        configuration.put("nodeType", MgnlNodeType.NT_PAGE);

        when(itemPath.getElements()).thenReturn(new Element[]{pageElement, contentNodeElement});

        DefaultACLBasedPermissions permissions = new DefaultACLBasedPermissions(list, session, configuration);
        long convertedPermissions = permissions.convertJackrabbitPermissionsToMagnoliaPermissions(org.apache.jackrabbit.core.security.authorization.Permission.READ);
        permissionImpl.setPattern(urlPattern);
        permissionImpl.setPermissions(convertedPermissions);

        //WHEN
        boolean access = permissions.grants(itemPath, org.apache.jackrabbit.core.security.authorization.Permission.READ);
        //THEN
        assertFalse(access);
    }

    @Test
    public void testGrantsHigherPermissionThenAssigned() throws RepositoryException {
        //GIVEN
        UrlPattern urlPattern = new SimpleUrlPattern("page");
        Map<Object, Object> configuration = new HashMap<Object, Object>();
        configuration.put("nodeType", MgnlNodeType.NT_PAGE);

        when(itemPath.getElements()).thenReturn(new Element[]{pageElement});

        DefaultACLBasedPermissions permissions = new DefaultACLBasedPermissions(list, session, configuration);
        long convertedPermissions = permissions.convertJackrabbitPermissionsToMagnoliaPermissions(org.apache.jackrabbit.core.security.authorization.Permission.READ);
        permissionImpl.setPattern(urlPattern);
        permissionImpl.setPermissions(convertedPermissions);

        //WHEN
        boolean access = permissions.grants(itemPath, org.apache.jackrabbit.core.security.authorization.Permission.ALL);
        //THEN
        assertFalse(access);
    }
}
