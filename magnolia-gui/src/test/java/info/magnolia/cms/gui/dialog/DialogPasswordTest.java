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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.test.ComponentsTestUtil;

import java.util.Iterator;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 * TODO: this test should NOT use SystemContext (or not *only*)
 */
public class DialogPasswordTest {

    @Before
    public void setUp() throws Exception {
        //MessagesManager.setDefaultLocale("en");
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.release();
        // reset manually since we used system context.
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void testValidateChangePwd() throws Exception {
        // GIVEN
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Content storageNode = mock(Content.class);
        Content configNode = mock(Content.class);
        NodeData pswd = mock(NodeData.class);

        SystemContext ctx = mock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        MgnlContext.setInstance(ctx);

        when(configNode.getHandle()).thenReturn("/som/dialog/path/pswd");
        when(configNode.getName()).thenReturn("pswd");

        // validate() call
        when(request.getParameter("pswd")).thenReturn("blah");
        when(request.getParameter("pswd_verification")).thenReturn("blah");
        when(storageNode.getNodeData("pswd")).thenReturn(pswd);
        when(pswd.getString()).thenReturn("oldBlah");

        DialogPassword dp = new DialogPassword();

        // WHEN
        dp.init(request, response, storageNode, configNode);

        // THEN
        assertTrue(dp.validate());
    }

    @Test
    public void testValidateSamePwd() throws Exception {
        // GIVEN
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Content storageNode = mock(Content.class);
        Content configNode = mock(Content.class);
        NodeData pswd = mock(NodeData.class);

        SystemContext ctx = mock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        MgnlContext.setInstance(ctx);

        when(configNode.getHandle()).thenReturn("/som/dialog/path/pswd");
        when(configNode.getName()).thenReturn("pswd");

        // validate() call
        when(request.getParameter("pswd")).thenReturn("blah");
        when(request.getParameter("pswd_verification")).thenReturn("blah");
        when(storageNode.getNodeData("pswd")).thenReturn(pswd);
        when(pswd.getString()).thenReturn(new String(Base64.encodeBase64("blah".getBytes())));

        DialogPassword dp = new DialogPassword();

        // WHEN
        dp.init(request, response, storageNode, configNode);

        // THEN
        assertTrue(dp.validate());
    }

    @Test
    public void testValidatePwdNotChanged() throws Exception {
        // GIVEN
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Content storageNode = mock(Content.class);
        Content configNode = mock(Content.class);
        NodeData pswd = mock(NodeData.class);

        SystemContext ctx = mock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        MgnlContext.setInstance(ctx);

        when(configNode.getHandle()).thenReturn("/som/dialog/path/pswd");
        when(configNode.getName()).thenReturn("pswd");

        // validate() call
        when(request.getParameter("pswd")).thenReturn("    ");
        when(request.getParameter("pswd_verification")).thenReturn("");
        when(storageNode.getNodeData("pswd")).thenReturn(pswd);
        when(pswd.getString()).thenReturn(new String(Base64.encodeBase64("blah".getBytes())));

        DialogPassword dp = new DialogPassword();

        // WHEN
        dp.init(request, response, storageNode, configNode);

        // THEN
        assertTrue(dp.validate());
    }

    @Test
    public void testValidateChangePwdVerifyDontMatch() throws Exception {
        // GIVEN
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Content storageNode = mock(Content.class);
        Content configNode = mock(Content.class);
        Iterator iterator = mock(Iterator.class);
        NodeData pswd = mock(NodeData.class);

        SystemContext ctx = mock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager(new Node2BeanProcessorImpl(new TypeMappingImpl(), new Node2BeanTransformerImpl())));
        MgnlContext.setInstance(ctx);

        when(configNode.getHandle()).thenReturn("/som/dialog/path/pswd");
        when(configNode.getName()).thenReturn("pswd");

        // validate() call
        when(request.getParameter("pswd")).thenReturn("blah");
        when(request.getParameter("pswd_verification")).thenReturn("huh");
        when(storageNode.getNodeData("pswd")).thenReturn(pswd);
        when(pswd.getString()).thenReturn("oldBlah");

        //ctx.setLocale(Locale.ENGLISH);
        when(ctx.getLocale()).thenReturn(Locale.ENGLISH);

        when(storageNode.hasNodeData("pswd")).thenReturn(Boolean.TRUE);
        when(storageNode.getNodeData("pswd")).thenReturn(pswd);
        // when(ctx.getAttribute("multipartform")).thenReturn(null);
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("pswd")).thenReturn("blah");
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("pswd")).thenReturn("blah");
        when(ctx.getAttribute("msg", Context.LOCAL_SCOPE)).thenReturn(null);
        ctx.setAttribute("msg", "dialog.password.failed.js", Context.LOCAL_SCOPE);

        DialogPassword dp = new DialogPassword();

        // WHEN
        dp.init(request, response, storageNode, configNode);

        // THEN
        assertFalse(dp.validate());
    }

    @Test
    public void testValidateChangePwdVerifyDontMatchVerifyEmpty() throws Exception {
        // GIVEN
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Content storageNode = mock(Content.class);
        Content configNode = mock(Content.class);
        Iterator iterator = mock(Iterator.class);
        NodeData pswd = mock(NodeData.class);
        // use nice mock due to issues with MessageManager and its static init block
        SystemContext ctx = mock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager(new Node2BeanProcessorImpl(new TypeMappingImpl(), new Node2BeanTransformerImpl())));
        MgnlContext.setInstance(ctx);

        when(configNode.getHandle()).thenReturn("/som/dialog/path/pswd");
        when(configNode.getName()).thenReturn("pswd");

        // validate() call
        when(request.getParameter("pswd")).thenReturn("blah");
        when(request.getParameter("pswd_verification")).thenReturn("");
        when(storageNode.getNodeData("pswd")).thenReturn(pswd);
        when(pswd.getString()).thenReturn("oldBlah");

        //ctx.setLocale(Locale.ENGLISH);
        when(ctx.getLocale()).thenReturn(Locale.ENGLISH);

        when(storageNode.hasNodeData("pswd")).thenReturn(Boolean.TRUE);
        when(storageNode.getNodeData("pswd")).thenReturn(pswd);
        // when(ctx.getAttribute("multipartform")).thenReturn(null);
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("pswd")).thenReturn("blah");
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("pswd")).thenReturn("blah");
        when(ctx.getAttribute("msg", Context.LOCAL_SCOPE)).thenReturn(null);
        ctx.setAttribute("msg", "dialog.password.failed.js", Context.LOCAL_SCOPE);

        DialogPassword dp = new DialogPassword();

        // WHEN
        dp.init(request, response, storageNode, configNode);

        // THEN
        assertFalse(dp.validate());
    }
}
