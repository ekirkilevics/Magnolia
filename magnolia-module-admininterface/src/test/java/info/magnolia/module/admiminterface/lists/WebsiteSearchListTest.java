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
package info.magnolia.module.admiminterface.lists;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.search.SearchableListModel;
import info.magnolia.cms.gui.query.SearchQuery;
import info.magnolia.cms.gui.query.StringSearchQueryParameter;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.admininterface.lists.WebsiteSearchList;
import info.magnolia.test.ComponentsTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @version $Id$
 */
public class WebsiteSearchListTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private WebContext ctx;

    @Before
    public void setUp(){
        Locale locale = new Locale("en");
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager());
        ComponentsTestUtil.setImplementation(MgnlContext.class, MgnlContext.class);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        ctx = mock(WebContext.class);
        when(ctx.getLocale()).thenReturn(locale);
        MgnlContext.setInstance(ctx);
    }
    
    @Test
    public void testSearchWithEmptySearchString(){
        WebsiteSearchList list = new WebsiteSearchList("test", request, response);
        ListControl clist = new ListControl();

        list.initList(clist);
        SearchQuery query = ((SearchableListModel)clist.getModel()).getQuery();
        StringSearchQueryParameter parameter = (StringSearchQueryParameter)query.getRootExpression();
        assertEquals(parameter.getValue(), "*$");
    }

    @After
    public void tearDown(){
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }
}
