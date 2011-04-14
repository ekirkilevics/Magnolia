/**
 * This file Copyright (c) 2009-2011 Magnolia International
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
package info.magnolia.module.admininterface;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.admininterface.commands.BaseActivationCommand;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockMetaData;
import junit.framework.TestCase;
import org.apache.commons.collections.ListUtils;

import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;


/**
 * @author had
 * @version $Id:$
 */
public class AdminTreeMVCHandlerUTF8Test extends TestCase
{

    private HttpServletRequest req;

    private HttpServletResponse res;

    private WebContext ctx;

    private SystemContext sysctx;

    private HierarchyManager hm;

    private Content cnt;

    private AdminTreeMVCHandler handler;

    private Object[] objs;

    private Workspace workspace;

    private Session session;

    public static final String TEXT_GREEK = "\u03BA\u1F79\u03C3\u03BC\u03B5";

    public static final String TEXT_RUSSIAN = "\u041D\u0430 \u0431\u0435\u0440\u0435\u0433\u0443 \u043F\u0443\u0441\u0442\u044B\u043D\u043D\u044B\u0445 \u0432\u043E\u043B\u043D";

    public static final String TEXT_RUSSIAN_VALIDATED = "\u041D\u0430-\u0431\u0435\u0440\u0435\u0433\u0443-\u043F\u0443\u0441\u0442\u044B\u043D\u043D\u044B\u0445-\u0432\u043E\u043B\u043D";

    public static final String TEXT_SPECIAL = "utf8!?#{}$!\u00A3%()=@";

    public static final String TEXT_SPECIAL_VALIDATED = "utf8---{}$-\u00A3-()--";

    public static final String TEXT_ACCENTED = "citt\u00E0\u00E8\u00EC\u00F2\u00F9";

    public static final String TEXT_NOT_ALLOWED = "[]; +";

    public static final String TEXT_NOT_ALLOWED_VALIDATED = "-----";

    public void setUp()
    {
        req = createStrictMock(HttpServletRequest.class);
        res = createStrictMock(HttpServletResponse.class);
        ctx = createStrictMock(WebContext.class);
        sysctx = createStrictMock(SystemContext.class);
        hm = createStrictMock(HierarchyManager.class);
        cnt = createNiceMock(Content.class);
        workspace = createNiceMock(Workspace.class);
        session = createNiceMock(Session.class);

        handler = new AdminTreeMVCHandler(TEXT_GREEK, req, res);
        handler.setRepository("repo-name");
        MgnlContext.setInstance(ctx);
        ComponentsTestUtil.setInstance(SystemContext.class, sysctx);
    }

    public void tearDown()
    {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        SystemProperty.clear();
    }
    
    public void testMove() throws Exception
    {
        expect(ctx.getHierarchyManager("repo-name")).andReturn(hm);
        expect(hm.isExist("/" + TEXT_RUSSIAN + "/foo")).andReturn(false);
        hm.moveTo("/" + TEXT_GREEK + "/foo", "/" + TEXT_RUSSIAN + "/foo");
        expect(hm.getContent("/" + TEXT_RUSSIAN + "/foo")).andReturn(cnt);
        cnt.updateMetaData();
        expect(cnt.getChildren()).andReturn(ListUtils.EMPTY_LIST);
        cnt.save();

        objs = new Object[]{req, res, ctx, hm, cnt };
        replay(objs);
        handler.moveNode("/" + TEXT_GREEK + "/foo", "/" + TEXT_RUSSIAN + "/foo");
        verify(objs);
    }

    /**
     * Make sure that the uuid is retrieved for activation and path is translated using SC
     * @throws Exception
     */
    public void testGetActivateCommandContext() throws Exception
    {
        handler.pathSelected = "/some/" + TEXT_ACCENTED + "/" + TEXT_SPECIAL;
        expect(ctx.put("repository", "repo-name")).andReturn(null);
        expect(ctx.put(BaseActivationCommand.ATTRIBUTE_SYNDICATOR, null)).andReturn(null);
        expect(ctx.getHierarchyManager("repo-name")).andReturn(hm);
        expect(hm.getContent("/some/" + TEXT_ACCENTED + "/" + TEXT_SPECIAL)).andReturn(cnt);
        expect(cnt.getUUID()).andReturn("uu-blah-id");
        expect(ctx.put("uuid", "uu-blah-id")).andReturn(null);
        expect(sysctx.getHierarchyManager("repo-name")).andReturn(hm);
        expect(hm.getContentByUUID("uu-blah-id")).andReturn(cnt);
        expect(cnt.getHandle()).andReturn("/some/" + TEXT_ACCENTED + "/" + TEXT_SPECIAL);
        expect(ctx.put("path", "/some/" + TEXT_ACCENTED + "/" + TEXT_SPECIAL)).andReturn(null);

        objs = new Object[]{req, res, ctx, hm, cnt, sysctx };
        replay(objs);
        handler.getCommandContext("activate");
        verify(objs);
    }

    /**
     * test on save value
     * @throws Exception
     */
    public void testSaveValueCommandContext() throws Exception
    {
        String parentHandle = "/foo";
        String nodeName = "untitled";
        handler.pathSelected = "column0";
        handler.path = parentHandle + "/" + nodeName;
        String saveValue = "bar";
        expect(req.getParameter("saveName")).andReturn(handler.pathSelected);
        expect(req.getParameter("isNodeDataValue")).andReturn("false");
        expect(req.getParameter("isNodeDataType")).andReturn("false");
        expect(req.getParameter("saveValue")).andReturn(saveValue);
        expect(req.getParameter("isMeta")).andReturn("false");
        expect(req.getParameter("isLabel")).andReturn("true");
        expect(handler.getHierarchyManager()).andReturn(hm);
        expect(hm.isExist(parentHandle + "/" + saveValue)).andReturn(false);
        expect(handler.getHierarchyManager()).andReturn(hm);
        expect(hm.isNodeData(handler.path)).andReturn(false);
        expect(handler.getHierarchyManager()).andReturn(hm);
        expect(hm.getContent(handler.path)).andReturn(cnt);
        MockContent foo = new MockContent(parentHandle);
        expect(cnt.getParent()).andReturn(foo);
        expect(cnt.getParent()).andReturn(foo);
        expect(cnt.getWorkspace()).andReturn(workspace);
        expect(workspace.getSession()).andReturn(session);
        expect(cnt.getHandle()).andReturn(parentHandle);
        expect(cnt.getParent()).andReturn(foo);
        expect(handler.getHierarchyManager()).andReturn(hm);
        expect(hm.getContent(parentHandle + "/" + saveValue)).andReturn(cnt);
        expect(req.getParameter("displayValue")).andReturn(null);
        expect(handler.getHierarchyManager()).andReturn(hm);

        objs = new Object[]{req, res, ctx, hm, cnt, workspace, session };
        replay(objs);
        String view = handler.saveValue();
        verify(objs);

        assertEquals("value", view);
    }

    /**
     * test on save value
     * @throws Exception
     */
    public void testSaveUTF8ValueCommandContext() throws Exception
    {
        String parentHandle = "/foo";
        String nodeName = "untitled";
        String saveValue = TEXT_SPECIAL + TEXT_ACCENTED + TEXT_GREEK + TEXT_RUSSIAN + TEXT_NOT_ALLOWED;
        String expectedValue = TEXT_SPECIAL_VALIDATED
            + TEXT_ACCENTED
            + TEXT_GREEK
            + TEXT_RUSSIAN_VALIDATED
            + TEXT_NOT_ALLOWED_VALIDATED;
        handler.pathSelected = "column0";
        handler.path = parentHandle + "/" + nodeName;
        expect(req.getParameter("saveName")).andReturn(handler.pathSelected);
        expect(req.getParameter("isNodeDataValue")).andReturn("false");
        expect(req.getParameter("isNodeDataType")).andReturn("false");
        expect(req.getParameter("saveValue")).andReturn(saveValue);
        expect(req.getParameter("isMeta")).andReturn("false");
        expect(req.getParameter("isLabel")).andReturn("true");
        expect(handler.getHierarchyManager()).andReturn(hm);
        expect(hm.isExist(parentHandle + "/" + expectedValue)).andReturn(false);
        expect(handler.getHierarchyManager()).andReturn(hm);
        expect(hm.isNodeData(handler.path)).andReturn(false);
        expect(handler.getHierarchyManager()).andReturn(hm);
        expect(hm.getContent(handler.path)).andReturn(cnt);
        MockContent foo = new MockContent(parentHandle);
        expect(cnt.getParent()).andReturn(foo);
        expect(cnt.getParent()).andReturn(foo);
        expect(cnt.getWorkspace()).andReturn(workspace);
        expect(workspace.getSession()).andReturn(session);
        expect(cnt.getHandle()).andReturn(parentHandle);
        expect(cnt.getParent()).andReturn(foo);
        expect(handler.getHierarchyManager()).andReturn(hm);
        expect(hm.getContent(parentHandle + "/" + expectedValue)).andReturn(cnt);
        expect(req.getParameter("displayValue")).andReturn(null);
        expect(handler.getHierarchyManager()).andReturn(hm);

        objs = new Object[]{req, res, ctx, hm, cnt, workspace, session };
        replay(objs);
        String view = handler.saveValue();
        verify(objs);
        assertEquals("value", view);
    }

    /**
     * Make sure path is set for <b>every</b> command.
     * @throws Exception
     */
    public void testGetSomeCommandContext() throws Exception
    {
        handler.pathSelected = "/some/" + TEXT_ACCENTED + "/" + TEXT_SPECIAL;
        expect(ctx.put("repository", "repo-name")).andReturn(null);
        expect(ctx.put("path", "/some/" + TEXT_ACCENTED + "/" + TEXT_SPECIAL)).andReturn(null);
        objs = new Object[]{req, res, ctx, hm, cnt };
        replay(objs);
        handler.getCommandContext("something");
        verify(objs);
    }

    public void testCopy() throws Exception
    {
        final boolean unactivated[] = new boolean[1];
        MetaData meta = new MockMetaData(new MockContent(TEXT_RUSSIAN))
        {

            public void setUnActivated() throws AccessDeniedException
            {
                unactivated[0] = true;
            }
        };
        // sanity check
        assertFalse(unactivated[0]);

        expect(ctx.getHierarchyManager("repo-name")).andReturn(hm);
        expect(hm.isExist("/bar/" + TEXT_ACCENTED)).andReturn(false);
        hm.copyTo("/test/" + TEXT_SPECIAL, "/bar/" + TEXT_ACCENTED);
        expect(hm.getContent("/bar/" + TEXT_ACCENTED)).andReturn(cnt);
        cnt.updateMetaData();
        expect(cnt.getMetaData()).andReturn(meta);
        expect(cnt.getChildren()).andReturn(ListUtils.EMPTY_LIST);
        cnt.save();

        objs = new Object[]{req, res, ctx, hm, cnt };
        replay(objs);
        handler.copyNode("/test/" + TEXT_SPECIAL, "/bar/" + TEXT_ACCENTED);
        verify(objs);
        assertTrue(unactivated[0]);
    }

    public void testDeepCopy() throws Exception
    {
        final boolean unactivated[] = new boolean[1];
        MetaData meta = new MockMetaData(new MockContent(TEXT_RUSSIAN))
        {

            public void setUnActivated() throws AccessDeniedException
            {
                unactivated[0] = true;
            }
        };
        Content child = createStrictMock(Content.class);
        MetaData childMeta = new MockMetaData(new MockContent(TEXT_RUSSIAN))
        {

            public void setUnActivated() throws AccessDeniedException
            {
                unactivated[0] = true;
            }
        };

        List<Content> children = new ArrayList<Content>();
        children.add(child);
        // sanity check
        assertFalse(unactivated[0]);

        expect(ctx.getHierarchyManager("repo-name")).andReturn(hm);
        expect(hm.isExist("/bar/" + TEXT_ACCENTED)).andReturn(false);
        hm.copyTo("/test/" + TEXT_SPECIAL, "/bar/" + TEXT_ACCENTED);
        expect(hm.getContent("/bar/" + TEXT_ACCENTED)).andReturn(cnt);
        cnt.updateMetaData();
        expect(cnt.getMetaData()).andReturn(meta);
        expect(cnt.getChildren()).andReturn(children);
        child.updateMetaData();
        expect(child.getMetaData()).andReturn(childMeta);
        expect(child.getChildren()).andReturn(ListUtils.EMPTY_LIST);
        cnt.save();

        objs = new Object[]{req, res, ctx, hm, cnt, child };
        replay(objs);
        handler.copyNode("/test/" + TEXT_SPECIAL, "/bar/" + TEXT_ACCENTED);
        verify(objs);
        assertTrue(unactivated[0]);
    }

}
