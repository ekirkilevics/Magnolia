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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.test.mock.MockContent;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ContentUtilTest extends TestCase {

    public void testVisitShouldPassFilterAlong() throws Exception {
        final ItemType foo = new ItemType("foo");
        final ItemType bar = new ItemType("bar");
        final MockContent root = new MockContent("root", foo);
        final MockContent child1 = new MockContent("child1", foo);
        root.addContent(child1);
        final MockContent child2 = new MockContent("child2", bar);
        root.addContent(child2);
        final MockContent child2child = new MockContent("child2-child", foo);
        child2.addContent(child2child);
        final MockContent child3 = new MockContent("child3", foo);
        root.addContent(child3);
        final MockContent child3childFoo = new MockContent("child3-childFoo", foo);
        child3.addContent(child3childFoo);
        final MockContent child3childFoo2 = new MockContent("child3-childFoo2", foo);
        child3.addContent(child3childFoo2);
        final MockContent child3childFooChild = new MockContent("child3-childFoo-child", foo);
        child3childFoo.addContent(child3childFooChild);
        final MockContent child3childBar = new MockContent("child3-childBar", bar);
        child3.addContent(child3childBar);
        final MockContent child4 = new MockContent("child4", bar);
        root.addContent(child4);
        final MockContent child5 = new MockContent("child5", foo);
        root.addContent(child5);
        final ContentTypeRejector filter = new ContentTypeRejector("bar");
        final ContentUtil.Visitor visitor = createStrictMock(ContentUtil.Visitor.class);

        visitor.visit(root);
        visitor.visit(child1);
        visitor.visit(child3);
        visitor.visit(child3childFoo);
        visitor.visit(child3childFooChild);
        visitor.visit(child3childFoo2);
        visitor.visit(child5);

        replay(visitor);
        ContentUtil.visit(root, visitor, filter);
        verify(visitor);
    }

    private final static class ContentTypeRejector implements Content.ContentFilter {
        private final List<String> rejectedTypes;

        public ContentTypeRejector(String... rejectedTypes) {
            this.rejectedTypes = Arrays.asList(rejectedTypes);
        }

        public boolean accept(Content content) {
            try {
                return !rejectedTypes.contains(content.getNodeTypeName());
            } catch (RepositoryException e) {
                throw new RuntimeException(e); // TODO
            }
        }
    }

}
