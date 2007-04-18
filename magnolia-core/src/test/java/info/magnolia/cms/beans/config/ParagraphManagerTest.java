package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ParagraphManagerTest extends MgnlTestCase {
    public void testJspIsDefaultTypeIfNotSpecified() throws IOException, RepositoryException {
        final Content paraNode = MockUtil.createHierarchyManager(PARA_NOTYPE).getContent("/modules/test/paragraph/foo");
        final ParagraphManager pm = new ParagraphManager();
        pm.addParagraphToCache(paraNode);
        final Paragraph p = pm.getInfo("foo");
        assertEquals("jsp", p.getType());
        assertEquals("foo", p.getName());
    }

    // TODO : this is not implemented at all - no checks are done on the paragraph config node
//    public void testShouldNotBeAbleToAddAnIrrelevantNode() throws IOException, RepositoryException {
//        final Content n = MockUtil.createHierarchyManager(PARA_TEST).getContent("/modules/test/paragraph");
//        final ParagraphManager pm = new ParagraphManager();
//        pm.addParagraphToCache(n);
//        assertEquals(0, pm.getParagraphs().size());
//    }
//
//    public void testShouldNotBeAbleToRegisterTwoParagraphsWithIdenticalNames() {
//        fail("not implemented yet");
//    }
//
//    public void testExceptionOnUnknownParagraph ?() {
//        fail("not implemented yet");
//    }

    public void testShouldUseNodeNameIfNoNameProperty() throws IOException, RepositoryException {
        final Content paraNode = MockUtil.createHierarchyManager(PARA_NONAME).getContent("/modules/test/paragraph/noname");
        final ParagraphManager pm = new ParagraphManager();
        pm.addParagraphToCache(paraNode);
        final Paragraph p = pm.getInfo("noname");
        assertEquals("noname", p.getName());
        assertEquals("jsp", p.getType());
    }

    public void testNamePropertyShouldPrevailOverNodeName() throws IOException, RepositoryException {
        final Content n = MockUtil.createHierarchyManager(PARA_TEST).getContent("/modules/test/paragraph");
        final ParagraphManager pm = new ParagraphManager();
        pm.onRegister(n);
        Paragraph baz = pm.getInfo("baz");
        assertEquals("baz", baz.getName());
        assertEquals(null, pm.getInfo("bar"));
        Paragraph foo = pm.getInfo("foo");
        assertEquals("foo", foo.getName());
    }

    public void testDialogNameShouldBeEqualsToParagraphNameIfNotSpecified() throws IOException, RepositoryException {
        final Content n = MockUtil.createHierarchyManager(PARA_TEST).getContent("/modules/test/paragraph");
        final Content c2 = MockUtil.createHierarchyManager(PARA_NONAME).getContent("/modules/test/paragraph");
        final ParagraphManager pm = new ParagraphManager();
        pm.onRegister(n);
        pm.onRegister(c2);

        final Paragraph baz = pm.getInfo("foo");
        assertEquals("foo", baz.getName());
        assertEquals("fooDialog", baz.getDialog());

        final Paragraph bar = pm.getInfo("baz");
        assertEquals("baz", bar.getName());
        assertEquals("baz", bar.getDialog());

        final Paragraph noname = pm.getInfo("noname");
        assertEquals("noname", noname.getName());
        assertEquals("noname", noname.getDialog());
    }

    private static final String PARA_NOTYPE = "" +
            "modules.test.paragraph.foo.@type=mgnl:contentNode\n" +
            "modules.test.paragraph.foo.name=foo";

    private static final String PARA_NONAME = "" +
            "modules.test.paragraph.noname.@type=mgnl:contentNode";

    private static final String PARA_TEST = "" +
            //"modules.test.paragraph.@type=mgnl:contentNode\n" +
            "modules.test.paragraph.foo.@type=mgnl:contentNode\n" +
            "modules.test.paragraph.foo.name=foo\n" +
            "modules.test.paragraph.foo.type=jsp\n" +
            "modules.test.paragraph.foo.dialog=fooDialog\n" +
            "modules.test.paragraph.bar.@type=mgnl:contentNode\n" +
            "modules.test.paragraph.bar.name=baz\n" +
            "modules.test.paragraph.bar.type=jsp";

}
