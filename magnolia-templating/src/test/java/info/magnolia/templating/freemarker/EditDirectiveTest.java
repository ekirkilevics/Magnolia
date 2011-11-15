/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.templating.freemarker;

import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;

import org.junit.Before;
import org.junit.Test;

import freemarker.template.TemplateModelException;
import static org.junit.Assert.*;

/**
 * $Id$
 */
public class EditDirectiveTest extends AbstractDirectiveTestCase {
    private ConfiguredTemplateDefinition renderableDef = new ConfiguredTemplateDefinition();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        renderableDef.setId("testParagraph1");
        renderableDef.setTitle("Test Paragraph 1");
        renderableDef.setDialog("testDialog");
    }

    @Test
    public void testRenderSimpleBar() throws Exception {
        String result = renderForTest("[@cms.edit /]", renderableDef);
        assertEquals("<cms:edit content=\"testWorkspace:/foo/bar/paragraphs/1\" dialog=\"testDialog\" template=\"testParagraph1\"></cms:edit>\n", result);
    }

    @Test
    public void testRenderWithDialog() throws Exception {
        final String result = renderForTest("[@cms.edit dialog='testDialog' /]", renderableDef);
        assertEquals("<cms:edit content=\"testWorkspace:/foo/bar/paragraphs/1\" dialog=\"testDialog\" template=\"testParagraph1\"></cms:edit>\n", result);
    }

    @Test
    public void testThrowsExceptionForUnknownParameters() throws Exception {
        try {
            renderForTest("[@cms.edit fake='lol' /]", null);
            fail("should have failed");
        } catch (TemplateModelException e) {
            assertEquals("Unsupported parameter(s): {fake=lol}", e.getMessage());
        }
    }

}
