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

import static org.junit.Assert.assertEquals;
import info.magnolia.test.mock.jcr.MockSession;

import java.io.IOException;

import javax.jcr.RepositoryException;

import org.junit.Test;

/**
 * @version $Id$
 */
public class SessionTestUtilTest {

    @Test
    public void testCreateSessionFromString() throws IOException, RepositoryException {
        String content = "/parent1/sub1.prop1=one";

        MockSession session = SessionTestUtil.createSession(content);
        assertEquals("one", session.getNode("/parent1/sub1").getProperty("prop1").getString());

        content = "/parent1/sub1.@uuid=1\n" + "/parent2/sub2.@uuid=2";

        session = SessionTestUtil.createSession(content);
        assertEquals("1", session.getNode("/parent1/sub1").getIdentifier());
        assertEquals("2", session.getNode("/parent2/sub2").getIdentifier());
    }

    @Test
    public void testCreateSessionWithStringVarargs() throws Exception {
        MockSession session = SessionTestUtil.createSession("testWorkspace",
            "/foo/bar.@type=mgnl:content",
            "/foo/bar/sub1.@uuid=1",
            "/foo/bar/subpath.property=testName");

        assertEquals("1", session.getNode("/foo/bar/sub1").getIdentifier());
        assertEquals("testName", session.getNode("/foo/bar/subpath").getProperty("property").getString());
    }

    @Test
    public void testMockSessionSetsProperWorkspaceName() throws Exception {
        final String workspaceName = "workspace";
        final MockSession session = SessionTestUtil.createSession(workspaceName, "/foo/bar/baz/01.text=dummy");
        assertEquals(workspaceName, session.getWorkspace().getName());
    }
}
