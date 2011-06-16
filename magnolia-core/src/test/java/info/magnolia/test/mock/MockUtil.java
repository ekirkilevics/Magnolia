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
package info.magnolia.test.mock;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.importexport.PropertiesImportExport;
import info.magnolia.test.ComponentsTestUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;


/**
 * Util to create mock objects. Use createHierarchyManager() to build mock content based on a property file. Property
 * values can have prefixes like boolean: int: for creating typed nodedatas.
 *
 * @version $Id$
 */
public class MockUtil {

    /**
     * Mocks the current and system context
     */
    public static MockContext initMockContext() {
        final MockContext ctx = new MockWebContext();
        MgnlContext.setInstance(ctx);
        ComponentsTestUtil.setImplementation(SystemContext.class, MockContext.class);
        return ctx;
    }

    public static MockContext getMockContext() {
        return getMockContext(false);
    }

    public static MockContext getMockContext(boolean create) {
        if (!MgnlContext.hasInstance() && create) {
            initMockContext();
        }
        return (MockContext) MgnlContext.getInstance();
    }

    public static MockContext getSystemMockContext() {
        return getSystemMockContext(false);
    }

    public static MockContext getSystemMockContext(boolean create) {
        MockContext ctx = (MockContext) MgnlContext.getSystemContext();
        if(ctx == null && create){
            initMockContext();
            ctx = getSystemMockContext(false);
        }
        return ctx;
    }

    /**
     * @deprecated since 5.0 - use {@link SessionTestUtil#createSession(InputStream)} instead.
     */
    public static MockHierarchyManager createHierarchyManager(InputStream propertiesStream) throws IOException, RepositoryException {
        return createHierarchyManager(null, propertiesStream);
    }

    /**
     * @deprecated since 5.0 - use {@link SessionTestUtil#createSession(String, InputStream)} instead.
     */
    public static MockHierarchyManager createHierarchyManager(String repository, InputStream propertiesStream) throws IOException, RepositoryException {
        MockHierarchyManager hm = new MockHierarchyManager(repository);
        Content root = hm.getRoot();
        createContent(root, propertiesStream);
        return hm;
    }

    /**
     * @deprecated since 5.0 - use {@link SessionTestUtil#createSession(String)} instead.
     */
    public static MockHierarchyManager createHierarchyManager(String propertiesStr) throws IOException, RepositoryException {
        final ByteArrayInputStream in = new ByteArrayInputStream(propertiesStr.getBytes());
        return createHierarchyManager(null, in);
    }

    public static MockHierarchyManager createAndSetHierarchyManager(String repository, InputStream propertiesStream) throws IOException, RepositoryException {
        MockHierarchyManager hm = createHierarchyManager(repository, propertiesStream);
        MockContext ctx = getMockContext(true);
        ctx.addHierarchyManager(repository, hm);
        ctx.addSession(repository, hm.getJcrSession());

        MockContext sysCtx = getSystemMockContext(true);
        sysCtx.addHierarchyManager(repository, hm);
        sysCtx.addSession(repository, hm.getJcrSession());
        hm.save();
        return hm;
    }

    public static MockHierarchyManager createAndSetHierarchyManager(String repository, String propertiesStr) throws IOException, RepositoryException {
        final ByteArrayInputStream in = new ByteArrayInputStream(propertiesStr.getBytes());
        return createAndSetHierarchyManager(repository, in);
    }

    public static void createContent(Content root, InputStream propertiesStream) throws IOException, RepositoryException {
        final PropertiesImportExport importer = new PropertiesImportExport() {
            @Override
            protected void populateContent(Content c, String name, String valueStr) throws RepositoryException {
                if ("@uuid".equals(name)) {
                    ((MockContent) c).setUUID(valueStr);
                } else {
                    super.populateContent(c, name, valueStr);
                }
            }
        };
        importer.createContent(root, propertiesStream);
    }

    public static Content createContent(final String name, Object[][] data, Content[] children) throws RepositoryException {
        OrderedMap nodeDatas = MockUtil.createNodeDatas(data);
        OrderedMap childrenMap = new ListOrderedMap();

        for (Content child : children) {
            childrenMap.put(child.getName(), child);
        }

        return new MockContent(name, nodeDatas, childrenMap);
    }

    public static Content createNode(String name, Object[][] data) throws RepositoryException {
        return createContent(name, data, new Content[]{});
    }

    public static OrderedMap createNodeDatas(Object[][] data) {
        OrderedMap nodeDatas = new ListOrderedMap();
        for (Object[] aData : data) {
            String name = (String) aData[0];
            Object value = aData[1];
            nodeDatas.put(name, new MockNodeData(name, value));
        }
        return nodeDatas;
    }

    /**
     * Utility method similar to other create* methods; takes a vararg string argument to avoid concatening long strings and \n's.
     * Creates a HierarchyManager based on the given properties, and the first argument is the path to the node which
     * we want to get from this HierarchyManager.
     */
    public static Content createNode(String returnFromPath, String... propertiesFormat) throws RepositoryException, IOException {
        return createHierarchyManager(propsStr(propertiesFormat)).getContent(returnFromPath);
    }

    private static String propsStr(String... s) {
        return StringUtils.join(Arrays.asList(s), "\n");
    }

    /**
     * @deprecated use PropertiesImportExport.toProperties(hm);
     */
    @Deprecated
    public static Properties toProperties(HierarchyManager hm) throws Exception {
        return PropertiesImportExport.toProperties(hm);
    }

    public static void mockObservation(MockHierarchyManager hm) throws RepositoryException, UnsupportedRepositoryOperationException {
        // fake observation
        Workspace ws = createMock(Workspace.class);
        ObservationManager om = createMock(ObservationManager.class);

        om.addEventListener(isA(EventListener.class), anyInt(),isA(String.class),anyBoolean(), (String[])anyObject(), (String[]) anyObject(), anyBoolean());

        expect(ws.getObservationManager()).andStubReturn(om);
        hm.setWorkspace(ws);
        replay(ws, om);
    }

}
