
/**
 * This file Copyright (c) 2012-2012 Magnolia International
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.PropertyType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;


/**
 * Tests for setting titles of documents.
 *
 * @version $Id:$
 */
public class AdminTreeMVCHandlerTitles extends RepositoryTestCase {

    //DMSAdminTree variables
    private AdminTreeMVCHandler tree;
    private HierarchyManager hm;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private final String newTitle = "newTitle";
    private final String oldFileName = "oldFileName";
    private final String newFileName = "newFileName";
    private final String path = "/level1/" + oldFileName;
    private NodeData value;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        //GIVEN
        request = mock(HttpServletRequest.class);
        when(request.getParameter("saveName")).thenReturn("title");
        when(request.getParameter("path")).thenReturn(path);

        response = mock(HttpServletResponse.class);

        Content content = mock(Content.class);
        value = mock(NodeData.class);
        when(content.getNodeData("title")).thenReturn(value);
        when(value.getType()).thenReturn(PropertyType.STRING);
        when(value.isExist()).thenReturn(true);

        hm = mock(HierarchyManager.class);
        when(hm.getContent(path)).thenReturn(content);

        WebContext ctx = mock(WebContext.class);
        when(ctx.getHierarchyManager("config")).thenReturn(hm);

        MgnlContext.setInstance(ctx);

        tree = new AdminTreeMVCHandler("config", request, response);
        tree.init();
    }

    @Test
    public void testDMSAdminTreeSaveTitle() throws Exception {
        //GIVEN
        when(request.getParameter("saveValue")).thenReturn(newTitle);
        // WHEN
        tree.saveValue();
        // THEN checking if the title was changed
        verify(value).setValue(newTitle);
    }

    @Test
    public void testDMSAdminTreeSaveEmptyTitle() throws Exception {
        //GIVEN
        when(request.getParameter("saveValue")).thenReturn("");
        // WHEN
        tree.saveValue();
        // THEN checking if the title was changed
        verify(value).setValue("untitled");
    }
}
