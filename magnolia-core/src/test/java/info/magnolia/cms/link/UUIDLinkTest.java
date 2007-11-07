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
package info.magnolia.cms.link;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.util.BaseLinkTest;
import info.magnolia.cms.util.ContentUtil;
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

    protected static final String UUID_PATTNER_BINARY = MessageFormat.format(UUID_PATTNER_NEW_FORMAT, new String[]{"2", ContentRepository.WEBSITE, "/parent/sub", "file", "jpg"});

    protected static final String UUID_PATTNER_SIMPLE_OLD_FORMAT = MessageFormat.format(UUID_PATTNER_OLD_FORMAT, new String[]{"2", ContentRepository.WEBSITE, "/parent/sub"});


    protected static final PathToLinkTransformer NOP_TRANSFORMER = new AbsolutePathTransformer(false, false, false);

    public void testParseFromUUIDPattern() throws Exception {
        UUIDLink link = new UUIDLink().parseUUIDLink(UUID_PATTNER_SIMPLE);

        assertEquals(HREF_ABSOLUTE_LINK, NOP_TRANSFORMER.transform(link));
        assertEquals(UUID_PATTNER_SIMPLE, link.toPattern());
    }

    public void testParseLink() throws Exception {
        UUIDLink link = new UUIDLink().parseLink(HREF_ABSOLUTE_LINK);

        assertEquals(ContentRepository.WEBSITE, link.getRepository());
        assertEquals(HANDLE_PARENT_SUB, link.getHandle());
        assertEquals(UUID_PATTNER_SIMPLE, link.toPattern());
    }

    public void testParseFromBrowserLink() throws Exception {
        UUIDLink link = new UUIDLink().parseLink(HREF_ABSOLUTE_LINK);

        assertEquals(HREF_ABSOLUTE_LINK, NOP_TRANSFORMER.transform(link));

        assertEquals(UUID_PATTNER_SIMPLE, link.toPattern());
    }

    public void testLinkWithAnchor() throws Exception{
        UUIDLink link = new UUIDLink().parseLink(HREF_ABSOLUTE_LINK + "#bar");
        assertEquals(UUID_PATTNER_SIMPLE + "#bar", link.toPattern());

        link = new UUIDLink().parseUUIDLink(UUID_PATTNER_SIMPLE + "#bar");
        assertEquals(HREF_ABSOLUTE_LINK + "#bar", NOP_TRANSFORMER.transform(link));
    }

    public void testLinkWithParameters() throws Exception {
        UUIDLink link = new UUIDLink().parseLink(HREF_ABSOLUTE_LINK + "?bar=test");
        assertEquals(UUID_PATTNER_SIMPLE + "?bar=test", link.toPattern());

        link = new UUIDLink().parseUUIDLink(UUID_PATTNER_SIMPLE + "?bar=test");
        assertEquals(HREF_ABSOLUTE_LINK + "?bar=test", NOP_TRANSFORMER.transform(link));
    }

    public void testUUIDToAbsoluteLinksAfterRenaming() throws Exception{
        ((MockContent)ContentUtil.getContent(ContentRepository.WEBSITE, "/parent/sub")).setName("subRenamed");
        UUIDLink link = new UUIDLink().parseUUIDLink(UUID_PATTNER_SIMPLE);
        assertEquals("/parent/subRenamed.html", NOP_TRANSFORMER.transform(link));
    }

    public void doTestParsingInternalLinksToBinaries() throws Exception {
        UUIDLink link = new UUIDLink().parseLink(HREF_BINARY);
        assertEquals(UUID_PATTNER_BINARY, link.toPattern());
    }

    public void testUUIDToBinary() throws Exception {
        UUIDLink link = new UUIDLink().parseUUIDLink(UUID_PATTNER_BINARY);
        assertEquals(HREF_BINARY, NOP_TRANSFORMER.transform(link));
    }

    public void testUUIDToBinaryAfterRenaming() throws Exception {
        // now rename the the page
        ((MockContent)ContentUtil.getContent(ContentRepository.WEBSITE, "/parent/sub")).setName("subRenamed");

        UUIDLink link = new UUIDLink().parseUUIDLink(UUID_PATTNER_BINARY);
        assertEquals(StringUtils.replace(HREF_BINARY, "sub", "subRenamed"), NOP_TRANSFORMER.transform(link));
    }

}
