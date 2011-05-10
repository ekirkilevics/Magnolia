/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.context;

import info.magnolia.test.ComponentsTestUtil;
import junit.framework.TestCase;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author ashapochka
 * @version $Revision: $ ($Author: $)
 */
public class RequestAttributeStrategyTest extends TestCase {
    @Override
    protected void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    public void testLocalAttributes() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        WebContext ctx = createMock(WebContext.class);
        RequestAttributeStrategy strategy = new RequestAttributeStrategy(ctx);
        expect(ctx.getRequest()).andReturn(request).anyTimes();
        request.setAttribute("attr1", "value1");
        expect(request.getAttribute("attr1")).andReturn("value1");
        request.removeAttribute("attr1");
        request.removeAttribute("attr2");
        expect(request.getAttribute("param1")).andReturn(null);
        expect(request.getParameter("param1")).andReturn("pvalue1");
        expect(request.getAttribute(WebContext.ATTRIBUTE_REQUEST_CHARACTER_ENCODING)).andReturn(null);
        expect(request.getParameter(WebContext.ATTRIBUTE_REQUEST_CHARACTER_ENCODING)).andReturn(null);
        expect(request.getCharacterEncoding()).andReturn("UTF-8");
        expect(request.getAttribute(WebContext.ATTRIBUTE_REQUEST_URI)).andReturn(null);
        expect(request.getParameter(WebContext.ATTRIBUTE_REQUEST_URI)).andReturn(null);
        expect(request.getRequestURI()).andReturn("/foo/bar");
        replay(request, ctx);
        strategy.setAttribute("attr1", "value1", Context.LOCAL_SCOPE);
        assertEquals("value1", strategy.getAttribute("attr1", Context.LOCAL_SCOPE));
        strategy.setAttribute("attr1", null, Context.LOCAL_SCOPE);
        strategy.removeAttribute("attr2", Context.LOCAL_SCOPE);
        assertEquals("pvalue1", strategy.getAttribute("param1", Context.LOCAL_SCOPE));
        assertEquals("UTF-8", strategy.getAttribute(WebContext.ATTRIBUTE_REQUEST_CHARACTER_ENCODING, Context.LOCAL_SCOPE));
        assertEquals("/foo/bar", strategy.getAttribute(WebContext.ATTRIBUTE_REQUEST_URI, Context.LOCAL_SCOPE));
        verify(request, ctx);
    }

    public void testSessionAttributes() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpSession session = createMock(HttpSession.class);
        WebContext ctx = createMock(WebContext.class);
        RequestAttributeStrategy strategy = new RequestAttributeStrategy(ctx);
        expect(ctx.getRequest()).andReturn(request).anyTimes();
        expect(request.getSession(false)).andReturn(session).times(3);
        session.setAttribute("attr1", "value1");
        expect(session.getAttribute("attr1")).andReturn("value1");
        session.removeAttribute("attr1");
        replay(request, session, ctx);
        strategy.setAttribute("attr1", "value1", Context.SESSION_SCOPE);
        assertEquals("value1", strategy.getAttribute("attr1", Context.SESSION_SCOPE));
        strategy.removeAttribute("attr1", Context.SESSION_SCOPE);
        verify(request, session, ctx);
    }

    public void testApplicationAttributes() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        SystemContext context = createMock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, context);
        WebContext ctx = createMock(WebContext.class);
        RequestAttributeStrategy strategy = new RequestAttributeStrategy(ctx);
        context.setAttribute("attr1", "value1", Context.APPLICATION_SCOPE);
        expect(context.getAttribute("attr1", Context.APPLICATION_SCOPE)).andReturn("value1");
        context.removeAttribute("attr1", Context.APPLICATION_SCOPE);
        replay(context);
        strategy.setAttribute("attr1", "value1", Context.APPLICATION_SCOPE);
        assertEquals("value1", strategy.getAttribute("attr1", Context.APPLICATION_SCOPE));
        strategy.removeAttribute("attr1", Context.APPLICATION_SCOPE);
        verify(context);
    }
}
