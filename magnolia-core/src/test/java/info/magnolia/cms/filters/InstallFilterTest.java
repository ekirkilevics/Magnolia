/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.filters;

import info.magnolia.module.ModuleManager;
import info.magnolia.module.ui.ModuleManagerUI;
import junit.framework.TestCase;
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
public class InstallFilterTest extends TestCase {
    private ModuleManager moduleManager;
    private ModuleManagerUI ui;
    private HttpServletRequest req;
    private HttpServletResponse res;
    private FilterChain chain;
    private PrintWriter writer;

    protected void setUp() throws Exception {
        super.setUp();
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
