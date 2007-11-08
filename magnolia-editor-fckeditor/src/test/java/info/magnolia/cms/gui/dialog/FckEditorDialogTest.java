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

import info.magnolia.cms.util.BaseLinkTest;
import info.magnolia.module.fckeditor.dialogs.FckEditorDialog;
import static org.easymock.EasyMock.*;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

/**
 * @author gjoseph
 * @version $Revision$ ($Author$)
 */
public class FckEditorDialogTest extends BaseLinkTest {

    public void testNullsAndBasicCharsAreNotTouchedForJS() {
        assertEquals("foo bar", new FckEditorDialog().escapeJsValue("foo bar"));
        assertNull(new FckEditorDialog().escapeJsValue(null));
    }

    public void testQuotesAreEscapedForJS() {
        assertEquals("foo\\'bar", new FckEditorDialog().escapeJsValue("foo'bar"));
        assertEquals("foo\\\"bar", new FckEditorDialog().escapeJsValue("foo\"bar"));
        assertEquals("fo\\'o\\\"bar", new FckEditorDialog().escapeJsValue("fo'o\"bar"));
    }

    public void testNewLinesAreEscapedForJS() {
        assertEquals("foo\\r\\nbar", new FckEditorDialog().escapeJsValue("foo\r\nbar"));
        assertEquals("foo\\nbar", new FckEditorDialog().escapeJsValue("foo\nbar"));
        assertEquals("foo\\rbar", new FckEditorDialog().escapeJsValue("foo\rbar"));
    }

    public void testBackSlashesAreEscapedForJS() {
        assertEquals("foo\\\\bar", new FckEditorDialog().escapeJsValue("foo\\bar"));
        assertEquals("Here is a \\\\backslash for Sean", new FckEditorDialog().escapeJsValue("Here is a \\backslash for Sean"));
    }

    public void testConvertToViewShouldNotConvertNonUUIDLinks() throws RepositoryException {
        final FckEditorDialog d = new FckEditorDialog();
        d.init(null, null, null, null);
        d.setTopParent(d);
        d.setConfig("path", "here");

        assertEquals("<a href=\"foo/bar\">baz</a>", d.convertToView("<a href=\"foo/bar\">baz</a>"));
        assertEquals("<img src=\"/foo/bar.gif\"/>", d.convertToView("<img src=\"/foo/bar.gif\"/>"));
        assertEquals("<img src=\"../here/bar.gif\">", d.convertToView("<img src=\"../here/bar.gif\">"));
    }

    public void testConvertToViewDoesNotImpactAbsoluteAndExternalLinksAndImages() throws RepositoryException {
        final HttpServletRequest mockReq = createMock(HttpServletRequest.class);

        final FckEditorDialog d = new FckEditorDialog();
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

        final FckEditorDialog d = new FckEditorDialog();
        d.init(mockReq, null, null, null);
        d.setTopParent(d);
        replay(mockReq);

        assertEquals("<a href=\"mailto:lieutenant@columbo.net\">mail me !</a>", d.convertToView("<a href=\"mailto:lieutenant@columbo.net\">mail me !</a>"));

        verify(mockReq);
    }
}
