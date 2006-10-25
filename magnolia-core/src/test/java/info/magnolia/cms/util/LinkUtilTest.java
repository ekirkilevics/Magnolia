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
package info.magnolia.cms.util;

import junit.framework.TestCase;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class LinkUtilTest extends TestCase {
    public void testInternalRelativeLinksAreProperlyDetermined() {
        assertTrue(LinkUtil.isInternalRelativeLink("foo"));
        assertTrue(LinkUtil.isInternalRelativeLink("foo/bar"));
        assertTrue(LinkUtil.isInternalRelativeLink("foo/bar.gif"));

        assertFalse(LinkUtil.isInternalRelativeLink("/foo"));
        assertFalse(LinkUtil.isInternalRelativeLink("/foo/bar"));
        assertFalse(LinkUtil.isInternalRelativeLink("/foo/bar.gif"));

        assertFalse(LinkUtil.isInternalRelativeLink("http://foo.com/bar.gif"));
        assertFalse(LinkUtil.isInternalRelativeLink("http://foo.com/bar/baz.gif"));
        assertFalse(LinkUtil.isInternalRelativeLink("http://foo.com/bar/"));
        assertFalse(LinkUtil.isInternalRelativeLink("http://foo.com/bar"));
        assertFalse(LinkUtil.isInternalRelativeLink("http://foo.com/"));
        assertFalse(LinkUtil.isInternalRelativeLink("http://foo.com"));
        assertFalse(LinkUtil.isInternalRelativeLink("https://foo.com"));
        assertFalse(LinkUtil.isInternalRelativeLink("https://foo.com/bar"));
        assertFalse(LinkUtil.isInternalRelativeLink("ftp://user:pass@server.com/foo/bar"));

        assertFalse(LinkUtil.isInternalRelativeLink("mailto:murdock@a-team.org"));

        assertFalse(LinkUtil.isInternalRelativeLink("#anchor"));
        assertFalse(LinkUtil.isInternalRelativeLink("#another-anchor"));

        assertFalse(LinkUtil.isInternalRelativeLink("javascript:void(window.open('http://www.google.com','','resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,fullscreen=no,dependent=no,width=200,height=200'))"));
        assertFalse(LinkUtil.isInternalRelativeLink("javascript:void(window.open('/foo/bar','','resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,fullscreen=no,dependent=no,width=200,height=200'))"));
    }
}
