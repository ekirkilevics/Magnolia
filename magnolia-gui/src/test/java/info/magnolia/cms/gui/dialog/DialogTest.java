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
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.test.ComponentsTestUtil;

import java.io.StringWriter;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Test;

/**
 * @version $Id$
 */
public class DialogTest {

    private static final String SAVE_BUTTON_SEQUENCE = "id=\"mgnlSaveButton\" class=\"mgnlControlButton\"";

    @Test
    public void testDrawHtmlPostSubsButtons() throws Exception {
        // GIVEN
        SystemContext ctx = mock(SystemContext.class);
        MessagesManager messagesManager = mock(MessagesManager.class);
        Messages messages = mock(Messages.class);
        StringWriter writer = new StringWriter();

        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        ComponentsTestUtil.setInstance(MessagesManager.class, messagesManager);
        MgnlContext.setInstance(ctx);

        when(messagesManager.getMessages(null, null) ).thenReturn(messages);

        when(messages.get("buttons.save")).thenReturn("SAVE");

        Dialog dialog = new Dialog();

        // WHEN
        dialog.drawHtmlPostSubsButtons(writer);

        // THEN
        assertTrue(writer.toString().contains(SAVE_BUTTON_SEQUENCE));
    }

    @Test
    public void testDrawHtmlPostSubsButtonsWithEmptyButtonSaveMessage() throws Exception {
        // GIVEN
        SystemContext ctx = mock(SystemContext.class);
        MessagesManager messagesManager = mock(MessagesManager.class);
        Messages messages = mock(Messages.class);
        StringWriter writer = new StringWriter();

        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        ComponentsTestUtil.setInstance(MessagesManager.class, messagesManager);
        MgnlContext.setInstance(ctx);

        when(messagesManager.getMessages(null, null) ).thenReturn(messages);

        Dialog dialog = new Dialog();

        // WHEN
        dialog.drawHtmlPostSubsButtons(writer);

        // THEN - save button should not be "written"
        assertFalse(writer.toString().contains(SAVE_BUTTON_SEQUENCE));
    }

    @Test
    public void testdrawHtmlPreSubsHeadOutputsJQuery() throws Exception {
        // GIVEN
        SystemContext ctx = mock(SystemContext.class);
        User user = mock(User.class);
        MessagesManager messagesManager = mock(MessagesManager.class);
        Messages messages = mock(Messages.class);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);

        StringWriter writer = new StringWriter();

        ComponentsTestUtil.setInstance(SystemContext.class, ctx);
        ComponentsTestUtil.setInstance(MessagesManager.class, messagesManager);
        MgnlContext.setInstance(ctx);

        when(req.getContextPath()).thenReturn("/blah");
        when(ctx.getLocale()).thenReturn(new Locale("en"));
        when(ctx.getUser()).thenReturn(user);
        when(messagesManager.getMessages(null, null)).thenReturn(messages);
        when(messagesManager.getMessages()).thenReturn(messages);
        when(messages.get(null)).thenReturn("foo");

        Dialog dialog = new Dialog();
        dialog.init(req, res, null, null);

        // WHEN
        dialog.drawHtmlPreSubsHead(writer);

        // THEN
        assertTrue(writer.toString().contains("jQuery.noConflict()"));
        StringBuilder expectedValue = new StringBuilder();
        expectedValue.append("jQuery(document).ready(function($) {\n");
        expectedValue.append("  window.onresize = eventHandlerOnResize;\n");
        expectedValue.append("  window.resizeTo("+Dialog.DIALOGSIZE_NORMAL_WIDTH+","+ Dialog.DIALOGSIZE_NORMAL_HEIGHT+");\n");
        expectedValue.append("  mgnlDialogResizeTabs();\n");
        expectedValue.append("  mgnlDialogShiftTab('" + dialog.getId() + "',false,0)\n");
        expectedValue.append("});");
        assertTrue(writer.toString().contains(expectedValue.toString()));
    }

    @After
    public void tearDown() {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }
}
