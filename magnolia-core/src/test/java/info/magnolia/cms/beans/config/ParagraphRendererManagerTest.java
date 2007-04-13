/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
import java.io.StringWriter;
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
        assertTrue(renderers.get("bar") instanceof ParagraphRenderer);

        prm.onRegister(node2);
        assertEquals(4, prm.getRenderers().size());
        assertEquals(4, renderers.size());
        assertEquals(renderers, prm.getRenderers());
        assertTrue(renderers.get("baz") instanceof ParagraphRenderer);
        assertTrue(renderers.get("gazonk") instanceof ParagraphRenderer);

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
        assertTrue(renderers.get("bar") instanceof ParagraphRenderer);

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
        assertTrue(renderers.get("foo") instanceof ParagraphRenderer);
        try {
            prm.getRenderer("foo-node");
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("No paragraph renderer registered with name foo-node", e.getMessage());
        }
        // just a sanity check
        assertTrue(renderers.get("bar") instanceof ParagraphRenderer);
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

    public void testRenderCallsTheAppropriateRenderer() throws IOException, RepositoryException {
        final ParagraphRendererManager prm = new ParagraphRendererManager();
        prm.onRegister(getConfigNode(CONFIGNODE1, "/modules/test/paragraph-renderers"));

        final Paragraph tra = new Paragraph();
        tra.setName("para-one");
        tra.setType("foo");
        final StringWriter res = new StringWriter();
        prm.render(tra, res);
        assertEquals("tralala:para-one", res.toString());

        final Paragraph tru = new Paragraph();
        tru.setName("para-two");
        tru.setType("bar");
        final StringWriter res2 = new StringWriter();
        prm.render(tru, res2);
        assertEquals("trülülü:para-two", res2.toString());
    }

    public static final class DummyParagraphRenderer implements ParagraphRenderer {
        public void render(Paragraph paragraph, Writer out) throws IOException {
            out.write("tralala:" + paragraph.getName());
        }
    }

    public static final class OtherDummyParagraphRenderer implements ParagraphRenderer {
        public void render(Paragraph paragraph, Writer out) throws IOException {
            out.write("trülülü:" + paragraph.getName());
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
