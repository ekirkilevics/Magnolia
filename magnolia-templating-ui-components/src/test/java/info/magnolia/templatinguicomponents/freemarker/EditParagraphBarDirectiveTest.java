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
package info.magnolia.templatinguicomponents.freemarker;

import freemarker.template.TemplateModelException;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class EditParagraphBarDirectiveTest extends AbstractDirectiveTest {

    public void testRenderSimpleBar() throws Exception {
        final String result = renderForTest("[@ui.edit /]");

        // TODO assertEquals("..not testing yet..", result);
    }

    public void testRenderWithSpecificDialog() throws Exception {
        final String result = renderForTest("[@ui.edit dialog='myDialog' /]");

        // TODO assertEquals("..not testing yet..", result);
    }

    public void testRenderWithEditLabelAndNoOtherButtons() throws Exception {
        final String result = renderForTest("[@ui.edit editLabel='edit my paragraph' move=false delete=false /]");

        // TODO assertEquals("..not testing yet..", result);

        // TODO - also validate error messages such as The 'move' parameter must be a TemplateBooleanModel and is a SimpleScalar.
        // TODO re:the above - maybe we want to support SimpleScalar as well for booleans (move='false' instead of move=false) .. for ease of use ?
    }

    public void testThrowsExceptionForUnknownParameters() throws Exception {
        try {
            renderForTest("[@ui.edit fake='lol' /]");
            fail("should have failed");
        } catch (TemplateModelException e) {
            assertEquals("Unsupported parameter(s): {fake=lol}", e.getMessage());
        }
    }

}
