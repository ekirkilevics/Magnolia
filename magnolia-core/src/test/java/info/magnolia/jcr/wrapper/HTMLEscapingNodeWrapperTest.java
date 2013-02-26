/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.jcr.wrapper;

import static org.junit.Assert.*;

import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;

import org.junit.Test;

/**
 * Test case for {@link HTMLEscapingNodeWrapper}.
 */
public class HTMLEscapingNodeWrapperTest {

    @Test
    public void testPropertyIsWrappedAndEncoded() throws Exception {
        MockNode node = new MockNode();
        node.setProperty("text", "<html/>");

        HTMLEscapingNodeWrapper wrapper = new HTMLEscapingNodeWrapper(node, false);

        Property property = wrapper.getProperty("text");
        assertTrue(property instanceof HTMLEscapingPropertyWrapper);
        assertEquals("&lt;html/&gt;", property.getString());
    }

    @Test
    public void testNodeNameIsWrappedAndEncoded() throws Exception {
        MockNode node = new MockNode("<html>");

        HTMLEscapingNodeWrapper wrapper = new HTMLEscapingNodeWrapper(node, false);

        String name = wrapper.getName();
        assertEquals("&lt;html&gt;", name);
    }

    @Test
    public void testPropertyNameIsWrappedAndEncoded() throws Exception {
        MockNode node = new MockNode();
        node.setProperty("<html>", "bla");
        HTMLEscapingNodeWrapper wrapper = new HTMLEscapingNodeWrapper(node, false);

        assertEquals("&lt;html&gt;", wrapper.getProperty("<html>").getName());
    }

    @Test
    public void testLineBreakEncoding() throws Exception {
        MockNode node = new MockNode();
        node.setProperty("text", "line1\nline2");

        HTMLEscapingNodeWrapper wrapper = new HTMLEscapingNodeWrapper(node, true);

        assertEquals("line1<br/>line2", wrapper.getProperty("text").getString());
    }

    @Test
    public void testPropertyIteratorReturnsWrappedProperty() throws Exception {
        MockNode node = new MockNode();
        node.setProperty("text", "<html/>");

        HTMLEscapingNodeWrapper wrapper = new HTMLEscapingNodeWrapper(node, false);

        PropertyIterator properties = wrapper.getProperties();
        assertEquals(1, properties.getSize());
        Property property = properties.nextProperty();
        assertTrue(property instanceof HTMLEscapingPropertyWrapper);
        assertEquals("&lt;html/&gt;", property.getString());
    }

    @Test
    public void testPropertyReturnedFromPropertyIsWrapped() throws Exception {

        MockSession session = new MockSession("sessionName");
        Node rootNode = session.getRootNode();

        Node referredTo = rootNode.addNode("referredTo");
        referredTo.setProperty("text", "<html/>");

        Node referrer = rootNode.addNode("referrer");
        referrer.setProperty("reference", "/referredTo/text"); // Reference to a property

        HTMLEscapingNodeWrapper wrapper = new HTMLEscapingNodeWrapper(referrer, false);

        Property property = wrapper.getProperty("reference").getProperty();
        assertTrue(property instanceof HTMLEscapingPropertyWrapper);
        assertEquals("&lt;html/&gt;", property.getString());
    }

    @Test
    public void testNodeReturnedFromPropertyIsWrapped() throws Exception {
        MockSession session = new MockSession("sessionName");
        Node rootNode = session.getRootNode();

        Node referredTo = rootNode.addNode("referredTo");
        referredTo.setProperty("text", "<html/>");

        Node referrer = rootNode.addNode("referrer");
        referrer.setProperty("reference", referredTo);

        HTMLEscapingNodeWrapper wrapper = new HTMLEscapingNodeWrapper(referrer, false);

        Property property = wrapper.getProperty("reference").getNode().getProperty("text");
        assertTrue(property instanceof HTMLEscapingPropertyWrapper);
        assertEquals("&lt;html/&gt;", property.getString());
    }

    @Test
    public void testNodeReturnedFromParentIsWrapped() throws Exception {
        MockSession session = new MockSession("sessionName");
        Node rootNode = session.getRootNode();

        Node foo = rootNode.addNode("foo");
        Node fooChild = foo.addNode("fooChild");
        foo.setProperty("text", "<html/>");

        HTMLEscapingNodeWrapper wrapper = new HTMLEscapingNodeWrapper(fooChild, false);

        Property property = wrapper.getParent().getProperty("text");
        assertTrue(property instanceof HTMLEscapingPropertyWrapper);
        assertEquals("&lt;html/&gt;", property.getString());
    }

    @Test
    public void testNodeReturnedFromAncestorIsWrapped() throws Exception {
        MockSession session = new MockSession("sessionName");
        Node rootNode = session.getRootNode();

        Node foo = rootNode.addNode("foo");
        Node fooChild = foo.addNode("fooChild");
        foo.setProperty("text", "<html/>");

        HTMLEscapingNodeWrapper wrapper = new HTMLEscapingNodeWrapper(fooChild, false);

        Property property = ((Node) wrapper.getAncestor(1)).getProperty("text");
        assertTrue(property instanceof HTMLEscapingPropertyWrapper);
        assertEquals("&lt;html/&gt;", property.getString());
    }

    @Test
    public void testPropertyFromNodeReturnedFromPropertyIsWrapped() throws Exception {
        MockSession session = new MockSession("sessionName");
        Node rootNode = session.getRootNode();

        Node foo = rootNode.addNode("foo");
        foo.setProperty("text", "<html/>");

        HTMLEscapingNodeWrapper wrapper = new HTMLEscapingNodeWrapper(foo, false);

        Property property = wrapper.getProperty("text").getParent().getProperty("text");
        assertTrue(property instanceof HTMLEscapingPropertyWrapper);
        assertEquals("&lt;html/&gt;", property.getString());
    }

    @Test
    public void testNameHaveToBeEscapedBecauseOfXss() throws Exception {
        // GIVEN
        MockSession session = new MockSession("sessionName");
        Node rootNode = session.getRootNode();
        Node foo = rootNode.addNode("<>\"&");
        HTMLEscapingNodeWrapper wrapper = new HTMLEscapingNodeWrapper(foo, false);
        // WHEN
        String name = wrapper.getName();
        // THEN
        assertEquals("&lt;&gt;&quot;&amp;", name);
    }
}
