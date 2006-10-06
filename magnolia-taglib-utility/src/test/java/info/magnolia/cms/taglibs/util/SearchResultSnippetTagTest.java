package info.magnolia.cms.taglibs.util;

import junit.framework.TestCase;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class SearchResultSnippetTagTest extends TestCase {

    public void testStripHtmlSimple() {
        String html = "<div>uh!</div>";
        String text = "uh!";
        String result = new SearchResultSnippetTag().stripHtmlTags(html);
        assertEquals(text, result);
    }

    public void testStripHtmlEmptyTag() {
        String html = "<div>uh!<br/></div>";
        String text = "uh!";
        String result = new SearchResultSnippetTag().stripHtmlTags(html);
        assertEquals(text, result);
    }

    public void testStripHtmlNewLines() {
        String html = "<div\n class=\"abc\">uh!</div>";
        String text = "uh!";
        String result = new SearchResultSnippetTag().stripHtmlTags(html);
        assertEquals(text, result);
    }

    public void testStripHtmlMultipleNewLines() {
        String html = "<div\n class=\"abc\"\n style=\"abc\">uh!</div>";
        String text = "uh!";
        String result = new SearchResultSnippetTag().stripHtmlTags(html);
        assertEquals(text, result);
    }

    public void testStripHtmlNewLineAsLast() {
        String html = "<div\n class=\"abc\"\n style=\"abc\"\n>uh!</div>";
        String text = "uh!";
        String result = new SearchResultSnippetTag().stripHtmlTags(html);
        assertEquals(text, result);
    }
}
