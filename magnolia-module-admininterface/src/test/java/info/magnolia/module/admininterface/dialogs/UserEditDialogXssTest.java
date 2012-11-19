/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.module.admininterface.dialogs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.cms.gui.dialog.DialogControlImpl;
import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.commands.CommandsManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.util.EscapeUtil;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.commons.chain.Command;
import org.junit.Test;

/**
 * Tests for XSS vulnerability in UserEditDialog.
 */
public class UserEditDialogXssTest extends TestCase {

    private final List<String> groupPath = new LinkedList<String>();
    private final List<String> rolesPath = new LinkedList<String>();
    private Dialog dialog;
    private WebContext context;
    private CommandsManager cm;
    private Command command;
    private MultipartForm form;
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    final String xssCode = "{{\"/><img src=x onerror=alert(/xss/)> }}";
    final String escapedXssCode = EscapeUtil.escapeXss("{{\"/><img src=x onerror=alert(/xss/)> }}");

    @Override
    protected void setUp() throws Exception {

        context = mock(WebContext.class);
        cm = mock(CommandsManager.class);
        command = mock(Command.class);
        HierarchyManager hm = mock(HierarchyManager.class);
        Content parentContent = mock(Content.class);
        Collection collection = mock(Collection.class);
        Iterator iterator = mock(Iterator.class);
        dialog = new Dialog();
        DialogControlImpl dci = mock(DialogControlImpl.class);
        DialogControlImpl dci2 = mock(DialogControlImpl.class);
        dialog.addSub(dci);
        dialog.addSub(dci2);
        NodeData nodeData = mock(NodeData.class);

        form = new MultipartForm();

        ComponentsTestUtil.setInstance(CommandsManager.class, cm);
        ComponentsTestUtil.setInstance(SystemContext.class, mock(SystemContext.class));
        ComponentsTestUtil.setInstance(MessagesManager.class, mock(MessagesManager.class));
        ComponentsTestUtil.setInstance(I18nAuthoringSupport.class, mock(I18nAuthoringSupport.class));

        when(dci.getValues()).thenReturn(groupPath);
        when(dci2.getValues()).thenReturn(rolesPath);
        when(dci.getName()).thenReturn("groups");
        when(dci2.getName()).thenReturn("roles");
        when(collection.iterator()).thenReturn(iterator);
        when(parentContent.createNodeData("0")).thenReturn(nodeData);
        when(parentContent.getContent("groups")).thenReturn(parentContent);
        when(parentContent.getContent("roles")).thenReturn(parentContent);
        when(parentContent.getNodeDataCollection()).thenReturn(collection);
        when(hm.getContent("")).thenReturn(parentContent);
        when(hm.getContent(escapedXssCode)).thenReturn(parentContent);
        when(hm.getContent(xssCode)).thenReturn(parentContent);
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("saveName")).thenReturn("title");
        when(cm.getCommand(null, "")).thenReturn(command);
        when(context.getHierarchyManager(RepositoryConstants.USER_GROUPS)).thenReturn(hm);
        when(context.getHierarchyManager(RepositoryConstants.USER_ROLES)).thenReturn(hm);
        when(context.getHierarchyManager("website")).thenReturn(hm);

        MgnlContext.setInstance(context);
    }

    @Override
    public void tearDown(){
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test //the parameters can be set via UserEditDialog...
    public void testUserEditDialogXss() {
        //GIVEN
        when(context.getPostedForm()).thenReturn(form);

        form.addparameterValues("groups", new String[]{xssCode});
        form.addparameterValues("roles", new String[]{xssCode});

        UserEditDialog ued = new UserEditDialog(null, request, null, null);

        //WHEN
        ued.onPreSave(null);
        ued.onPreSave(null); //do it again and check if parameters aren't escaped twice

        //THEN
        assertEquals(escapedXssCode,form.getParameterValues("groups")[0]);
        assertEquals(escapedXssCode,form.getParameterValues("roles")[0]);
    }
}
