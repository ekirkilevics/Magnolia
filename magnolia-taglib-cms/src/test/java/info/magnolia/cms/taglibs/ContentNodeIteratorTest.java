package info.magnolia.cms.taglibs;

import java.util.ArrayList;

import javax.servlet.jsp.jstl.core.LoopTagStatus;
import javax.servlet.jsp.tagext.IterationTag;

import info.magnolia.cms.util.Resource;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContent;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockPageContext;
import com.mockrunner.mock.web.MockServletConfig;

public class ContentNodeIteratorTest extends MgnlTestCase {

    ContentNodeIterator cni;
    MockPageContext pc;
    ArrayList items;

    protected void setUp() throws Exception {
        super.setUp();
        cni = new ContentNodeIterator();
        MockHttpServletRequest req = new MockHttpServletRequest();
        pc = new MockPageContext(new MockServletConfig(),
                req, new MockHttpServletResponse());
        cni.setPageContext(pc);
        cni.setVarStatus("testStatus");
//        cni.setContentNodeCollectionName(colName);
        items = new ArrayList();
        for (int i = 0; i < 10; i++) {
            items.add(new MockContent("mc" + i));
        }
        cni.setItems(items);
    }

    public void testDoStartTag() {
        assertNull(pc.getAttribute("testStatus"));
        cni.doStartTag();
        LoopTagStatus lts = (LoopTagStatus) pc.getAttribute("testStatus");
        assertNotNull(lts);
        assertEquals("Count has to be 1 after call to doStartTag", 1, lts.getCount());
        assertEquals("Zero based index", 0, lts.getIndex());

        int count = 1;
        while (count < items.size() ) {
            assertEquals(IterationTag.EVAL_BODY_AGAIN, cni.doAfterBody());
            assertEquals(items.get(count), Resource.getLocalContentNode());
            count++;
        }
        // after the last. skip!
        assertEquals(IterationTag.SKIP_BODY, cni.doAfterBody());
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
        while (count < items.size() ) {
            assertEquals(IterationTag.EVAL_BODY_AGAIN, cni.doAfterBody());
            assertEquals(items.get(count), Resource.getLocalContentNode());
            count++;
        }
        // after the last. skip!
        assertEquals(IterationTag.SKIP_BODY, cni.doAfterBody());
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

        int count =  1;
        while (count < end + 1 ) {
            assertEquals(IterationTag.EVAL_BODY_AGAIN, cni.doAfterBody());
            assertEquals(items.get(count), Resource.getLocalContentNode());
            count++;
        }
        // after the last. skip!
        assertEquals(IterationTag.SKIP_BODY, cni.doAfterBody());
    }

}
