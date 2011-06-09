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
import static org.junit.Assert.assertNotNull;
import info.magnolia.test.mock.jcr.MockSession;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Test;

/**
 * @version $Id$
 */
public class SessionTestUtilTest {

    @Test
    public void testSyntax() throws IOException, RepositoryException {
        // TODO :this syntax is deprecated
        String content = "/parent1/sub1.prop1=one\n"
            + "parent2/sub2.prop1=two\n"
            + "parent3.sub3.prop1=three";

        MockSession session = SessionTestUtil.createSession(content);
        assertEquals("one", session.getNode("/parent1/sub1").getProperty("prop1").getString());
        assertEquals("two", session.getNode("/parent2/sub2").getProperty("prop1").getString());
        assertEquals("three", session.getNode("/parent3/sub3").getProperty("prop1").getString());

        content = "/parent1/sub1@uuid=1\n" +
                "parent2/sub2.@uuid=2\n" + // TODO : this syntax is deprecated
                "parent3.sub3@uuid=3\n" + // TODO : this syntax is deprecated
                "parent4.sub4.@uuid=4"; // TODO : this syntax is deprecated

        session = SessionTestUtil.createSession(content);
        assertEquals("1", session.getNode("/parent1/sub1").getIdentifier());
        assertEquals("2", session.getNode("/parent2/sub2").getIdentifier());
        assertEquals("3", session.getNode("/parent3/sub3").getIdentifier());
        assertEquals("4", session.getNode("/parent4/sub4").getIdentifier());
    }

    @Test
    public void testSingleDot() throws Exception {
        final MockSession session = SessionTestUtil.createSession("/foo/bar/baz/paragraphs/01.text=dummy\n");

        final Node root = session.getRootNode();
        assertNotNull(root.getNode("/foo/bar/baz"));

        assertEquals("dummy", session.getNode("/foo/bar/baz/paragraphs/01").getProperty("text").getString());
    }

    @Test
    public void testSingleMonkeyTail() throws Exception {
        final String pathToNode = "/parent1/sub1";
        final MockSession session = SessionTestUtil.createSession(pathToNode + "@uuid=100");
        assertEquals("100", session.getNode(pathToNode).getIdentifier());
    }

    @Test
    public void testCombined() throws Exception {
        final MockSession session =
                SessionTestUtil.createSession("/parent1/sub1.prop1=one\n"+
                        "parent1/sub1.prop2=two\n"+
                        "parent1.sub1.prop3=three");

        assertEquals("one", session.getNode("/parent1/sub1").getProperty("prop1").getString());
        assertEquals("two", session.getNode("/parent1/sub1").getProperty("prop2").getString());
        assertEquals("three", session.getNode("/parent1/sub1").getProperty("prop3").getString());


    }
}
