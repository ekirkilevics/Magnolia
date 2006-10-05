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
}
