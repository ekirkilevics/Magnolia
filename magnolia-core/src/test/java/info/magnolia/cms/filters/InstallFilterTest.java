/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.cms.filters;

import info.magnolia.module.ModuleManager;
import info.magnolia.module.ui.ModuleManagerUI;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import static org.easymock.EasyMock.*;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class InstallFilterTest extends MgnlTestCase {
    private ModuleManager moduleManager;
    private ModuleManagerUI ui;
    private HttpServletRequest req;
    private HttpServletResponse res;
    private FilterChain chain;
    private PrintWriter writer;

    protected void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(WebContainerResources.class, WebContainerResourcesImpl.class);

        moduleManager = createStrictMock(ModuleManager.class);
        ui = createStrictMock(ModuleManagerUI.class);
        req = createStrictMock(HttpServletRequest.class);
        res = createStrictMock(HttpServletResponse.class);
        chain = createStrictMock(FilterChain.class);

        expect(moduleManager.getUI()).andReturn(ui);
        expect(req.getContextPath()).andReturn("/test");
        res.setContentType("text/html");
        writer = new PrintWriter(new StringWriter());
        expect(res.getWriter()).andReturn(writer);
    }

    public void testExecutesCorrectCommandBasedOnURI() throws Exception {
        // this is the interesting bit:
        expect(req.getRequestURI()).andReturn("/test/.magnolia/installer/foo");
        expect(ui.execute(writer, "foo")).andReturn(false);

        replay(moduleManager, ui, req, res, chain);
        final InstallFilter installFilter = new InstallFilter(moduleManager, null);
        installFilter.doFilter(req, res, chain);
        verify(moduleManager, ui, req, res, chain);
    }

    public void testPassesNullAsCommandIfNoneSpecifiedWithTrailingSlash() throws Exception {
        // this is the interesting bit:
        expect(req.getRequestURI()).andReturn("/test/.magnolia/installer/");
        expect(ui.execute(writer, null)).andReturn(false);

        replay(moduleManager, ui, req, res, chain);
        final InstallFilter installFilter = new InstallFilter(moduleManager, null);
        installFilter.doFilter(req, res, chain);
        verify(moduleManager, ui, req, res, chain);
    }

    public void testPassesNullAsCommandIfNoneSpecified() throws Exception {
        // this is the interesting bit:
        expect(req.getRequestURI()).andReturn("/test/.magnolia/installer");
        expect(ui.execute(writer, null)).andReturn(false);

        replay(moduleManager, ui, req, res, chain);
        final InstallFilter installFilter = new InstallFilter(moduleManager, null);
        installFilter.doFilter(req, res, chain);
        verify(moduleManager, ui, req, res, chain);
    }
}
