/**
 * This file Copyright (c) 2007-2010 Magnolia International
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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;

import java.util.ArrayList;

import javax.servlet.jsp.jstl.core.LoopTagStatus;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockPageContext;
import com.mockrunner.mock.web.MockServletConfig;


public class ContentNodeIteratorTest extends MgnlTestCase {
    private ContentNodeIterator cni;
    private MockPageContext pc;
    private ArrayList items;

    protected void setUp() throws Exception {
        super.setUp();
        // reinit mock context - this test needs instance of WebContext
        final MockWebContext ctx = new MockWebContext();
        MgnlContext.setInstance(ctx);
        // and system context as well
        ComponentsTestUtil.setInstanceFactory(SystemContext.class, new ComponentFactory() {
            public Object newInstance() {
                return ctx;
            }
        });

        // need some basic i18n config
        MockUtil.createAndSetHierarchyManager(ContentRepository.CONFIG,
            "/server/i18n/content/class=info.magnolia.cms.i18n.DefaultI18nContentSupport\n" +
            "/server/i18n/content/enabled=false\n"
        );
        // init tested tag
        cni = new ContentNodeIterator();
        MockHttpServletRequest req = new MockHttpServletRequest();
        pc = new MockPageContext(new MockServletConfig(), req, new MockHttpServletResponse());
        cni.setPageContext(pc);
        cni.setVarStatus("testStatus");
        // cni.setContentNodeCollectionName(colName);
        items = new ArrayList();
        for (int i = 0; i < 10; i++) {
            items.add(new MockContent("mc" + i));
        }
        cni.setItems(items);
    }

    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        super.tearDown();
    }

    public void testDoStartTag() {
        assertNull(pc.getAttribute("testStatus"));
        cni.doStartTag();
        LoopTagStatus lts = (LoopTagStatus) pc.getAttribute("testStatus");
        assertNotNull(lts);
        assertEquals("Count has to be 1 after call to doStartTag", 1, lts.getCount());
        assertEquals("Zero based index", 0, lts.getIndex());

        int count = 1;
        while (count < items.size()) {
            assertEquals(IterationTag.EVAL_BODY_AGAIN, cni.doAfterBody());
            assertEquals(items.get(count), Resource.getLocalContentNode());
            count++;
        }
        // after the last. skip!
        assertEquals(Tag.SKIP_BODY, cni.doAfterBody());
    }

    public void testDoStartTag2() {
        assertNull(pc.getAttribute("testStatus"));
        int begin = 5;
        cni.setBegin(begin);
        cni.doStartTag();
        LoopTagStatus lts = (LoopTagStatus) pc.getAttribute("testStatus");
        assertNotNull(lts);
        assertEquals("Count has to be 1 after call to doStartTag", 1, lts.getCount());
        assertEquals("Zero based index", begin, lts.getIndex());

        int count = begin + 1;
        while (count < items.size()) {
            assertEquals(IterationTag.EVAL_BODY_AGAIN, cni.doAfterBody());
            assertEquals(items.get(count), Resource.getLocalContentNode());
            count++;
        }
        // after the last. skip!
        assertEquals(Tag.SKIP_BODY, cni.doAfterBody());
    }

    public void testDoStartTag3() {
        assertNull(pc.getAttribute("testStatus"));
        int end = 5;
        cni.setEnd(end);
        cni.doStartTag();
        LoopTagStatus lts = (LoopTagStatus) pc.getAttribute("testStatus");
        assertNotNull(lts);
        assertEquals("Count has to be 1 after call to doStartTag", 1, lts.getCount());
        assertEquals("Zero based index", 0, lts.getIndex());

        int count = 1;
        while (count < end + 1) {
            assertEquals(IterationTag.EVAL_BODY_AGAIN, cni.doAfterBody());
            assertEquals(items.get(count), Resource.getLocalContentNode());
            count++;
        }
        // after the last. skip!
        assertEquals(Tag.SKIP_BODY, cni.doAfterBody());
    }

    /**
     * #MAGNOLIA-1896 - NPE on contentNodeCollectionName not found
     */
    public void testContentNodeCollectionNameNPE() {
        assertNull(pc.getAttribute("testStatus"));
        // NPE thrown when specified collection doesn't exist
        cni.setContentNodeCollectionName("collNameXXX");
        cni.setItems(null);
        MockContent actPage = new MockContent("curActPage");
        MockContent coll = new MockContent("collName");
        for (int i = 0; i < 10; i++) {
            coll.addContent(new MockContent("c" + i));
        }
        actPage.addContent(coll);
        MgnlContext.getWebContext().getAggregationState().setMainContent(actPage);
        assertEquals(Tag.SKIP_BODY, cni.doStartTag());
    }

    public void testContentNodeCollectionName() {
        assertNull(pc.getAttribute("testStatus"));
        cni.setContentNodeCollectionName("collName");
        cni.setItems(null);
        MockContent actPage = new MockContent("curActPage");
        MockContent coll = new MockContent("collName");
        for (int i = 0; i < 10; i++) {
            coll.addContent(new MockContent("c" + i));
        }
        actPage.addContent(coll);
        MgnlContext.getWebContext().getAggregationState().setMainContent(actPage);
        assertEquals(Tag.EVAL_BODY_INCLUDE, cni.doStartTag());
        assertNotNull(Resource.getLocalContentNode());
        assertEquals(IterationTag.EVAL_BODY_AGAIN, cni.doAfterBody());
    }

    public void testGetLastNoEnd() {
        assertNull(pc.getAttribute("testStatus"));
        int begin = 5;
        cni.setBegin(begin);
        cni.doStartTag();
        LoopTagStatus lts = (LoopTagStatus) pc.getAttribute("testStatus");
        assertNotNull(lts);
        assertEquals("Count has to be 1 after call to doStartTag", 1, lts.getCount());
        assertFalse(lts.isLast());

        int count = begin + 1;
        while (count < items.size()) {
            assertEquals(IterationTag.EVAL_BODY_AGAIN, cni.doAfterBody());
            assertEquals(items.get(count), Resource.getLocalContentNode());
            assertEquals(count, lts.getIndex());

            assertEquals(count == items.size() - 1, lts.isLast());
            count++;
        }
        // after the last. skip!
        assertEquals(Tag.SKIP_BODY, cni.doAfterBody());
    }

    public void testNestedCollections() {
        assertNull(pc.getAttribute("testStatus"));
        cni.setContentNodeCollectionName("collName");
        cni.setItems(null);
        MockContent actPage = new MockContent("curActPage");
        MockContent actParagraph = new MockContent("curActPage");
        actPage.addContent(actParagraph);
        MockContent coll = new MockContent("collName");
        for (int i = 0; i < 10; i++) {
            coll.addContent(new MockContent("c" + i));
        }
        actParagraph.addContent(coll);
        MgnlContext.getWebContext().getAggregationState().setMainContent(actPage);
        Resource.setLocalContentNode(actParagraph);
        assertEquals(Tag.EVAL_BODY_INCLUDE, cni.doStartTag());
        assertNotNull(Resource.getLocalContentNode());
        assertEquals(IterationTag.EVAL_BODY_AGAIN, cni.doAfterBody());
    }
}
