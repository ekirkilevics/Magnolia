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
package info.magnolia.templating.elements;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 *
 * @version $Id$
 */
public class InitElementTest extends AbstractElementTestCase {

    private InitElement element;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        element = new InitElement(getServerCfg(), getContext());
    }

    @Test
    @Ignore("See SCRUM-1239. Will most likely be removed in 5.0")
    public void testOutputContainsPageEditorJavascript() throws Exception {
        // GIVEN look at setUp method()

        // WHEN
        element.begin(out);
        // THEN
        assertThat(out.toString(), containsString(InitElement.PAGE_EDITOR_JS_SOURCE));
    }

    @Test
    public void testOutputContainsSourcesJavascript() throws Exception {
        // GIVEN look at setUp method()

        // WHEN
        element.begin(out);
        // THEN
        assertThat(out.toString(), containsString("/.magnolia/pages/javascript.js"));
        assertThat(out.toString(), containsString("/.resources/admin-js/dialogs/dialogs.js"));
    }

    @Test
    public void testOutputContainsSourcesCss() throws Exception {
        // GIVEN look at setUp method()

        // WHEN
        element.begin(out);
        // THEN
        assertThat(out.toString(), containsString("/.resources/admin-css/admin-all.css"));
    }

    @Test
    public void testOutputContainsGwtLocaleMetaProperty() throws Exception {
        // GIVEN look at setUp method()

        // WHEN
        element.begin(out);
        // THEN
        assertThat(out.toString(), containsString("<meta name=\"gwt:property\" content=\"locale=en\"/>"));
    }

    @Test
    @Ignore("See SCRUM-1239. Will most likely be removed in 5.0")
    public void testOutputContainsPageEditorStyles() throws Exception {
        // GIVEN look at setUp method()

        // WHEN
        element.begin(out);
        // THEN
        assertThat(out.toString(), containsString(InitElement.PAGE_EDITOR_CSS));
    }

    @Test
    public void testOutputContainsContent() throws Exception {
        // GIVEN

        // WHEN
        element.begin(out);
        // THEN
        assertThat(out.toString(), containsString("<!-- cms:page content=\"website:/foo/bar/paragraphs\" dialog=\"dialog\" preview=\"false\" -->"));
        assertThat(out.toString(), containsString("<!-- /cms:page -->"));
    }

    @Test
    public void testOutputWithoutContent() throws Exception {
        // GIVEN
        getAggregationState().setCurrentContent(null);
        // WHEN
        element.begin(out);
        // THEN
        assertThat(out.toString(), containsString("<!-- cms:page dialog=\"dialog\" preview=\"false\" -->"));
    }

    @Test
    public void testOutputContainsContentAndDialog() throws Exception {
        // GIVEN
        getTemplateDefinition().setDialog("testDialog");
        // WHEN
        element.begin(out);
        // THEN
        assertThat(out.toString(), containsString("<!-- cms:page content=\"website:/foo/bar/paragraphs\" dialog=\"testDialog\" preview=\"false\" -->"));
    }

    @Test
    public void testOutputEndPart() throws Exception {
        // GIVEN

        // WHEN
        element.end(out);
        // THEN
        assertThat(out.toString(), containsString("<!-- end js and css added by @cms.init -->"));
    }

}
