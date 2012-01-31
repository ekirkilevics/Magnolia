/**
 * This file Copyright (c) 2011 Magnolia International
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.rendering.context.AggregationStateBasedRenderingContext;
import info.magnolia.rendering.engine.OutputProvider;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @version $Id$
 */
public class InitElementTest {

    private InitElement element;
    private StringWriter out;
    private ConfiguredTemplateDefinition templateDefinition;
    private AggregationState aggregationState;

    @Before
    public void setUp() throws Exception {

        final MockHierarchyManager session = MockUtil.createHierarchyManager(
            "/foo/bar/baz@type=mgnl:page\n" +
            "/foo/bar/baz/main@type=mgnl:area\n" +
            "/foo/bar/baz/main/01@type=mgnl:component\n" +
            "/foo/bar/baz/main/01.text=dummy" +
            "/foo/bar/baz/main/01/MetaData.mgnl\\:template=testParagraph0");

        aggregationState = new AggregationState();
        aggregationState.setMainContent(session.getContent("/foo/bar/baz"));
        Content currentContent = session.getContent("/foo/bar/baz/main/01");
        aggregationState.setCurrentContent(currentContent);


        final WebContext ctx = mock(WebContext.class);
        final User user = mock(User.class);
        when(ctx.getUser()).thenReturn(user);
        final Locale localeEn = new Locale("en");
        when(ctx.getLocale()).thenReturn(localeEn);
        MgnlContext.setInstance(ctx);

        final ServerConfiguration serverCfg = new ServerConfiguration();
        serverCfg.setAdmin(true);
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverCfg);

        final I18nContentSupport defSupport = mock(I18nContentSupport.class);
        when(defSupport.getLocale()).thenReturn(localeEn);
        ComponentsTestUtil.setInstance(I18nContentSupport.class, defSupport);

        //set Rendering context
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState, null);
        out = new StringWriter();
        templateDefinition = new ConfiguredTemplateDefinition();
        templateDefinition.setDialog("customDialog");
        context.push(aggregationState.getCurrentContent().getJCRNode(), templateDefinition, new OutputProvider() {

            @Override
            public OutputStream getOutputStream() throws RenderException, IOException {
                return null;
            }

            @Override
            public Appendable getAppendable() throws RenderException, IOException {
                return out;
            }
        });

        element = new InitElement(serverCfg,context);
    }

    @Test
    public void testOutputContainsPageEditorJavascript() throws Exception {
        //GIVEN look at setUp method()

        //WHEN
        element.begin(out);
        //THEN
        assertThat(out.toString(),containsString(InitElement.PAGE_EDITOR_JS_SOURCE));
    }

    @Test
    public void testOutputContainsSourcesJavascript() throws Exception {
        //GIVEN look at setUp method()

        //WHEN
        element.begin(out);
        //THEN
        assertThat(out.toString(),containsString("/.magnolia/pages/javascript.js"));
        assertThat(out.toString(),containsString("/.resources/admin-js/dialogs/dialogs.js"));
    }

    @Test
    public void testOutputContainsSourcesCss() throws Exception {
        //GIVEN look at setUp method()

        //WHEN
        element.begin(out);
        //THEN
        assertThat(out.toString(),containsString("/.resources/admin-css/admin-all.css"));
    }

    @Test
    public void testOutputContainsGwtLocaleMetaProperty() throws Exception {
        //GIVEN look at setUp method()

        //WHEN
        element.begin(out);
        //THEN
        assertThat(out.toString(),containsString("<meta name=\"gwt:property\" content=\"locale=en\"/>"));
    }

    @Test
    public void testOutputContainsPageEditorStyles() throws Exception {
        //GIVEN look at setUp method()

        //WHEN
        element.begin(out);
        //THEN
        assertThat(out.toString(),containsString(InitElement.PAGE_EDITOR_CSS));
    }



    @Test
    public void testOutputContainsContent() throws Exception {
        //GIVEN

        //WHEN
        element.begin(out);
        //THEN
        assertThat(out.toString(),containsString("<!-- cms:page content=\"testSession:/foo/bar/baz/main/01\""));
        assertThat(out.toString(),containsString("<!-- /cms:page -->"));
    }

    @Test
    public void testOutputWithoutContent() throws Exception {
        //GIVEN
        aggregationState.setCurrentContent(null);
        //WHEN
        element.begin(out);
        //THEN
        assertThat(out.toString(),containsString("<!-- cms:page dialog=\"customDialog\" -->"));
    }

    @Test
    public void testOutputContainsContentAndDialog() throws Exception {
        //GIVEN
        templateDefinition.setDialog("testDialog");
        //WHEN
        element.begin(out);
        //THEN
        assertThat(out.toString(),containsString("<!-- cms:page content=\"testSession:/foo/bar/baz/main/01\" dialog=\"testDialog\" -->"));
    }

    @Test
    public void testOutputEndPart() throws Exception {
        //GIVEN

        //WHEN
        element.end(out);
        //THEN
        assertThat(out.toString(),containsString("<!-- end js and css added by @cms.init -->"));
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        SystemProperty.clear();
    }
}
