/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
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
        String absolutePath = LinkHelper.convertUUIDtoHandle("2", ContentRepository.WEBSITE);
        assertEquals("/parent/sub", absolutePath);
    }
}
