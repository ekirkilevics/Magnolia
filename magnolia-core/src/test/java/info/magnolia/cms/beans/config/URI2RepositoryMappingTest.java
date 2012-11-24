/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.beans.config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.TestMagnoliaConfigurationProperties;
import info.magnolia.test.mock.MockRepositoryAcquiringStrategy;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * URI2RepositoryMappingTest.
 */
public class URI2RepositoryMappingTest {

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Before
    public void setUp() throws Exception {
        // some tests do not cleanup properly so we need to cleanup here before starting to be sure.
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        SystemProperty.setMagnoliaConfigurationProperties(new TestMagnoliaConfigurationProperties());
    }

    @Test
    public void testGetUri() throws Exception {
        // GIVEN
        ComponentsTestUtil.setInstance(ServerConfiguration.class, new ServerConfiguration());
        Components.getComponent(ServerConfiguration.class).setDefaultExtension("bla");
        URI2RepositoryMapping mapping = new URI2RepositoryMapping();
        mapping.setRepository("dummy-repo");
        mapping.setURIPrefix("/blabla/");
        // instance is set only in constructor ...

        final Workspace ws = mock(Workspace.class);
        final Context context = mock(Context.class);
        final Session session = mock(Session.class);
        final Node node = mock(Node.class);
        final Property property = mock(Property.class);
        final Property property2 = mock(Property.class);
        when(context.getJCRSession("dummy-repo")).thenReturn(session);
        when(session.nodeExists("/Test/image")).thenReturn(true);
        when(session.getNode("/Test/image")).thenReturn(node);

        when(node.isNodeType(NodeTypes.Resource.NAME)).thenReturn(true);
        when(node.hasProperty("fileName")).thenReturn(true);
        when(node.getProperty("fileName")).thenReturn(property);
        when(property.getString()).thenReturn("blah");

        when(node.getSession()).thenReturn(session);
        when(session.getWorkspace()).thenReturn(ws);
        when(ws.getName()).thenReturn("dummy-repo");

        when(node.hasProperty("extension")).thenReturn(true);
        when(node.getProperty("extension")).thenReturn(property2);
        when(property2.getString()).thenReturn("jpg");

        MgnlContext.setInstance(context);

        // WHEN
        String uri = mapping.getURI("/Test/image");

        // THEN
        assertEquals("Detected double slash in generated link path.",-1, uri.indexOf("//"));
        assertTrue("Incorrect file name generated.",uri.endsWith("/blah.jpg"));
    }

    @Test
    public void testGetHandleStripsExtensionInclTheDot() throws Exception {
        WebContext context = mock(WebContext.class);
        Session session = mock(Session.class);
        MgnlContext.setInstance(context);
        MockRepositoryAcquiringStrategy strategy = Components.getComponent(MockRepositoryAcquiringStrategy.class);
        strategy.addSession("config", session);
        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverConfiguration);
        Components.getComponent(ServerConfiguration.class).setDefaultExtension("ext");
        ComponentsTestUtil.setInstance(URI2RepositoryManager.class, new URI2RepositoryManager());

        when(context.getJCRSession("website")).thenReturn(session);
        String handle = Components.getComponent(URI2RepositoryManager.class).getHandle("/blah.ext");
        assertEquals("/blah", handle);
        handle = Components.getComponent(URI2RepositoryManager.class).getHandle("/b.l/ah.ext");
        assertEquals("/b.l/ah", handle);
        handle = Components.getComponent(URI2RepositoryManager.class).getHandle("/bl.ah.ext");
        assertEquals("/bl.ah", handle);
    }

    @Test
    public void testGetHandleWhenLinkWithPrefixHandleExistInRepo() throws Exception{
        WebContext context = mock(WebContext.class);
        Session session = mock(Session.class);

        MgnlContext.setInstance(context);
        URI2RepositoryManager uri2RepositoryManager = new URI2RepositoryManager();
        uri2RepositoryManager.addMapping(new URI2RepositoryMapping("/demo-project", "website", "/demoproject/year2010"));
        ComponentsTestUtil.setInstance(URI2RepositoryManager.class, uri2RepositoryManager);

        when(context.getJCRSession("website")).thenReturn(session);
        when(session.nodeExists("/demoproject/year2010/blah")).thenReturn(true);

        String handle = Components.getComponent(URI2RepositoryManager.class).getHandle("/demo-project/blah.ext");
        assertEquals("/demoproject/year2010/blah", handle); 
    }

    @Test
    public void testGetHandleWhenLinkWithPrefixHandleDoesNotExistInRepo() throws Exception{
        WebContext context = mock(WebContext.class);
        Session session = mock(Session.class);

        MgnlContext.setInstance(context);
        URI2RepositoryManager uri2RepositoryManager = new URI2RepositoryManager();
        uri2RepositoryManager.addMapping(new URI2RepositoryMapping("", "website", "/blabla"));
        ComponentsTestUtil.setInstance(URI2RepositoryManager.class, uri2RepositoryManager);

        when(context.getJCRSession("website")).thenReturn(session);
        when(session.itemExists("/demoproject/year2010/blah")).thenReturn(true);
        when(session.itemExists("/blah")).thenReturn(true);

        String handle = Components.getComponent(URI2RepositoryManager.class).getHandle("/blah.ext");
        assertEquals("/blah", handle); 
    }
}
