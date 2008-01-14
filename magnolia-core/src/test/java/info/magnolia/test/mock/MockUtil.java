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
package info.magnolia.test.mock;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Util to create mock objects. Use createHierarchyManager() to build mock content based on a property file. property
 * values can have prefixes like boolean: int: for creating typed nodedatas.
 * @author philipp
 * @version $Id$
 */
public class MockUtil {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MockContext.class);

    /**
     * Ordered properties. Uses to keep the order in the mocked content
     * @author philipp
     * @version $Id$
     */
    public static final class OrderedProperties extends Properties {

        private final LinkedHashMap map = new LinkedHashMap();

        public Object put(Object key, Object value) {
            return map.put(key, value);
        }

        public Object get(Object key) {
            return map.get(key);
        }

        public String getProperty(String key) {
            return (String) get(key);
        }

        public synchronized Object setProperty(String key, String value) {
            return this.map.put(key, value);
        }

        public Set entrySet() {
            return this.map.entrySet();
        }

        public Set keySet() {
            return this.map.keySet();
        }

        public int size() {
            return this.map.size();
        }
    }

    /**
     * Mocks the current and system context
     */
    public static MockContext initMockContext() {
        final MockContext ctx = new MockContext();
        MgnlContext.setInstance(ctx);
        // and system context as well
        FactoryUtil.setInstanceFactory(SystemContext.class, new FactoryUtil.InstanceFactory(){
        	public Object newInstance() {
        	    return ctx;
        	}
        }); 
        return ctx;
    }

    public static MockContext getMockContext() {
        return getMockContext(false);
    }

    public static MockContext getMockContext(boolean create) {
        MockContext ctx = (MockContext) MgnlContext.getInstance();
        if(ctx == null && create){
            initMockContext();
            ctx = getMockContext(false);
        }
        return ctx;
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

    public static MockHierarchyManager createHierarchyManager(InputStream propertiesStream) throws IOException, RepositoryException {
        MockHierarchyManager hm = new MockHierarchyManager();
        Content root = hm.getRoot();
        createContent(root, propertiesStream);
        return hm;
    }

    public static MockHierarchyManager createHierarchyManager(String propertiesStr) throws IOException, RepositoryException {
        final ByteArrayInputStream in = new ByteArrayInputStream(propertiesStr.getBytes());
        return createHierarchyManager(in);
    }

    public static MockHierarchyManager createAndSetHierarchyManager(String repository, InputStream propertiesStream) throws IOException, RepositoryException {
        MockHierarchyManager hm = createHierarchyManager(propertiesStream);
        getMockContext(true).addHierarchyManager(repository, hm);
        getSystemMockContext(true).addHierarchyManager(repository, hm);
        return hm;
    }

    public static MockHierarchyManager createAndSetHierarchyManager(String repository, String propertiesStr) throws IOException, RepositoryException {
        final ByteArrayInputStream in = new ByteArrayInputStream(propertiesStr.getBytes());
        return createAndSetHierarchyManager(repository, in);
    }

    public static void createContent(Content root, InputStream propertiesStream) throws IOException, RepositoryException {
        Properties properties = new OrderedProperties();

        properties.load(propertiesStream);

        for (Object o : properties.keySet()) {
            String orgKey = (String) o;
            String valueStr = properties.getProperty(orgKey);

            String key = StringUtils.replace(orgKey, "/", ".");
            key = StringUtils.removeStart(key, ".");
            // guarantee a dot in front of @ to make it a property
            key = StringUtils.replace(StringUtils.replace(key, "@", ".@"), "..@", ".@");

            String name = StringUtils.substringAfterLast(key, ".");
            String path = StringUtils.substringBeforeLast(key, ".");
            path = StringUtils.replace(path, ".", "/");

            MockContent c = (MockContent) ContentUtil.createPath(root, path, ItemType.CONTENTNODE);
            populateContent(c, name, valueStr);
        }
    }

    public static void populateContent(MockContent c, String name, String valueStr) {
        if (StringUtils.isEmpty(name) && StringUtils.isEmpty(valueStr)) {
            // happens if the input properties file just created a node with no properties
            return;
        }
        if (name.equals("@type")) {
            c.setNodeTypeName(valueStr);
        }
        else if (name.equals("@uuid")) {
            c.setUUID(valueStr);
        }
        else {
            Object valueObj = convertNodeDataStringToObject(valueStr);
            c.addNodeData(new MockNodeData(name, valueObj));
        }
    }

    public static Object convertNodeDataStringToObject(String valueStr) {
        Object valueObj = valueStr;

        if (valueStr.contains(":")) {
            String type = StringUtils.substringBefore(valueStr, ":");
            if (type.equals("int")) {
                type = "integer";
            }
            String value = StringUtils.substringAfter(valueStr, ":");
            try {
                valueObj = ConvertUtils.convert(value, Class.forName("java.lang." + StringUtils.capitalize(type)));
            }
            catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("can't convert value [" + valueStr + "]", e);
            }
        }
        return valueObj;
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

    // TODO : does not take property type into account
    public static Properties toProperties(HierarchyManager hm) throws Exception {
        final Properties out = new OrderedProperties();
        ContentUtil.visit(hm.getRoot(), new ContentUtil.Visitor(){
            public void visit(Content node) throws Exception {
                appendNodeProperties(node, out);
            }
        });
        return out;
    }

    private static void appendNodeProperties(Content node, Properties out) {
        final Collection props = node.getNodeDataCollection();
        final Iterator it = props.iterator();
        while (it.hasNext()) {
            final NodeData prop = (NodeData) it.next();
            final String path = node.getHandle() + "." + prop.getName();
            out.setProperty(path, prop.getString());
        }
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
