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
package info.magnolia.jcr.util;

import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.MockValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;


/**
 * Utility to setUp mock-jcr-structures for tests.. Use createSession() to build mock content based on a property file. Property
 * values can have prefixes like boolean: int: for creating typed properties.
 *
 * @version $Id$
 */
public class SessionTestUtil {

    public static MockSession createSession(InputStream propertiesStream) throws IOException, RepositoryException {
        return createSession(null, propertiesStream);
    }

    public static MockSession createSession(String propertiesStr) throws IOException, RepositoryException {
        final ByteArrayInputStream in = new ByteArrayInputStream(propertiesStr.getBytes());
        return createSession(null, in);
    }

    public static MockSession createSession(String repository, InputStream propertiesStream) throws IOException, RepositoryException {
        MockSession hm = new MockSession(repository);
        Node root = hm.getRootNode();
        createContent(root, propertiesStream);
        return hm;
    }

    public static void createContent(Node root, InputStream propertiesStream) throws IOException, RepositoryException {
        final PropertiesImportExport importer = new PropertiesImportExport() {
            @Override
            protected void populateContent(Node c, String name, String valueStr) throws RepositoryException {
                if ("@uuid".equals(name)) {
                    ((MockNode) c).setIdentifier(valueStr);
                } else {
                    super.populateContent(c, name, valueStr);
                }
            }
        };
        importer.createContent(root, propertiesStream);
    }

    public static MockNode createContent(final String name, Object[][] data, MockNode[] children) {
        Map<String, MockValue> nodeDatas = createNodeDatas(data);
        Map<String, MockNode> childrenMap = new LinkedHashMap<String, MockNode>();

        for (MockNode child : children) {
            childrenMap.put(child.getName(), child);
        }

        return new MockNode(name, nodeDatas, childrenMap);
    }

    public static MockNode createNode(String name, Object[][] data) throws RepositoryException {
        return createContent(name, data, new MockNode[]{});
    }

    public static Map<String, MockValue> createNodeDatas(Object[][] data) {
        Map<String, MockValue> nodeDatas = new LinkedHashMap<String, MockValue>();
        for (Object[] aData : data) {
            String name = (String) aData[0];
            Object value = aData[1];
            nodeDatas.put(name, new MockValue(value));
        }
        return nodeDatas;
    }

    /**
     * Utility method similar to other create* methods; takes a vararg string argument to avoid concatening long strings and \n's.
     * Creates a HierarchyManager based on the given properties, and the first argument is the path to the node which
     * we want to get from this HierarchyManager.
     */
    public static Node createNode(String returnFromPath, String... propertiesFormat) throws RepositoryException, IOException {
        return createSession(propsStr(propertiesFormat)).getNode(returnFromPath);
    }

    private static String propsStr(String... s) {
        return StringUtils.join(Arrays.asList(s), "\n");
    }

}
