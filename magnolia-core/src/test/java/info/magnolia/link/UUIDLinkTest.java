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
package info.magnolia.link;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.link.AbsolutePathTransformer;
import info.magnolia.link.Link;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.link.LinkTransformer;
import info.magnolia.test.mock.MockContent;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;


/**
 * @author gjoseph
 * @version $Revision$ ($Author$)
 */
public class UUIDLinkTest extends BaseLinkTest {

    protected static final String HREF_BINARY = "/parent/sub/file/test.jpg";

    protected static final String HREF_BINARY_WITHOUT_NAME = "/parent/sub/file.jpg";

    protected static final String UUID_PATTERN_BINARY = MessageFormat.format(UUID_PATTERN_NEW_FORMAT, new String[]{"2", ContentRepository.WEBSITE, "/parent/sub", "file", "jpg"});

    protected static final String UUID_PATTERN_SIMPLE_OLD_FORMAT = MessageFormat.format(UUID_PATTERN_OLD_FORMAT, new String[]{"2", ContentRepository.WEBSITE, "/parent/sub"});


    protected static final LinkTransformer NOP_TRANSFORMER = new AbsolutePathTransformer(false, false, false);

    public void testParseFromUUIDPattern() throws Exception {
        Link link = LinkFactory.parseUUIDLink(UUID_PATTERN_SIMPLE);

        assertEquals(HREF_ABSOLUTE_LINK, NOP_TRANSFORMER.transform(link));
        assertEquals(UUID_PATTERN_SIMPLE, LinkFactory.toPattern(link));
    }

    public void testParseLink() throws Exception {
        Link link = LinkFactory.parseLink(HREF_ABSOLUTE_LINK);

        assertEquals(ContentRepository.WEBSITE, link.getRepository());
        assertEquals(HANDLE_PARENT_SUB, link.getHandle());
        assertEquals(UUID_PATTERN_SIMPLE, LinkFactory.toPattern(link));
    }

    public void testParseFromBrowserLink() throws Exception {
        Link link = LinkFactory.parseLink(HREF_ABSOLUTE_LINK);

        assertEquals(HREF_ABSOLUTE_LINK, NOP_TRANSFORMER.transform(link));

        assertEquals(UUID_PATTERN_SIMPLE, LinkFactory.toPattern(link));
    }

    public void testLinkWithAnchor() throws Exception{
        Link link = LinkFactory.parseLink(HREF_ABSOLUTE_LINK + "#bar");
        assertEquals(UUID_PATTERN_SIMPLE + "#bar", LinkFactory.toPattern(link));

        link = LinkFactory.parseUUIDLink(UUID_PATTERN_SIMPLE + "#bar");
        assertEquals(HREF_ABSOLUTE_LINK + "#bar", NOP_TRANSFORMER.transform(link));
    }

    public void testLinkWithParameters() throws Exception {
        Link link = LinkFactory.parseLink(HREF_ABSOLUTE_LINK + "?bar=test");
        assertEquals(UUID_PATTERN_SIMPLE + "?bar=test", LinkFactory.toPattern(link));

        link = LinkFactory.parseUUIDLink(UUID_PATTERN_SIMPLE + "?bar=test");
        assertEquals(HREF_ABSOLUTE_LINK + "?bar=test", NOP_TRANSFORMER.transform(link));
    }

    public void testUUIDToAbsoluteLinksAfterRenaming() throws Exception{
        ((MockContent)ContentUtil.getContent(ContentRepository.WEBSITE, "/parent/sub")).setName("subRenamed");
        Link link = LinkFactory.parseUUIDLink(UUID_PATTERN_SIMPLE);
        assertEquals("/parent/subRenamed.html", NOP_TRANSFORMER.transform(link));
    }
    
    public void doTestParsingInternalLinksToBinaries() throws Exception {
        Link link = LinkFactory.parseLink(HREF_BINARY);
        assertEquals(UUID_PATTERN_BINARY, LinkFactory.toPattern(link));
    }

    public void testUUIDToBinary() throws Exception {
        Link link = LinkFactory.parseUUIDLink(UUID_PATTERN_BINARY);
        assertEquals(HREF_BINARY, NOP_TRANSFORMER.transform(link));
    }

    public void testUUIDToBinaryAfterRenaming() throws Exception {
        // now rename the the page
        ((MockContent)ContentUtil.getContent(ContentRepository.WEBSITE, "/parent/sub")).setName("subRenamed");

        Link link = LinkFactory.parseUUIDLink(UUID_PATTERN_BINARY);
        assertEquals(StringUtils.replace(HREF_BINARY, "sub", "subRenamed"), NOP_TRANSFORMER.transform(link));
    }

}
