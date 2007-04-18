package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ParagraphRenderingFacadeTest extends MgnlTestCase {

    public void testRenderCallsTheAppropriateRenderer() throws IOException, RepositoryException {
        final ParagraphRendererManager prm = new ParagraphRendererManager();
        prm.onRegister(getNode(CONFIGNODE1_RENDERER, "/modules/test/paragraph-renderers"));
        final ParagraphRenderingFacade prf = new ParagraphRenderingFacade(prm, null);

        final Paragraph tra = new Paragraph();
        tra.setName("para-one");
        tra.setType("foo");
        final StringWriter res = new StringWriter();
        prf.render(null, tra, res);
        assertEquals("tralala:para-one", res.toString());

        final Paragraph tru = new Paragraph();
        tru.setName("para-two");
        tru.setType("bar");
        final StringWriter res2 = new StringWriter();
        prf.render(null, tru, res2);
        assertEquals("trülülü:para-two", res2.toString());
    }

    public void testUsesTheAppropriateParagraphWhenNotExplicitelyPassed() throws IOException, RepositoryException {
        final ParagraphRendererManager prm = new ParagraphRendererManager();
        final ParagraphManager pm = new ParagraphManager();
        final ParagraphRenderingFacade prf = new ParagraphRenderingFacade(prm, pm);

        prm.onRegister(getNode(CONFIGNODE1_RENDERER, "/modules/test/paragraph-renderers"));
        final Content content = getNode(CONTENTNODE1, "/foo/bar/MyPage");

        try {
            prf.render(content, new StringWriter());
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("Paragraph para1 not found for page /foo/bar/MyPage", e.getMessage());
        }

        // now register the paragraphs
        pm.addParagraphToCache(getNode(CONFIG_PARAGRAPH1, "modules/test/paragraph/para1"));
        pm.addParagraphToCache(getNode(CONFIG_PARAGRAPH2, "modules/test/paragraph/para2"));
        final StringWriter out = new StringWriter();
        prf.render(content, out);
        assertEquals("tralala:para1", out.toString());
    }

    private static final String CONFIGNODE1_RENDERER = "" +
            "modules.test.paragraph-renderers.foo.@type=mgnl:contentNode\n" +
            "modules.test.paragraph-renderers.foo.name=foo\n" +
            "modules.test.paragraph-renderers.foo.class=info.magnolia.cms.beans.config.ParagraphRendererManagerTest$DummyParagraphRenderer\n" +
            "modules.test.paragraph-renderers.bar.@type=mgnl:contentNode\n" +
            "modules.test.paragraph-renderers.bar.name=bar\n" +
            "modules.test.paragraph-renderers.bar.class=info.magnolia.cms.beans.config.ParagraphRendererManagerTest$OtherDummyParagraphRenderer";

    private static final String CONFIG_PARAGRAPH1 = "" +
            "modules.test.paragraph.para1.@type=mgnl:contentNode\n" +
            "modules.test.paragraph.para1.name=para1\n" +
            "modules.test.paragraph.para1.type=foo";

    private static final String CONFIG_PARAGRAPH2 = "" +
            "modules.test.paragraph.para2.@type=mgnl:contentNode\n" +
            "modules.test.paragraph.para2.name=para2\n" +
            "modules.test.paragraph.para2.type=foo";

    private static final String CONTENTNODE1 = "" +
            "foo.bar.MyPage.text=hello\n" +
            "foo.bar.MyPage.MetaData.mgnl\\:template=para1";

    private Content getNode(String configNode, String path) throws IOException, RepositoryException {
        return MockUtil.createHierarchyManager(configNode).getContent(path);
    }
}
