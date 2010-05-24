/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.fckeditor.dialogs;

import info.magnolia.cms.gui.dialog.DialogControlImpl;
import info.magnolia.link.BaseLinkTest;
import static org.easymock.EasyMock.*;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

/**
 * @author gjoseph
 * @version $Revision$ ($Author$)
 */
public class FckEditorDialogTest extends BaseLinkTest {

    /**
     *
     */
    protected static final String SOME_PATH = "/some/path/to/here";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        replay(allMocks.toArray());
    }

    @Override
    protected void tearDown() throws Exception {
        verify(allMocks.toArray());
        super.tearDown();
    }

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

    public void testConvertToViewShouldConvertFormerImageLinks() throws RepositoryException {
        final FckEditorDialogForTest d = new FckEditorDialogForTest();
        d.init(null, null, null, null);
        d.setTopParent(d);
        d.setConfig("path", SOME_PATH);

        assertEquals("<a href=\"" + SOME_CONTEXT + SOME_PATH + "/field/image.jpg\">baz</a>", d.convertToView("<a href=\"page/field/image.jpg\">baz</a>"));
    }

    public void testConvertToViewShouldNotConvertNonUUIDLinks() throws RepositoryException {
        final FckEditorDialogForTest d = new FckEditorDialogForTest();
        d.init(null, null, null, null);
        d.setTopParent(d);
        d.setConfig("path", SOME_PATH);

        assertEquals("<img src=\"/foo/bar.gif\"/>", d.convertToView("<img src=\"/foo/bar.gif\"/>"));
    }

    public void testConvertToViewDoesNotImpactAbsoluteAndExternalLinksAndImages() throws RepositoryException {
        final HttpServletRequest mockReq = createMock(HttpServletRequest.class);

        final FckEditorDialogForTest d = new FckEditorDialogForTest();
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

    /* MAGNOLIA-2768, MAGNOLIA-2862, MAGNOLIA-2865, MAGNOLIA-2867 */
    public void testLinksToRemovedPageAreStillDisplayed() throws Exception {
        final FckEditorDialogForTest d = new FckEditorDialogForTest();
        d.init(null, null, null, null);
        d.setTopParent(d);
        d.setConfig("path", SOME_PATH);

        final String input = "<p><strong>Some</strong> <em><a href=\"${link:{uuid:{unexi-sting-uuid},repository:{website},handle:{/unexi/sting/path},nodeData:{},extension:{html}}}\">formatted</a></em> text.</p>";

        assertEquals("<p><strong>Some</strong> <em><a href=\"/unexi/sting/path.html\">formatted</a></em> text.</p>", d.convertToView(input));
    }

    public void testConvertToViewDoesNotConvertMailtoLinks() throws RepositoryException {
        final HttpServletRequest mockReq = createMock(HttpServletRequest.class);

        final FckEditorDialogForTest d = new FckEditorDialogForTest();
        d.init(mockReq, null, null, null);
        d.setTopParent(d);
        replay(mockReq);

        assertEquals("<a href=\"mailto:lieutenant@columbo.net\">mail me !</a>", d.convertToView("<a href=\"mailto:lieutenant@columbo.net\">mail me !</a>"));

        verify(mockReq);
    }

    // just makes setTopParent available for tests ...
    private final class FckEditorDialogForTest extends FckEditorDialog {
        public void setTopParent(DialogControlImpl top) {
            super.setTopParent(top);
        }
    }
}
