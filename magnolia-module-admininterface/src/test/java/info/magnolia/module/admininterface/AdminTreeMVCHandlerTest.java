/**
 * This file Copyright (c) 2009-2010 Magnolia International
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

import java.util.ArrayList;
import java.util.List;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.admininterface.commands.BaseActivationCommand;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockMetaData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ListUtils;

import static org.easymock.EasyMock.*;

import junit.framework.TestCase;

/**
 * @author had
 * @version $Id:$
 */
public class AdminTreeMVCHandlerTest extends TestCase {

    private HttpServletRequest req;
    private HttpServletResponse res;
    private WebContext ctx;
    private SystemContext sysctx;
    private HierarchyManager hm;
    private Content cnt;
    private AdminTreeMVCHandler handler;
    private Object[] objs;

    public void setUp() {
        req = createStrictMock(HttpServletRequest.class);
        res = createStrictMock(HttpServletResponse.class);
        ctx = createStrictMock(WebContext.class);
        sysctx = createStrictMock(SystemContext.class);
        hm = createStrictMock(HierarchyManager.class);
        cnt = createStrictMock(Content.class);
        handler = new AdminTreeMVCHandler("test", req, res);
        handler.setRepository("repo-name");
        MgnlContext.setInstance(ctx);
        ComponentsTestUtil.setInstance(SystemContext.class, sysctx);
    }

    public void testMove() throws Exception {
        expect(ctx.getHierarchyManager("repo-name")).andReturn(hm);
        expect(hm.isExist("/bar/foo")).andReturn(false);
        hm.moveTo("/test/foo", "/bar/foo");
        expect(hm.getContent("/bar/foo")).andReturn(cnt);
        cnt.updateMetaData();
        expect(cnt.getChildren()).andReturn(ListUtils.EMPTY_LIST);
        cnt.save();

        objs = new Object[] {req, res, ctx, hm, cnt};
        replay(objs);
        handler.moveNode("/test/foo", "/bar/foo");
        verify(objs);
    }

    /**
     * Make sure that the uuid is retrieved for activation and path is translated using SC
     * @throws Exception
     */
    public void testGetActivateCommandContext() throws Exception {
        handler.pathSelected = "/some/selected/path";
        expect(ctx.put("repository", "repo-name")).andReturn(null);
        expect(ctx.put(BaseActivationCommand.ATTRIBUTE_SYNDICATOR, null)).andReturn(null);
        expect(ctx.getHierarchyManager("repo-name")).andReturn(hm);
        expect(hm.getContent("/some/selected/path")).andReturn(cnt);
        expect(cnt.getUUID()).andReturn("uu-blah-id");
        expect(ctx.put("uuid", "uu-blah-id")).andReturn(null);
        expect(sysctx.getHierarchyManager("repo-name")).andReturn(hm);
        expect(hm.getContentByUUID("uu-blah-id")).andReturn(cnt);
        expect(cnt.getHandle()).andReturn("/some/selected/path");
        expect(ctx.put("path", "/some/selected/path")).andReturn(null);

        objs = new Object[] {req, res, ctx, hm, cnt, sysctx};
        replay(objs);
        handler.getCommandContext("activate");
        verify(objs);
    }

    /**
     * Make sure path is set for <b>every</b> command.
     * @throws Exception
     */
    public void testGetSomeCommandContext() throws Exception {
        handler.pathSelected = "/some/selected/path";
        expect(ctx.put("repository", "repo-name")).andReturn(null);
        expect(ctx.put("path", "/some/selected/path")).andReturn(null);
        objs = new Object[] {req, res, ctx, hm, cnt};
        replay(objs);
        handler.getCommandContext("something");
        verify(objs);
    }

    public void testCopy() throws Exception {
        final boolean unactivated[] = new boolean[1];
        MetaData meta = new MockMetaData(new MockContent("blah")) {
            public void setUnActivated() throws AccessDeniedException {
                unactivated[0] = true;
            }
        };
        // sanity check
        assertFalse(unactivated[0]);

        expect(ctx.getHierarchyManager("repo-name")).andReturn(hm);
        expect(hm.isExist("/bar/foo")).andReturn(false);
        hm.copyTo("/test/foo", "/bar/foo");
        expect(hm.getContent("/bar/foo")).andReturn(cnt);
        cnt.updateMetaData();
        expect(cnt.getMetaData()).andReturn(meta);
        expect(cnt.getChildren()).andReturn(ListUtils.EMPTY_LIST);
        cnt.save();

        objs = new Object[] {req, res, ctx, hm, cnt};
        replay(objs);
        handler.copyNode("/test/foo", "/bar/foo");
        verify(objs);
        assertTrue(unactivated[0]);
    }

    public void testDeepCopy() throws Exception {
        final boolean unactivated[] = new boolean[1];
        MetaData meta = new MockMetaData(new MockContent("blah")) {
            public void setUnActivated() throws AccessDeniedException {
                unactivated[0] = true;
            }
        };
        Content child = createStrictMock(Content.class);
        MetaData childMeta = new MockMetaData(new MockContent("blah")) {
            public void setUnActivated() throws AccessDeniedException {
                unactivated[0] = true;
            }
        };

        List<Content> children = new ArrayList<Content>();
        children.add(child);
        // sanity check
        assertFalse(unactivated[0]);

        expect(ctx.getHierarchyManager("repo-name")).andReturn(hm);
        expect(hm.isExist("/bar/foo")).andReturn(false);
        hm.copyTo("/test/foo", "/bar/foo");
        expect(hm.getContent("/bar/foo")).andReturn(cnt);
        cnt.updateMetaData();
        expect(cnt.getMetaData()).andReturn(meta);
        expect(cnt.getChildren()).andReturn(children);
        child.updateMetaData();
        expect(child.getMetaData()).andReturn(childMeta);
        expect(child.getChildren()).andReturn(ListUtils.EMPTY_LIST);
        cnt.save();

        objs = new Object[] {req, res, ctx, hm, cnt, child};
        replay(objs);
        handler.copyNode("/test/foo", "/bar/foo");
        verify(objs);
        assertTrue(unactivated[0]);
    }

    public void tearDown() {
        MgnlContext.setInstance(null);
    }
}
