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
package info.magnolia.cms.gui.dialog;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import javax.servlet.http.HttpServletRequest;
import javax.jcr.RepositoryException;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DialogFckEditTest extends TestCase {

    public void testNullsAndBasicCharsAreNotTouchedForJS() {
        assertEquals("foo bar", new DialogFckEdit().escapeJsValue("foo bar"));
        assertNull(new DialogFckEdit().escapeJsValue(null));
    }

    public void testQuotesAreEscapedForJS() {
        assertEquals("foo\\'bar", new DialogFckEdit().escapeJsValue("foo'bar"));
        assertEquals("foo\\\"bar", new DialogFckEdit().escapeJsValue("foo\"bar"));
        assertEquals("fo\\'o\\\"bar", new DialogFckEdit().escapeJsValue("fo'o\"bar"));
    }

    public void testNewLinesAreEscapedForJS() {
        assertEquals("foo\\r\\nbar", new DialogFckEdit().escapeJsValue("foo\r\nbar"));
        assertEquals("foo\\nbar", new DialogFckEdit().escapeJsValue("foo\nbar"));
        assertEquals("foo\\rbar", new DialogFckEdit().escapeJsValue("foo\rbar"));
    }

    public void testBackSlashesAreEscapedForJS() {
        assertEquals("foo\\\\bar", new DialogFckEdit().escapeJsValue("foo\\bar"));
        assertEquals("Here is a \\\\backslash for Sean", new DialogFckEdit().escapeJsValue("Here is a \\backslash for Sean"));
    }

    public void testConvertToViewMakesLinksAndImagesPathRelativeToTheContextAndDialogConfig() throws RepositoryException {
        final HttpServletRequest mockReq = createMock(HttpServletRequest.class);
        expect(mockReq.getContextPath()).andReturn("/myContextPath/").times(3);

        final DialogFckEdit d = new DialogFckEdit();
        d.init(mockReq, null, null, null);
        d.setTopParent(d);
        d.setConfig("path", "here");
        replay(mockReq);

        assertEquals("<a href=\"/myContextPath/here/bar\">baz</a>", d.convertToView("<a href=\"foo/bar\">baz</a>"));
        assertEquals("<img src=\"/myContextPath/here/bar.gif\"/>", d.convertToView("<img src=\"foo/bar.gif\"/>"));
        assertEquals("<img src=\"/myContextPath/here/bar.gif\">", d.convertToView("<img src=\"foo/bar.gif\">"));

        verify(mockReq);
    }

    /* TODO  : MAGNOLIA-1133
    public void testConvertToViewMakesLinksAndImagesPathRelativeToTheContext() throws RepositoryException {
        final HttpServletRequest mockReq = createMock(HttpServletRequest.class);
        expect(mockReq.getContextPath()).andReturn("/myContextPath").times(3);

        final DialogFckEdit d = new DialogFckEdit();
        d.init(mockReq, null, null, null);
        d.setTopParent(d);
        //d.setConfig("path", "here");
        replay(mockReq);

        assertEquals("<a href=\"/myContextPath/foo/bar\">baz</a>", d.convertToView("<a href=\"foo/bar\">baz</a>"));
        assertEquals("<a href=\"/myContextPath/foo/bar/yabadabadoo\">baz</a>", d.convertToView("<a href=\"foo/bar/yabadabadoo\">baz</a>"));
        assertEquals("<img src=\"/myContextPath/foo/bar.gif\"/>", d.convertToView("<img src=\"foo/bar.gif\"/>"));
        assertEquals("<img src=\"/myContextPath/foo/bar.gif\">", d.convertToView("<img src=\"foo/bar.gif\">"));

        verify(mockReq);
    }
    */

    public void testConvertToViewDoesNotImpactAbsoluteAndExternalLinksAndImages() throws RepositoryException {
        final HttpServletRequest mockReq = createMock(HttpServletRequest.class);

        final DialogFckEdit d = new DialogFckEdit();
        d.init(mockReq, null, null, null);
        d.setTopParent(d);
        replay(mockReq);

        assertEquals("<a href=\"/foo/bar\">baz</a>", d.convertToView("<a href=\"/foo/bar\">baz</a>"));
        assertEquals("<a href=\"http://foo.com/bar\">baz</a>", d.convertToView("<a href=\"http://foo.com/bar\">baz</a>"));
        assertEquals("<a href=\"https://foo.com/bar\">baz</a>", d.convertToView("<a href=\"https://foo.com/bar\">baz</a>"));
        assertEquals("<img src=\"/foo/bar.gif\"/>", d.convertToView("<img src=\"/foo/bar.gif\"/>"));
        assertEquals("<img src=\"http://foo.com/bar/baz.gif\">", d.convertToView("<img src=\"http://foo.com/bar/baz.gif\">"));
        assertEquals("<img src=\"https://foo.com/bar/baz.gif\">", d.convertToView("<img src=\"https://foo.com/bar/baz.gif\">"));

        verify(mockReq);
    }

    public void testConvertToViewDoesNotConvertMailtoLinks() throws RepositoryException {
        final HttpServletRequest mockReq = createMock(HttpServletRequest.class);

        final DialogFckEdit d = new DialogFckEdit();
        d.init(mockReq, null, null, null);
        d.setTopParent(d);
        replay(mockReq);

        assertEquals("<a href=\"mailto:lieutenant@columbo.net\">mail me !</a>", d.convertToView("<a href=\"mailto:lieutenant@columbo.net\">mail me !</a>"));

        verify(mockReq);
    }

    public void testInternalRelativeLinksAreProperlyDetermined() {
        final DialogFckEdit d = new DialogFckEdit();
        assertTrue(d.isInternalRelativeLink("foo"));
        assertTrue(d.isInternalRelativeLink("foo/bar"));
        assertTrue(d.isInternalRelativeLink("foo/bar.gif"));

        assertFalse(d.isInternalRelativeLink("/foo"));
        assertFalse(d.isInternalRelativeLink("/foo/bar"));
        assertFalse(d.isInternalRelativeLink("/foo/bar.gif"));
        
        assertFalse(d.isInternalRelativeLink("http://foo.com/bar.gif"));
        assertFalse(d.isInternalRelativeLink("http://foo.com/bar/baz.gif"));
        assertFalse(d.isInternalRelativeLink("http://foo.com/bar/"));
        assertFalse(d.isInternalRelativeLink("http://foo.com/bar"));
        assertFalse(d.isInternalRelativeLink("http://foo.com/"));
        assertFalse(d.isInternalRelativeLink("http://foo.com"));
        assertFalse(d.isInternalRelativeLink("https://foo.com"));
        assertFalse(d.isInternalRelativeLink("https://foo.com/bar"));
        assertFalse(d.isInternalRelativeLink("ftp://user:pass@server.com/foo/bar"));

        assertFalse(d.isInternalRelativeLink("mailto:murdock@a-team.org"));

        assertFalse(d.isInternalRelativeLink("#anchor"));
        assertFalse(d.isInternalRelativeLink("#another-anchor"));
    }
}
