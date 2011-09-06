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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Utility to setUp mock-jcr-structures for tests. Use createSession() to build mock content based on a property file.
 * Property values can have prefixes like boolean: int: for creating typed properties.
 *
 * @version $Id$
 */
public class SessionTestUtil {

    /**
     * TODO - will be removed in a next step
     */
    public static MockSession createSession(InputStream propertiesStream) throws IOException, RepositoryException {
        return createSession(null, propertiesStream);
    }

    /**
     * TODO - will be removed in a next step
     */
    public static MockSession createSession(String propertiesStr) throws IOException, RepositoryException {
        return createSession(null, propertiesStr);
    }

    public static MockSession createSession(String workspace, String propertiesStr) throws IOException, RepositoryException {
        final ByteArrayInputStream in = new ByteArrayInputStream(propertiesStr.getBytes());
        return createSession(workspace, in);
    }

    /**
     * Create and return a MockSession for the provided workspace by merging all propertiesFormats to a linefeed separated String.
     */
    public static MockSession createSession(String workspace, String... propertiesFormat) throws IOException, RepositoryException {
        return createSession(workspace, asLineFeedSeparatedString(propertiesFormat));
    }

    public static MockSession createSession(String workspace, InputStream propertiesStream) throws IOException, RepositoryException {
        MockSession hm = new MockSession(workspace);
        Node root = hm.getRootNode();
        NodeTestUtil.createSubnodes(root, propertiesStream);
        return hm;
    }

    static String asLineFeedSeparatedString(String... s) {
        return StringUtils.join(Arrays.asList(s), "\n");
    }

}
