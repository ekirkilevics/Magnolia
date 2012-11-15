/**
 * This file Copyright (c) 2011-2012 Magnolia International
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

import static org.easymock.EasyMock.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 *
 */
public class DialogMVCServletTest {
    private HttpServletRequest req;
    private DialogMVCServlet dialogServlet;

    @Before
    public void setUp() {
        req = createMock(HttpServletRequest.class);
        dialogServlet = new DialogMVCServlet();
    }

    @Test
    public void testGetDialogNameReturnsMgnlDialogRequestParameter() throws Exception {
        //GIVEN
        String expected = "myModule:myFancyDialog";
        expect(req.getParameter("mgnlDialog")).andReturn(expected);
        expect(req.getParameter("mgnlParagraph")).andReturn("foo:bar");
        expect(req.getPathInfo()).andReturn("/fooBar.html");
        replay(req);
        //WHEN
        String actual = dialogServlet.getDialogName(req);
        //THEN
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetDialogNameReturnsMgnlParagraphRequestParameter() throws Exception {
        //GIVEN
        String expected = "myModule:myFancyDialog";
        expect(req.getParameter("mgnlDialog")).andReturn(null);
        expect(req.getParameter("mgnlParagraph")).andReturn(expected);
        expect(req.getPathInfo()).andReturn("/fooBar.html");
        replay(req);
        //WHEN
        String actual = dialogServlet.getDialogName(req);
        //THEN
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetDialogNameReturnsSelectParagraph() throws Exception {
        //GIVEN
        String expected = "adminInterface:selectParagraph";
        expect(req.getParameter("mgnlDialog")).andReturn("baz:foo");
        expect(req.getParameter("mgnlParagraph")).andReturn("foo:bar");
        expect(req.getPathInfo()).andReturn("/selectParagraph.html");
        replay(req);
        //WHEN
        String actual = dialogServlet.getDialogName(req);
        //THEN
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetDialogNameReturnsEditParagraph() throws Exception {
        //GIVEN
        String expected = "adminInterface:editParagraph";
        expect(req.getParameter("mgnlDialog")).andReturn("baz:foo");
        expect(req.getParameter("mgnlParagraph")).andReturn("foo:bar");
        expect(req.getPathInfo()).andReturn("/editParagraph.html");
        replay(req);
        //WHEN
        String actual = dialogServlet.getDialogName(req);
        //THEN
        Assert.assertEquals(expected, actual);
    }
}
