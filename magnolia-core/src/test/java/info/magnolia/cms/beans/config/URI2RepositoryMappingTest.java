/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import static org.easymock.EasyMock.*;

import javax.jcr.PropertyType;

import junit.framework.TestCase;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleManagerImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.test.mock.MockRepositoryAcquiringStrategy;

/**
 * @author had
 * @version $Id:$
 */
public class URI2RepositoryMappingTest extends TestCase {

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        FactoryUtil.setInstance(ServerConfiguration.class, null);
        super.tearDown();
    }

    public void testGetUri() throws Exception {
        FactoryUtil.setInstance(ServerConfiguration.class, new ServerConfiguration());
        ServerConfiguration.getInstance().setDefaultExtension("bla");
        URI2RepositoryMapping mapping = new URI2RepositoryMapping();
        mapping.setRepository("dummy-repo");
        mapping.setURIPrefix("/blabla/");
        // instance is set only in constructor ...
        final Context context = createStrictMock(Context.class);
        final HierarchyManager hm = createStrictMock(HierarchyManager.class);
        final Content cnt = createStrictMock(Content.class);
        final Content root = createStrictMock(Content.class);
        final NodeData docu = createStrictMock(NodeData.class);
        expect(context.getHierarchyManager("dummy-repo")).andReturn(hm);
        expect(hm.isExist("/Test/image")).andReturn(Boolean.TRUE);
        expect(hm.isNodeData("/Test/image")).andReturn(Boolean.TRUE);
        expect(hm.isNodeData("/Test")).andReturn(Boolean.FALSE);
        expect(hm.isNodeData("/Test/image")).andReturn(Boolean.TRUE);
        expect(hm.getContent("/Test")).andReturn(cnt);
        expect(cnt.getNodeData("image")).andReturn(docu);
        expect(hm.getName()).andReturn("dummy-repo");
        expect(cnt.getHierarchyManager()).andReturn(hm);
        expect(cnt.getUUID()).andReturn("uu-something-real");

        expect(docu.getType()).andReturn(PropertyType.BINARY);
        expect(docu.getAttribute("nodeDataTemplate")).andReturn("");
        expect(docu.getAttribute("extension")).andReturn("jpg");
        expect(docu.getAttribute("fileName")).andReturn("blah");
        expect(docu.getAttribute("contentType")).andReturn("mgnl:resource");
        expect(docu.getAttribute("size")).andReturn("1234");
        expect(docu.getType()).andReturn(PropertyType.BINARY);
        expect(docu.getAttribute("nodeDataTemplate")).andReturn("");
        expect(docu.getAttribute("extension")).andReturn("jpg");
        expect(docu.getAttribute("fileName")).andReturn("blah");
        expect(docu.getAttribute("contentType")).andReturn("mgnl:resource");
        expect(docu.getAttribute("size")).andReturn("1234");
        replay(context, hm, cnt, root, docu);
        MgnlContext.setInstance(context);
        String uri = mapping.getURI("/Test/image");
        assertEquals("Detected double slash in generated link path.",-1, uri.indexOf("//"));
        assertTrue("Incorrect file name generated.",uri.endsWith("/blah.jpg"));
        verify(context, hm, cnt, root, docu);
    }

    public void testGetHandleStripsExtensionInclTheDot() throws Exception {
        WebContext context = createNiceMock(WebContext.class);
        HierarchyManager hm = createNiceMock(HierarchyManager.class);
        MgnlContext.setInstance(context);
        MockRepositoryAcquiringStrategy strategy = (MockRepositoryAcquiringStrategy) FactoryUtil.getSingleton(MockRepositoryAcquiringStrategy.class);
        strategy.addHierarchyManager("config", hm);
        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        FactoryUtil.setInstance(ServerConfiguration.class, serverConfiguration);
        ServerConfiguration.getInstance().setDefaultExtension("ext");
        Object[] objs = new Object[] {context, hm};
        replay(objs);
        String handle = URI2RepositoryManager.getInstance().getHandle("/blah.ext");
        assertEquals("/blah", handle);
        handle = URI2RepositoryManager.getInstance().getHandle("/b.l/ah.ext");
        assertEquals("/b.l/ah", handle);
        handle = URI2RepositoryManager.getInstance().getHandle("/bl.ah.ext");
        assertEquals("/bl.ah", handle);
        verify(objs);
    }
}
