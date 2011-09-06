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
package info.magnolia.test.mock.jcr;

import info.magnolia.jcr.util.PropertiesImportExport;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Utility to setUp mock-jcr-structures for tests.
 *
 * @version $Id$
 */
public class NodeTestUtil {

    public static MockNode createNode(String name, Object[][] data) {
        return NodeTestUtil.createNode(name, data, new MockNode[] {});
    }

    /**
     * Utility method similar to other create* methods; takes a vararg string argument to avoid concatenating long strings
     * and \n's.
     */
    public static Node createNode(String returnFromPath, String workspaceName, String... propertiesFormat) throws RepositoryException, IOException {
        if (propertiesFormat.length == 0) {
            throw new IllegalArgumentException("You have to provide a non-empty array of properties.");
        }
        return SessionTestUtil.createSession(workspaceName, SessionTestUtil.asLineFeedSeparatedString(propertiesFormat)).getNode(returnFromPath);
    }

    public static void createSubnodes(Node root, InputStream propertiesStream) throws IOException, RepositoryException {
        final PropertiesImportExport importer = new PropertiesImportExport() {
            @Override
            protected void setIdentifier(Node c, String valueStr) {
                ((MockNode) c).setIdentifier(valueStr);
            }
        };
        importer.createContent(root, propertiesStream);
    }

    public static MockNode createNode(final String name, Object[][] data, MockNode[] children) {
        Map<String, MockValue> values = createValues(data);
        Map<String, MockNode> childrenMap = new LinkedHashMap<String, MockNode>();

        for (MockNode child : children) {
            childrenMap.put(child.getName(), child);
        }

        return new MockNode(name, values, childrenMap);
    }

    public static Map<String, MockValue> createValues(Object[][] data) {
        Map<String, MockValue> nodeDatas = new LinkedHashMap<String, MockValue>();
        for (Object[] aData : data) {
            String name = (String) aData[0];
            Object value = aData[1];
            nodeDatas.put(name, new MockValue(value));
        }
        return nodeDatas;
    }

}
