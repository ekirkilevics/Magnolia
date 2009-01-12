/**
 * This file Copyright (c) 2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */

package info.magnolia.cms.beans.config;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.jcr.PropertyType;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import junit.framework.TestCase;

/**
 * @author had
 * @version $Id:$
 */
public class URI2RepositoryMappingTest extends TestCase {

    public void testGetUri() throws Exception {
        URI2RepositoryMapping mapping = new URI2RepositoryMapping();
        mapping.setRepository("dummy-repo");
        mapping.setURIPrefix("/blabla/");
        // instance is set only in constructor ...
        final Context context = createStrictMock(Context.class);
        final HierarchyManager hm = createStrictMock(HierarchyManager.class);
        final Content cnt = createStrictMock(Content.class);
        final Content root = createStrictMock(Content.class);
        final NodeData docu = createStrictMock(NodeData.class);
        expect(context.getHierarchyManager("dummy-repo")).andReturn(hm);
        expect(hm.isExist("/Test/image")).andReturn(Boolean.TRUE);
        expect(hm.isNodeData("/Test/image")).andReturn(Boolean.TRUE);
        expect(hm.isNodeData("/Test")).andReturn(Boolean.FALSE);
        expect(hm.isNodeData("/Test/image")).andReturn(Boolean.TRUE);
        expect(hm.getContent("/Test")).andReturn(cnt);
        expect(cnt.getNodeData("image")).andReturn(docu);
        //expect(context.getHierarchyManager("dummy-repo")).andReturn(hm);
        //expect(hm.getContent("/Test")).andReturn(cnt);
        expect(cnt.getUUID()).andReturn("uu-something-real");
        expect(docu.getType()).andReturn(PropertyType.BINARY);
        expect(docu.getAttribute("nodeDataTemplate")).andReturn("");
        expect(docu.getAttribute("extension")).andReturn("jpg");
        expect(docu.getAttribute("fileName")).andReturn("blah");
        expect(docu.getAttribute("contentType")).andReturn("mgnl:resource");
        expect(docu.getAttribute("size")).andReturn("1234");
        expect(docu.getType()).andReturn(PropertyType.BINARY);
        expect(docu.getAttribute("nodeDataTemplate")).andReturn("");
        expect(docu.getAttribute("extension")).andReturn("jpg");
        expect(docu.getAttribute("fileName")).andReturn("blah");
        expect(docu.getAttribute("contentType")).andReturn("mgnl:resource");
        expect(docu.getAttribute("size")).andReturn("1234");
        replay(context, hm, cnt, root, docu);
        MgnlContext.setInstance(context);
        String uri = mapping.getURI("/Test/image");
        assertEquals("Detected double slash in generated link path.",-1, uri.indexOf("//"));
        assertTrue("Incorrect file name generated.",uri.endsWith("/blah.jpg"));
        verify(context, hm, cnt, root, docu);
    }
}
