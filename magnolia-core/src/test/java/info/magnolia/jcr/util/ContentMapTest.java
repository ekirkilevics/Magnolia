/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.jcr.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.link.LinkTransformerManager;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;

import java.util.Arrays;
import java.util.Calendar;

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for content map functionality.
 *
 * @author had
 *
 */
public class ContentMapTest {

    @Before
    public void setup() {
        ComponentsTestUtil.setImplementation(LinkTransformerManager.class, LinkTransformerManager.class);
    }
    @Test
    public void testGetBasicProps() throws Exception {
        HierarchyManager hm = MockUtil.createHierarchyManager("/bla/prop1=test\n" + "/bla/bla/prop1=test\n" + "/bla/bla/prop2=something\n" + "/bla/bla@uuid=12345\n" + "/bla/bla/jcr/@type=mgnl:contentNode");
        ContentMap map = new ContentMap(hm.getContent("/bla/bla").getJCRNode());
        assertFalse(map.keySet().isEmpty());
        assertTrue(map.containsKey("prop1"));
        assertEquals("something", map.get("prop2"));
        assertTrue(map.get("prop2") instanceof String);
        assertEquals("bla", map.get("@name"));
        assertEquals("/bla/bla", map.get("@handle"));
        assertEquals("/bla/bla", map.get("@path"));
        assertEquals("12345", map.get("@uuid"));
        assertEquals("12345", map.get("@id"));
    }

    // @Test
    public void testGetBinaryProps() throws Exception {
        // FIXME: yes, you!
        String contentProperties = StringUtils.join(Arrays.asList(
                "/somepage/mypage@type=mgnl:content",
                "/somepage/mypage/paragraphs@type=mgnl:contentNode",
                "/somepage/mypage/paragraphs/0@type=mgnl:contentNode",
                "/somepage/mypage/paragraphs/0@type=mgnl:contentNode",

                "/somepage/mypage/paragraphs/0/attachment1@type=mgnl:resource",
                "/somepage/mypage/paragraphs/0/attachment1.fileName=hello",
                "/somepage/mypage/paragraphs/0/attachment1.extension=gif",
                // being a binary node, magnolia knows to store data as jcr:data w/o need to be explicitly told so
                "/somepage/mypage/paragraphs/0/attachment1=binary:X",
                "/somepage/mypage/paragraphs/0/attachment1.jcr\\:mimeType=image/gif",
                "/somepage/mypage/paragraphs/0/attachment1.jcr\\:lastModified=date:2009-10-14T08:59:01.227-04:00",

                "/somepage/mypage/paragraphs/0/attachment2@type=mgnl:resource",
                "/somepage/mypage/paragraphs/0/attachment2.fileName=test",
                "/somepage/mypage/paragraphs/0/attachment2.extension=jpeg",
                "/somepage/mypage/paragraphs/0/attachment2=binary:X",
                "/somepage/mypage/paragraphs/0/attachment2.jcr\\:mimeType=image/jpeg",
                "/somepage/mypage/paragraphs/0/attachment2.jcr\\:lastModified=date:2009-10-14T08:59:01.227-04:00",

                "/somepage/mypage/paragraphs/0/image3@type=mgnl:resource",
                "/somepage/mypage/paragraphs/0/image3.fileName=third",
                "/somepage/mypage/paragraphs/0/image3.extension=png",
                "/somepage/mypage/paragraphs/0/image3=binary:X",
                "/somepage/mypage/paragraphs/0/image3.jcr\\:mimeType=image/png",
                "/somepage/mypage/paragraphs/0/image3.jcr\\:lastModified=date:2009-10-14T08:59:01.227-04:00",

                // and more which should not match
                "/somepage/mypage/paragraphs/0/foo=bar",
                "/somepage/mypage/paragraphs/0/mybool=boolean:true",
                "/somepage/mypage/paragraphs/0/rand@type=mgnl:resource",
                "/somepage/mypage/paragraphs/0/rand.fileName=randdddd",
                "/somepage/mypage/paragraphs/0/rand.extension=png",
                "/somepage/mypage/paragraphs/0/rand=binary:X",
                "/somepage/mypage/paragraphs/0/rand.jcr\\:mimeType=image/png",
                "/somepage/mypage/paragraphs/0/rand.jcr\\:lastModified=date:2009-10-14T08:59:01.227-04:00"
        ), "\n");
        HierarchyManager hm = MockUtil.createHierarchyManager(contentProperties);
        ContentMap map = new ContentMap(hm.getContent("/somepage/mypage/paragraphs/0").getJCRNode());
        assertNotNull(map.get("attachment1"));
        assertTrue(map.get("attachment1") instanceof Node);
        System.out.println(map.get("attachment1").getClass());
    }
    @Test
    public void testGetOtherProps() throws Exception {
        String contentProperties = StringUtils.join(Arrays.asList(
                "/somepage/mypage@type=mgnl:content",
                "/somepage/mypage/paragraphs@type=mgnl:contentNode",
                "/somepage/mypage/paragraphs/0@type=mgnl:contentNode",
                "/somepage/mypage/paragraphs/0@type=mgnl:contentNode",

                // 2 regular props
                "/somepage/mypage/paragraphs/0/attention=booyah",
                "/somepage/mypage/paragraphs/0/imaginary=date:2009-10-14T08:59:01.227-04:00"

        ), "\n");
        HierarchyManager hm = MockUtil.createHierarchyManager(contentProperties);
        ContentMap map = new ContentMap(hm.getContent("/somepage/mypage/paragraphs/0").getJCRNode());
        assertNotNull(map.get("imaginary"));
        assertTrue(map.get("imaginary") instanceof Calendar);
    }
}
