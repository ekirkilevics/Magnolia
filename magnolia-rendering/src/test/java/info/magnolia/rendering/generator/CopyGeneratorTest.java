/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.rendering.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.rendering.template.AutoGenerationConfiguration;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO fgrilli: tests now expect UnsupportedOperationException which is currently raised when calling save on  MockSession.
 * When Mock objects refactoring will be completed, we will probably be able to get rid of it.
 * Clean up and simplify messy creation of nested map returned by AutoGenerationConfiguration.
 * @version $Id$
 */
public class CopyGeneratorTest {

    private MockSession session;

    @Before
    public void setUp() throws Exception{
        session = SessionTestUtil.createSession("website", "/foo/bar");
    }

    /*
     * We expect a structure like the following will be created, where "foo" and "bar" already exist.
     *
     * + foo
     * + bar
     * + autogen-foo
     */
    @Test(expected=UnsupportedOperationException.class)
    public void testSameLevelNodesCreation() throws Exception{
        //GIVEN
        Node parent = session.getNode("/foo");
        AutoGenerationConfiguration config = mock(AutoGenerationConfiguration.class);

        Map<String, Object> content = new HashMap<String, Object>();
        Map<String, Object> firstNodeProps = new HashMap<String, Object>();
        firstNodeProps.put("nodeType", MgnlNodeType.NT_CONTENTNODE);
        firstNodeProps.put("templateId", "foo:/bar/baz");
        firstNodeProps.put("anotherProp", "some value");
        content.put("autogen-foo", firstNodeProps);

        Map<String, Object> secondNodeProps = new HashMap<String, Object>();
        secondNodeProps.put("nodeType", MgnlNodeType.NT_CONTENTNODE);
        secondNodeProps.put("templateId", "foo:/bar/baz");
        secondNodeProps.put("someProp", "a different value");
        content.put("same-level-autogen", secondNodeProps);

        when(config.getContent()).thenReturn(content);

        //WHEN
        new CopyGenerator(parent).generate(config);

        //THEN
        Node newNode = session.getNode("/foo/autogen-foo");
        assertTrue(newNode.isNodeType(MgnlNodeType.NT_CONTENTNODE));

        Property prop = newNode.getProperty("anotherProp");
        assertEquals("some value", prop.getString());

        Node metaData = session.getNode("/foo/autogen-foo/MetaData");
        Property template = metaData.getProperty("mgnl:template");
        assertEquals("foo:/bar/baz",template.getString());

        Node secondNode = session.getNode("/foo/same-level-autogen");
        assertTrue(secondNode.isNodeType(MgnlNodeType.NT_CONTENTNODE));

        prop = secondNode.getProperty("someProp");
        assertEquals("a different value", prop.getString());

        metaData = session.getNode("/foo/same-level-autogen/MetaData");
        template = metaData.getProperty("mgnl:template");
        assertEquals("foo:/bar/baz",template.getString());
    }

    /*
     * We expect a structure like the following will be created, where "foo" and "bar" already exist.
     *
     * + foo
     * |  + autogen-foo
     * |    + nested-autogen
     * + bar
     */
    @Test(expected=UnsupportedOperationException.class)
    public void testNestedNodesCreation() throws Exception {
        //GIVEN
        Node parent = session.getNode("/foo");
        AutoGenerationConfiguration config = mock(AutoGenerationConfiguration.class);

        Map<String, Object> content = new HashMap<String, Object>();
        Map<String, Object> firstNodeProps = new HashMap<String, Object>();
        firstNodeProps.put("nodeType", MgnlNodeType.NT_CONTENTNODE);
        firstNodeProps.put("templateId", "foo:/bar/baz");
        firstNodeProps.put("anotherProp", "some value");

        Map<String, Object> nestedNodeProps = new HashMap<String, Object>();
        nestedNodeProps.put("nodeType", MgnlNodeType.NT_CONTENTNODE);
        nestedNodeProps.put("templateId", "foo:/bar/baz");
        nestedNodeProps.put("someProp", "a different value");

        Map<String, Object> nestedSubNodeProps = new HashMap<String, Object>();
        nestedSubNodeProps.put("nodeType", MgnlNodeType.NT_CONTENTNODE);
        nestedSubNodeProps.put("templateId", "foo:/bar/baz");

        nestedNodeProps.put("nestedSubNode-autogen", nestedSubNodeProps);
        firstNodeProps.put("nested-autogen", nestedNodeProps);
        content.put("autogen-foo", firstNodeProps);

        when(config.getContent()).thenReturn(content);

        //WHEN
        new CopyGenerator(parent).generate(config);

        //THEN
        Node newNode = session.getNode("/foo/autogen-foo");
        assertTrue(newNode.isNodeType(MgnlNodeType.NT_CONTENTNODE));

        Property prop = newNode.getProperty("anotherProp");
        assertEquals("some value", prop.getString());

        Node metaData = session.getNode("/foo/autogen-foo/MetaData");
        Property template = metaData.getProperty("mgnl:template");
        assertEquals("foo:/bar/baz",template.getString());

        Node secondNode = session.getNode("/foo/autogen-foo/nested-autogen");
        assertTrue(secondNode.isNodeType(MgnlNodeType.NT_CONTENTNODE));

        prop = secondNode.getProperty("someProp");
        assertEquals("a different value", prop.getString());

        metaData = session.getNode("/foo/autogen-foo/nested-autogen/MetaData");
        template = metaData.getProperty("mgnl:template");
        assertEquals("foo:/bar/baz",template.getString());

        Node secondSubNode = session.getNode("/foo/autogen-foo/nested-autogen/nestedSubNode-autogen");
        assertTrue(secondSubNode.isNodeType(MgnlNodeType.NT_CONTENTNODE));

        metaData = session.getNode("/foo/autogen-foo/nested-autogen/nestedSubNode-autogen/MetaData");
        template = metaData.getProperty("mgnl:template");
        assertEquals("foo:/bar/baz",template.getString());
    }
    /*
     * We expect a structure like the following will be created, where "foo" and "bar" already exist.
     *
     * + foo
     * |  + autogen-foo
     * |  + same-level-autogen-foo
     * |    + nested-autogen
     * |    + same-level-as-nested
     * + bar
     */
    @Test(expected=UnsupportedOperationException.class)
    public void testSameLevelNestedNodesCreation() throws Exception {
        //GIVEN
        Node parent = session.getNode("/foo");
        AutoGenerationConfiguration config = mock(AutoGenerationConfiguration.class);

        Map<String, Object> content = new HashMap<String, Object>();
        Map<String, Object> firstNodeProps = new HashMap<String, Object>();
        firstNodeProps.put("nodeType", MgnlNodeType.NT_CONTENTNODE);
        firstNodeProps.put("templateId", "foo:/bar/baz");

        content.put("autogen-foo", firstNodeProps);

        Map<String, Object> sameLevelNodeProps = new HashMap<String, Object>();
        sameLevelNodeProps.put("nodeType", MgnlNodeType.NT_CONTENTNODE);
        sameLevelNodeProps.put("templateId", "foo:/bar/baz");

        content.put("same-level-autogen-foo", sameLevelNodeProps);

        Map<String, Object> nestedNodeProps = new HashMap<String, Object>();
        nestedNodeProps.put("nodeType", MgnlNodeType.NT_CONTENTNODE);
        nestedNodeProps.put("templateId", "foo:/bar/baz");

        firstNodeProps.put("nested-autogen", nestedNodeProps);

        Map<String, Object> sameLevelNestedNodeProps = new HashMap<String, Object>();
        sameLevelNestedNodeProps.put("nodeType", MgnlNodeType.NT_CONTENTNODE);
        sameLevelNestedNodeProps.put("templateId", "foo:/bar/baz");

        firstNodeProps.put("same-level-as-nested", sameLevelNestedNodeProps);

        when(config.getContent()).thenReturn(content);

        //WHEN
        new CopyGenerator(parent).generate(config);

        //THEN
        Node newNode = session.getNode("/foo/autogen-foo");
        assertTrue(newNode.isNodeType(MgnlNodeType.NT_CONTENTNODE));

        Node metaData = session.getNode("/foo/autogen-foo/MetaData");
        Property template = metaData.getProperty("mgnl:template");
        assertEquals("foo:/bar/baz",template.getString());

        Node secondNode = session.getNode("/foo/same-level-autogen-foo");
        assertTrue(secondNode.isNodeType(MgnlNodeType.NT_CONTENTNODE));

        metaData = session.getNode("/foo/same-level-autogen-foo/MetaData");
        template = metaData.getProperty("mgnl:template");
        assertEquals("foo:/bar/baz",template.getString());

        Node nestedNode = session.getNode("/foo/autogen-foo/nested-autogen");
        assertTrue(nestedNode.isNodeType(MgnlNodeType.NT_CONTENTNODE));

        metaData = session.getNode("/foo/autogen-foo/nested-autogen/MetaData");
        template = metaData.getProperty("mgnl:template");
        assertEquals("foo:/bar/baz",template.getString());

        Node sameLevelAsNested = session.getNode("/foo/autogen-foo/same-level-as-nested");
        assertTrue(sameLevelAsNested.isNodeType(MgnlNodeType.NT_CONTENTNODE));

        metaData = session.getNode("/foo/autogen-foo/same-level-as-nested/MetaData");
        template = metaData.getProperty("mgnl:template");
        assertEquals("foo:/bar/baz",template.getString());
    }

    @After
    public void tearDown() throws Exception {
        session = null;
    }
}
