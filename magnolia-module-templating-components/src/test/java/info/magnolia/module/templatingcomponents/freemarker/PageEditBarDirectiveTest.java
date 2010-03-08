/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.templatingcomponents.freemarker;

import info.magnolia.cms.security.AccessManager;
import info.magnolia.context.WebContext;

import javax.servlet.http.HttpServletRequest;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PageEditBarDirectiveTest extends DirectiveAbstractTestCase {
    @Override
    protected void setupExpectations(WebContext ctx, HttpServletRequest req, AccessManager accessManager) {
    }

    public void testBasicRendering() throws Exception {
        final String s = renderForTest("[@ui.page dialog='myDialog' /]");
        // TODO assertEquals("... not testing yet... ", s);
    }

    public void testCustomLabel() throws Exception {
        final String s = renderForTest("[@ui.page dialog='myDialog' editLabel='Edit this!' /]");
        assertEquals(true, s.contains("Edit this!"));
        assertEquals(false, s.contains("buttons.properties")); // the default button label
        assertEquals(false, s.contains("Properties")); // the i18n'd default button label
        // TODO assertEquals("... not testing yet... ", s);
    }

    public void testNoDialogButton() throws Exception {
        // usecase: [@ui.main dialog=def.dialog! /] - if you want to support templates which might not have a dialog defined.
        final String s = renderForTest("[@ui.page dialog=someVar! editLabel='should not appear' /]");
        assertEquals(false, s.contains("should not appear"));
        // TODO assertEquals("... not testing yet... ", s);
    }
}
