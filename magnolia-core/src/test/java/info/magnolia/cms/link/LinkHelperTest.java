/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.link;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.util.BaseLinkTest;

import java.io.IOException;

import javax.jcr.RepositoryException;

/**
 * @author philipp
 * @version $Id$
 *
 */
public class LinkHelperTest extends BaseLinkTest {

    public void testMakingRelativeLinks() {
        assertEquals("d.html", LinkHelper.makePathRelative("/a/b/c.html", "/a/b/d.html"));
        assertEquals("c/e.html", LinkHelper.makePathRelative("/a/b/c.html", "/a/b/c/e.html"));
        assertEquals("../x/y.html", LinkHelper.makePathRelative("/a/b/c.html", "/a/x/y.html"));
        assertEquals("../../z/x/y.html", LinkHelper.makePathRelative("/a/b/c.html", "/z/x/y.html"));
        assertEquals("../../../b.html", LinkHelper.makePathRelative("/a/b/c/d/e.html", "/a/b.html"));
        assertEquals("a/b.html", LinkHelper.makePathRelative("/a.html", "/a/b.html"));
    }

    public void testInternalRelativeLinksAreProperlyDetermined() {
        assertTrue(LinkHelper.isInternalRelativeLink("foo"));
        assertTrue(LinkHelper.isInternalRelativeLink("foo/bar"));
        assertTrue(LinkHelper.isInternalRelativeLink("foo/bar.gif"));

        assertFalse(LinkHelper.isInternalRelativeLink("/foo"));
        assertFalse(LinkHelper.isInternalRelativeLink("/foo/bar"));
        assertFalse(LinkHelper.isInternalRelativeLink("/foo/bar.gif"));

        assertFalse(LinkHelper.isInternalRelativeLink("http://foo.com/bar.gif"));
        assertFalse(LinkHelper.isInternalRelativeLink("http://foo.com/bar/baz.gif"));
        assertFalse(LinkHelper.isInternalRelativeLink("http://foo.com/bar/"));
        assertFalse(LinkHelper.isInternalRelativeLink("http://foo.com/bar"));
        assertFalse(LinkHelper.isInternalRelativeLink("http://foo.com/"));
        assertFalse(LinkHelper.isInternalRelativeLink("http://foo.com"));
        assertFalse(LinkHelper.isInternalRelativeLink("https://foo.com"));
        assertFalse(LinkHelper.isInternalRelativeLink("https://foo.com/bar"));
        assertFalse(LinkHelper.isInternalRelativeLink("ftp://user:pass@server.com/foo/bar"));

        assertFalse(LinkHelper.isInternalRelativeLink("mailto:murdock@a-team.org"));

        assertFalse(LinkHelper.isInternalRelativeLink("#anchor"));
        assertFalse(LinkHelper.isInternalRelativeLink("#another-anchor"));

        assertFalse(LinkHelper.isInternalRelativeLink("javascript:void(window.open('http://www.google.com','','resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,fullscreen=no,dependent=no,width=200,height=200'))"));
        assertFalse(LinkHelper.isInternalRelativeLink("javascript:void(window.open('/foo/bar','','resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,fullscreen=no,dependent=no,width=200,height=200'))"));
    }

    public void testExternalLinksAreProperlyDetermined() {
        assertFalse(LinkHelper.isExternalLinkOrAnchor("foo"));
        assertFalse(LinkHelper.isExternalLinkOrAnchor("foo/bar"));
        assertFalse(LinkHelper.isExternalLinkOrAnchor("foo/bar.gif"));

        assertFalse(LinkHelper.isExternalLinkOrAnchor("/foo"));
        assertFalse(LinkHelper.isExternalLinkOrAnchor("/foo/bar"));
        assertFalse(LinkHelper.isExternalLinkOrAnchor("/foo/bar.gif"));

        assertTrue(LinkHelper.isExternalLinkOrAnchor("http://foo.com/bar.gif"));
        assertTrue(LinkHelper.isExternalLinkOrAnchor("http://foo.com/bar/baz.gif"));
        assertTrue(LinkHelper.isExternalLinkOrAnchor("http://foo.com/bar/"));
        assertTrue(LinkHelper.isExternalLinkOrAnchor("http://foo.com/bar"));
        assertTrue(LinkHelper.isExternalLinkOrAnchor("http://foo.com/"));
        assertTrue(LinkHelper.isExternalLinkOrAnchor("http://foo.com"));
        assertTrue(LinkHelper.isExternalLinkOrAnchor("https://foo.com"));
        assertTrue(LinkHelper.isExternalLinkOrAnchor("https://foo.com/bar"));
        assertTrue(LinkHelper.isExternalLinkOrAnchor("ftp://user:pass@server.com/foo/bar"));

        assertTrue(LinkHelper.isExternalLinkOrAnchor("mailto:murdock@a-team.org"));

        assertTrue(LinkHelper.isExternalLinkOrAnchor("#anchor"));
        assertTrue(LinkHelper.isExternalLinkOrAnchor("#another-anchor"));

        assertTrue(LinkHelper.isExternalLinkOrAnchor("javascript:void(window.open('http://www.google.com','','resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,fullscreen=no,dependent=no,width=200,height=200'))"));
        assertTrue(LinkHelper.isExternalLinkOrAnchor("javascript:void(window.open('/foo/bar','','resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,fullscreen=no,dependent=no,width=200,height=200'))"));
    }

    public void testMakeAbsolutePathFromUUID() throws IOException, RepositoryException {
        String absolutePath = LinkHelper.convertUUIDtoAbsolutePath("2", ContentRepository.WEBSITE);
        assertEquals("/parent/sub", absolutePath);
    }
}
