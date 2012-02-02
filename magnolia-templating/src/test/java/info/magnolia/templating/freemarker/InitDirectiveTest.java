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

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;

import org.junit.Before;
import org.junit.Test;

/**
 * $Id$
 */
public class InitDirectiveTest extends AbstractDirectiveTestCase {

    private ConfiguredTemplateDefinition renderableDef;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        renderableDef = new ConfiguredTemplateDefinition();
    }


    @Test
    public void testRenderSimpleBar() throws Exception {
        // GIVEN

        // WHEN
        final String result = renderForTest("[@cms.init /]", renderableDef);

        // THEN
        assertThat(result, containsString("<meta name=\"gwt:property\" content=\"locale=en_US\"/>"));
        assertThat(result, containsString("<script type=\"text/javascript\" src=\"test/.magnolia/pages/messages.en.js\"></script>"));
        assertThat(result, containsString(".resources/magnolia-templating-editor/css/editor.css\"></link>"));
        assertThat(result, containsString("<script type=\"text/javascript\" src=\"test/.resources/admin-js/dialogs/dialogs.js\"></script>"));
    }

    @Test
    public void testRenderWithContent() throws Exception {
        // GIVEN

        // WHEN
        final String result = renderForTest("[@cms.init /]", renderableDef);

        // THEN
        assertThat(result, containsString("<!-- cms:page content=\"testWorkspace:/foo/bar/paragraphs/1\" preview=\"false\" -->\n<!-- /cms:page -->"));

    }


    @Test
    public void testRenderWithContentAndDialog() throws Exception {
        // GIVEN

        // WHEN
        final String result = renderForTest("[@cms.init dialog='newTagDefinedDialog' /]", renderableDef);

        // THEN
        assertThat(result, containsString("<!-- cms:page content=\"testWorkspace:/foo/bar/paragraphs/1\" dialog=\"newTagDefinedDialog\" preview=\"false\" -->\n<!-- /cms:page -->"));
    }
}
