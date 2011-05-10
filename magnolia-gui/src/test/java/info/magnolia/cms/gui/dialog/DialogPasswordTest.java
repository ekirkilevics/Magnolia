/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import junit.framework.TestCase;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import static org.easymock.EasyMock.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.Locale;

/**
 *
 * @author had
 * TODO: this test should NOT use SystemContext (or not *only*)
 */
public class DialogPasswordTest extends TestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        //MessagesManager.setDefaultLocale("en");
    }

    @Override
    public void tearDown() throws Exception {
        MgnlContext.release();
        // reset manually since we used system context.
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        super.tearDown();
    }

    public void testValidateChangePwd() throws Exception {
        DialogPassword dp = new DialogPassword();
        HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        Content storageNode = createStrictMock(Content.class);
        Content configNode = createStrictMock(Content.class);
        NodeData pswd = createStrictMock(NodeData.class);

        SystemContext ctx = createStrictMock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        MgnlContext.setInstance(ctx);

        expect(configNode.getNodeDataCollection()).andReturn(CollectionUtils.EMPTY_COLLECTION);
        expect(configNode.getHandle()).andReturn("/som/dialog/path/pswd");
        expect(configNode.getName()).andReturn("pswd");
        expect(configNode.getChildren(ItemType.CONTENTNODE)).andReturn(CollectionUtils.EMPTY_COLLECTION);

        // validate() call
        expect(request.getParameter("pswd")).andReturn("blah");
        expect(request.getParameter("pswd_verification")).andReturn("blah");
        expect(storageNode.getNodeData("pswd")).andReturn(pswd);
        expect(pswd.getString()).andReturn("oldBlah");

        Object[] mocks = new Object[] {request, response, storageNode, configNode, pswd, ctx};
        replay(mocks);
        dp.init(request, response, storageNode, configNode);
        assertTrue(dp.validate());
        verify(mocks);
    }

    public void testValidateSamePwd() throws Exception {
        DialogPassword dp = new DialogPassword();
        HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        Content storageNode = createStrictMock(Content.class);
        Content configNode = createStrictMock(Content.class);
        NodeData pswd = createStrictMock(NodeData.class);

        SystemContext ctx = createStrictMock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        MgnlContext.setInstance(ctx);

        expect(configNode.getNodeDataCollection()).andReturn(CollectionUtils.EMPTY_COLLECTION);
        expect(configNode.getHandle()).andReturn("/som/dialog/path/pswd");
        expect(configNode.getName()).andReturn("pswd");
        expect(configNode.getChildren(ItemType.CONTENTNODE)).andReturn(CollectionUtils.EMPTY_COLLECTION);

        // validate() call
        expect(request.getParameter("pswd")).andReturn("blah");
        expect(request.getParameter("pswd_verification")).andReturn("blah");
        expect(storageNode.getNodeData("pswd")).andReturn(pswd);
        expect(pswd.getString()).andReturn(new String(Base64.encodeBase64("blah".getBytes())));

        Object[] mocks = new Object[] {request, response, storageNode, configNode, pswd, ctx};
        replay(mocks);
        dp.init(request, response, storageNode, configNode);
        assertTrue(dp.validate());
        verify(mocks);
    }

    public void testValidatePwdNotChanged() throws Exception {
        DialogPassword dp = new DialogPassword();
        HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        Content storageNode = createStrictMock(Content.class);
        Content configNode = createStrictMock(Content.class);
        NodeData pswd = createStrictMock(NodeData.class);

        SystemContext ctx = createStrictMock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        MgnlContext.setInstance(ctx);

        expect(configNode.getNodeDataCollection()).andReturn(CollectionUtils.EMPTY_COLLECTION);
        expect(configNode.getHandle()).andReturn("/som/dialog/path/pswd");
        expect(configNode.getName()).andReturn("pswd");
        expect(configNode.getChildren(ItemType.CONTENTNODE)).andReturn(CollectionUtils.EMPTY_COLLECTION);

        // validate() call
        expect(request.getParameter("pswd")).andReturn("    ");
        expect(request.getParameter("pswd_verification")).andReturn("");
        expect(storageNode.getNodeData("pswd")).andReturn(pswd);
        expect(pswd.getString()).andReturn(new String(Base64.encodeBase64("blah".getBytes())));

        Object[] mocks = new Object[] {request, response, storageNode, configNode, pswd, ctx};
        replay(mocks);
        dp.init(request, response, storageNode, configNode);
        assertTrue(dp.validate());
        verify(mocks);
    }

    public void testValidateChangePwdVerifyDontMatch() throws Exception {
        DialogPassword dp = new DialogPassword();
        HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        Content storageNode = createStrictMock(Content.class);
        Content configNode = createStrictMock(Content.class);
        Iterator iterator = createStrictMock(Iterator.class);
        NodeData pswd = createStrictMock(NodeData.class);
        // use nice mock due to issues with MessageManager and its static init block
        SystemContext ctx = createNiceMock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager());
        MgnlContext.setInstance(ctx);

        expect(configNode.getNodeDataCollection()).andReturn(CollectionUtils.EMPTY_COLLECTION);
        expect(configNode.getHandle()).andReturn("/som/dialog/path/pswd");
        expect(configNode.getName()).andReturn("pswd");
        expect(configNode.getChildren(ItemType.CONTENTNODE)).andReturn(CollectionUtils.EMPTY_COLLECTION);

        // validate() call
        expect(request.getParameter("pswd")).andReturn("blah");
        expect(request.getParameter("pswd_verification")).andReturn("huh");
        expect(storageNode.getNodeData("pswd")).andReturn(pswd);
        expect(pswd.getString()).andReturn("oldBlah").times(2);

        //ctx.setLocale(Locale.ENGLISH);
        expect(ctx.getLocale()).andReturn(Locale.ENGLISH).anyTimes();

        expect(storageNode.hasNodeData("pswd")).andReturn(Boolean.TRUE);
        expect(storageNode.getNodeData("pswd")).andReturn(pswd);
        // expect(ctx.getAttribute("multipartform")).andReturn(null);
        expect(request.getMethod()).andReturn("POST");
        expect(request.getParameter("pswd")).andReturn("blah");
        expect(request.getMethod()).andReturn("POST");
        expect(request.getParameter("pswd")).andReturn("blah");
        expect(ctx.getAttribute("msg", Context.LOCAL_SCOPE)).andReturn(null);
        ctx.setAttribute("msg", "dialog.password.failed.js", Context.LOCAL_SCOPE);

        Object[] mocks = new Object[] {request, response, storageNode, configNode, iterator, pswd, ctx};
        replay(mocks);
        dp.init(request, response, storageNode, configNode);
        assertFalse(dp.validate());
        verify(mocks);
    }

    public void testValidateChangePwdVerifyDontMatchVerifyEmpty() throws Exception {
        DialogPassword dp = new DialogPassword();
        HttpServletRequest request = createStrictMock(HttpServletRequest.class);
        HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        Content storageNode = createStrictMock(Content.class);
        Content configNode = createStrictMock(Content.class);
        Iterator iterator = createStrictMock(Iterator.class);
        NodeData pswd = createStrictMock(NodeData.class);
        // use nice mock due to issues with MessageManager and its static init block
        SystemContext ctx = createNiceMock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager());
        MgnlContext.setInstance(ctx);

        expect(configNode.getNodeDataCollection()).andReturn(CollectionUtils.EMPTY_COLLECTION);
        expect(configNode.getHandle()).andReturn("/som/dialog/path/pswd");
        expect(configNode.getName()).andReturn("pswd");
        expect(configNode.getChildren(ItemType.CONTENTNODE)).andReturn(CollectionUtils.EMPTY_COLLECTION);

        // validate() call
        expect(request.getParameter("pswd")).andReturn("blah");
        expect(request.getParameter("pswd_verification")).andReturn("");
        expect(storageNode.getNodeData("pswd")).andReturn(pswd);
        expect(pswd.getString()).andReturn("oldBlah").times(2);

        //ctx.setLocale(Locale.ENGLISH);
        expect(ctx.getLocale()).andReturn(Locale.ENGLISH).anyTimes();

        expect(storageNode.hasNodeData("pswd")).andReturn(Boolean.TRUE);
        expect(storageNode.getNodeData("pswd")).andReturn(pswd);
        // expect(ctx.getAttribute("multipartform")).andReturn(null);
        expect(request.getMethod()).andReturn("POST");
        expect(request.getParameter("pswd")).andReturn("blah");
        expect(request.getMethod()).andReturn("POST");
        expect(request.getParameter("pswd")).andReturn("blah");
        expect(ctx.getAttribute("msg", Context.LOCAL_SCOPE)).andReturn(null);
        ctx.setAttribute("msg", "dialog.password.failed.js", Context.LOCAL_SCOPE);

        Object[] mocks = new Object[] {request, response, storageNode, configNode, iterator, pswd, ctx};
        replay(mocks);
        dp.init(request, response, storageNode, configNode);
        assertFalse(dp.validate());
        verify(mocks);
    }

}
