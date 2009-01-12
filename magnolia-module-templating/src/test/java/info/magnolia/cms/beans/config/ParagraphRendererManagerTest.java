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

import info.magnolia.cms.beans.runtime.ParagraphRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ParagraphRendererManagerTest extends TestCase {

    public void testNotSpecifyingTheNamePropertyShouldMeanTheNodeNameIsUsedInstead() throws IOException, RepositoryException {
        final Content node = getConfigNode(CONFIGNODE2, "/modules/test2/paragraph-renderers");
        final ParagraphRendererManager prm = new ParagraphRendererManager();
        prm.onRegister(node);
        assertEquals(2, prm.getRenderers().size());
        assertNotNull(prm.getRenderer("gazonk"));
        assertNotNull(prm.getRenderer("baz"));
    }

    public void testUnknownParagraphRendererNamesThrowsException() throws IOException, RepositoryException {
        final Content node = getConfigNode(CONFIGNODE1, "/modules/test/paragraph-renderers");
        final ParagraphRendererManager prm = new ParagraphRendererManager();
        prm.onRegister(node);
        assertEquals(2, prm.getRenderers().size());
        assertNotNull(prm.getRenderer("foo"));
        assertNotNull(prm.getRenderer("bar"));
        try {
            prm.getRenderer("gazonk");
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("No paragraph renderer registered with name gazonk", e.getMessage());
        }
    }

    public void testParagraphRenderersAreAddedProperly() throws IOException, RepositoryException {
        Content node1 = getConfigNode(CONFIGNODE1, "/modules/test/paragraph-renderers");
        Content node2 = getConfigNode(CONFIGNODE2, "/modules/test2/paragraph-renderers");

        final ParagraphRendererManager prm = new ParagraphRendererManager();
        prm.onRegister(node1);
        final Map renderers = prm.getRenderers();
        assertEquals(2, renderers.size());
        final ParagraphRenderer foo = (ParagraphRenderer) renderers.get("foo");
        assertEquals(foo, prm.getRenderer("foo"));
        assertTrue(renderers.get("bar") instanceof OtherDummyParagraphRenderer);

        prm.onRegister(node2);
        assertEquals(4, prm.getRenderers().size());
        assertEquals(4, renderers.size());
        assertEquals(renderers, prm.getRenderers());
        assertTrue(renderers.get("baz") instanceof DummyParagraphRenderer);
        assertTrue(renderers.get("gazonk") instanceof DummyParagraphRenderer);

        prm.clear();
        assertEquals(0, prm.getRenderers().size());
        assertEquals(0, renderers.size());
    }

    public void testCanNotAddParagraphRenderersWithDuplicateNames() throws IOException, RepositoryException {
        Content node1 = getConfigNode(CONFIGNODE1, "/modules/test/paragraph-renderers");
        Content node3 = getConfigNode(CONFIGNODE3, "/modules/test3/paragraph-renderers");

        final ParagraphRendererManager prm = new ParagraphRendererManager();
        prm.onRegister(node1);
        final Map renderers = prm.getRenderers();
        assertEquals(2, renderers.size());
        final ParagraphRenderer foo = (ParagraphRenderer) renderers.get("foo");
        assertEquals(foo, prm.getRenderer("foo"));
        assertTrue(renderers.get("bar") instanceof OtherDummyParagraphRenderer);

        try {
            prm.onRegister(node3);
            fail("should have thrown an IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Duplicate paragraph name \"foo\"", e.getMessage());
            // TODO : would be nice if the exception message also said "in path /modules/test3/paragraph-renderers/foo3"
        }
    }

    public void testRenderersNamePropertyHasPriorityOverNodeName() throws IOException, RepositoryException {
        Content node3 = getConfigNode(CONFIGNODE3, "/modules/test3/paragraph-renderers");
        final ParagraphRendererManager prm = new ParagraphRendererManager();
        prm.onRegister(node3);
        final Map renderers = prm.getRenderers();
        assertEquals(2, renderers.size());
        // node name is "foo-node" will the name property is set to "foo"
        assertTrue(renderers.get("foo") instanceof DummyParagraphRenderer);
        try {
            prm.getRenderer("foo-node");
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("No paragraph renderer registered with name foo-node", e.getMessage());
        }
        // just a sanity check
        assertTrue(renderers.get("bar") instanceof DummyParagraphRenderer);
    }

    public void testRenderersWithFaultyClassNamesShouldNotBeIgnored() throws IOException, RepositoryException {
        final Content node = getConfigNode(CONFIGNODE4, "/modules/test4/paragraph-renderers");
        final ParagraphRendererManager prm = new ParagraphRendererManager();

        try {
            prm.onRegister(node);
            fail("should have thrown an IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Can't register paragraph renderer with name [foo] and class [com.foo.bar.baz.Gazonk] : java.lang.ClassNotFoundException : com.foo.bar.baz.Gazonk", e.getMessage());
        }
    }

    public static final class DummyParagraphRenderer implements ParagraphRenderer {
        public void render(Content content, Paragraph paragraph, Writer out) throws IOException {
            out.write("tralala:" + paragraph.getName());
        }
    }

    public static final class OtherDummyParagraphRenderer implements ParagraphRenderer {
        public void render(Content content, Paragraph paragraph, Writer out) throws IOException {
            out.write("trululu:" + paragraph.getName());
        }
    }

    private static final String CONFIGNODE1 = "" +
            "modules.test.paragraph-renderers.foo.@type=mgnl:contentNode\n" +
            "modules.test.paragraph-renderers.foo.name=foo\n" +
            "modules.test.paragraph-renderers.foo.class=info.magnolia.cms.beans.config.ParagraphRendererManagerTest$DummyParagraphRenderer\n" +
            "modules.test.paragraph-renderers.bar.@type=mgnl:contentNode\n" +
            "modules.test.paragraph-renderers.bar.name=bar\n" +
            "modules.test.paragraph-renderers.bar.class=info.magnolia.cms.beans.config.ParagraphRendererManagerTest$OtherDummyParagraphRenderer";

    private static final String CONFIGNODE2 = "" +
            "modules.test2.paragraph-renderers.baz.@type=mgnl:contentNode\n" +
            "modules.test2.paragraph-renderers.baz.name=baz\n" +
            "modules.test2.paragraph-renderers.baz.class=info.magnolia.cms.beans.config.ParagraphRendererManagerTest$DummyParagraphRenderer\n" +
            "modules.test2.paragraph-renderers.gazonk.@type=mgnl:contentNode\n" +
            // not specifying the name property should mean usage of the node's name "modules.test2.paragraph-renderers.gazonk.name=gazonk\n" +
            "modules.test2.paragraph-renderers.gazonk.class=info.magnolia.cms.beans.config.ParagraphRendererManagerTest$DummyParagraphRenderer";

    private static final String CONFIGNODE3 = "" +
            "modules.test3.paragraph-renderers.foo-node.@type=mgnl:contentNode\n" +
            "modules.test3.paragraph-renderers.foo-node.name=foo\n" +
            "modules.test3.paragraph-renderers.foo-node.class=info.magnolia.cms.beans.config.ParagraphRendererManagerTest$DummyParagraphRenderer\n" +
            "modules.test3.paragraph-renderers.bar.@type=mgnl:contentNode\n" +
            "modules.test3.paragraph-renderers.bar.name=bar\n" +
            "modules.test3.paragraph-renderers.bar.class=info.magnolia.cms.beans.config.ParagraphRendererManagerTest$DummyParagraphRenderer";

    private static final String CONFIGNODE4 = "" +
            "modules.test4.paragraph-renderers.foo.@type=mgnl:contentNode\n" +
            "modules.test4.paragraph-renderers.foo.name=foo\n" +
            "modules.test4.paragraph-renderers.foo.class=com.foo.bar.baz.Gazonk";


    private Content getConfigNode(String configNode, String path) throws IOException, RepositoryException {
        return MockUtil.createHierarchyManager(configNode).getContent(path);
    }

}
