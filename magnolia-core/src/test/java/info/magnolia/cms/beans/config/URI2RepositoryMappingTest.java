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
package info.magnolia.cms.beans.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.TestMagnoliaConfigurationProperties;
import info.magnolia.test.mock.MockRepositoryAcquiringStrategy;

import javax.jcr.PropertyType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
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
        ComponentsTestUtil.setInstance(ServerConfiguration.class, new ServerConfiguration());
        ServerConfiguration.getInstance().setDefaultExtension("bla");
        URI2RepositoryMapping mapping = new URI2RepositoryMapping();
        mapping.setRepository("dummy-repo");
        mapping.setURIPrefix("/blabla/");
        // instance is set only in constructor ...
        final Context context = mock(Context.class);
        final HierarchyManager hm = mock(HierarchyManager.class);
        final Content cnt = mock(Content.class);
        final NodeData docu = mock(NodeData.class);
        when(context.getHierarchyManager("dummy-repo")).thenReturn(hm);
        when(hm.isNodeData("/Test")).thenReturn(Boolean.FALSE);
        when(hm.isNodeData("/Test/image")).thenReturn(Boolean.TRUE);
        when(hm.getContent("/Test")).thenReturn(cnt);
        when(cnt.getNodeData("image")).thenReturn(docu);
        when(cnt.getHierarchyManager()).thenReturn(hm);
        when(cnt.isNodeType("mix:referenceable")).thenReturn(true);

        when(docu.getType()).thenReturn(PropertyType.BINARY);
        when(docu.getAttribute("extension")).thenReturn("jpg");
        when(docu.getAttribute("fileName")).thenReturn("blah");
        MgnlContext.setInstance(context);
        String uri = mapping.getURI("/Test/image");
        assertEquals("Detected double slash in generated link path.",-1, uri.indexOf("//"));
        assertTrue("Incorrect file name generated.",uri.endsWith("/blah.jpg"));
    }

    @Test
    public void testGetHandleStripsExtensionInclTheDot() throws Exception {
        WebContext context = mock(WebContext.class);
        HierarchyManager hm = mock(HierarchyManager.class);
        MgnlContext.setInstance(context);
        MockRepositoryAcquiringStrategy strategy = Components.getSingleton(MockRepositoryAcquiringStrategy.class);
        strategy.addHierarchyManager("config", hm);
        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverConfiguration);
        ServerConfiguration.getInstance().setDefaultExtension("ext");
        ComponentsTestUtil.setInstance(URI2RepositoryManager.class, new URI2RepositoryManager());
        Object[] objs = new Object[] {context, hm};
        String handle = URI2RepositoryManager.getInstance().getHandle("/blah.ext");
        assertEquals("/blah", handle);
        handle = URI2RepositoryManager.getInstance().getHandle("/b.l/ah.ext");
        assertEquals("/b.l/ah", handle);
        handle = URI2RepositoryManager.getInstance().getHandle("/bl.ah.ext");
        assertEquals("/bl.ah", handle);
    }
}
