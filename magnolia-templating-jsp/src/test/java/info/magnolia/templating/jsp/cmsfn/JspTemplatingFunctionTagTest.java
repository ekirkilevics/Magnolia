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
package info.magnolia.templating.jsp.cmsfn;


import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.templating.functions.TemplatingFunctions;
import info.magnolia.templating.jsp.AbstractTagTestCase;
import info.magnolia.test.ComponentsTestUtil;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
/**
 * This test class only check in the Tags are correctly called.
 * The TemplatingFunction method test is done in the Templating module.
 *
 * @version $Id$
 *
 */
public class JspTemplatingFunctionTagTest extends AbstractTagTestCase {

    public WebResponse response;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        Provider<AggregationState> aggregationProvider = new Provider<AggregationState>() {
            @Override
            public AggregationState get() {
                return MgnlContext.getAggregationState();
            }
        };

        TemplatingFunctions templatingFunctions = new TemplatingFunctions(aggregationProvider);
        ComponentsTestUtil.setInstance(TemplatingFunctions.class, templatingFunctions);
    }

    private WebRequest initTestCase(String methodToTest,String nodePath) throws Exception{
        final String jspPath = getClass().getName().replace('.', '/')+ methodToTest + ".jsp";
        final String jspUrl = "http://localhost" + CONTEXT + "/" + jspPath;

        final WebRequest request = new GetMethodWebRequest(jspUrl);
        ContentMap content = null;
        if(nodePath == null) {
            content = new ContentMap(getSession().getNode("/foo/bar/paragraphs/1"));
        } else {
            content = new ContentMap(getSession().getNode(nodePath));
        }

        runner.getSession(true).getServletContext().setAttribute("content",content);
        return request;
    }


    @Test
    public void testRoot() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Root",null);

        // WHEN
        response = runner.getResponse(request);


        // THEN
        String responseStr = response.getText();
        //Check the root with nodeTypeName
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=/foo/bar/paragraphs\n</div>"));
        //Check the root with nodeTypeName = ''
        assertThat(responseStr, containsString("<div id=\"2\">\n    res=/\n</div>"));
        //Check the root with nodeTypeName = null
        assertThat(responseStr, containsString("<div id=\"3\">\n    res=/\n</div>"));
    }


    @Test
    public void testAncestors() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Ancestors","/foo/bar");

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the ancestor with nodeType
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=\n</div>"));
        //Check the ancestor with nodeType = ''
        assertThat(responseStr, containsString("<div id=\"2\">\n    res=/foo\n</div>"));
        //Check the ancestor with nodeType = null
        assertThat(responseStr, containsString("<div id=\"3\">\n    res=/foo\n</div>"));
    }


    @Test
    public void testAsJCRNode() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("AsJCRNode",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the content with repository
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=/foo/bar/paragraphs/1\n</div>"));
    }


    @Test
    public void testChildren() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Children","/foo/bar");

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the content with repository
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=/foo/bar/paragraphs\n</div>"));
        //Check the content with repository = ''
        assertThat(responseStr, containsString("<div id=\"2\">\n    res=/foo/bar/MetaData\n</div>"));
        //Check the content with repository = null
        assertThat(responseStr, containsString("<div id=\"3\">\n    res=/foo/bar/paragraphs\n</div>"));
    }

    @Test
    public void testContent() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Content",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the content with repository
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=/foo/bar/paragraphs\n</div>"));
        //Check the content with repository = ''
        assertThat(responseStr, containsString("<div id=\"2\">\n    res=/foo/bar/paragraphs\n</div>"));
        //Check the content with repository = null
        assertThat(responseStr, containsString("<div id=\"3\">\n    res=/foo/bar/paragraphs\n</div>"));
    }

    @Test
    public void testCreateHtmlAttribute() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("CreateHtmlAttribute",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the inherit with nodeTypeName
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=a=\"b\"\n</div>"));
    }

    @Test
    public void testDecode() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Decode",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the inherit with nodeTypeName
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=/foo/bar/paragraphs/1\n</div>"));
    }

    @Test
    public void testExternalLink() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("ExternalLink",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the inherit with nodeTypeName
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=http://hello 1\n</div>"));
    }

    @Test
    public void testExternalLinkTitle() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("ExternalLinkTitle",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the inherit with nodeTypeName
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=http://hello 1\n</div>"));
    }

    @Test
    public void testInherit() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Inherit","/foo/bar");

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the inherit with nodeTypeName
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=/foo/bar/paragraphs\n</div>"));
        //Check the inherit with nodeTypeName = ''
        assertThat(responseStr, containsString("<div id=\"2\">\n    res=/foo/bar\n</div>"));
        //Check the inherit with nodeTypeName = null
        assertThat(responseStr, containsString("<div id=\"3\">\n    res=/foo/bar\n</div>"));
    }

    @Test
    public void testInheritList() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("InheritList","/foo/bar");

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the LinkForWorkspace
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=[info.magnolia.jcr.util.ContentMap@"));
    }

    @Test
    public void testInheritProperty() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("InheritProperty",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the LinkForWorkspace
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=/foo/bar/paragraphs/1/text\n</div>"));
    }

    @Test
    public void testIsAuthorInstance() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Is",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the LinkForWorkspace
        assertThat(responseStr, containsString("<div id=\"2\">\n    res=true\n</div>"));
    }

    @Test
    public void testIsEditMode() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Is",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the LinkForWorkspace
        assertThat(responseStr, containsString("<div id=\"4\">\n    res=true\n</div>"));
    }

    @Test
    public void testIsFromCurrentPage() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("IsFromCurrentPage",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the LinkForWorkspace
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=true\n</div>"));
    }

    @Test
    public void testIsInherited() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("IsInherited",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the LinkForWorkspace
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=false\n</div>"));
    }

    @Test
    public void testIsPreviewMode() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Is",null);

        // WHEN
        response = runner.getResponse(request);


        // THEN
        String responseStr = response.getText();
        //Check the LinkForWorkspace
        assertThat(responseStr, containsString("<div id=\"3\">\n    res=false\n</div>"));
    }

    @Test
    public void testIsPublicInstance() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Is",null);

        // WHEN
        response = runner.getResponse(request);


        // THEN
        String responseStr = response.getText();
        //Check the LinkForWorkspace
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=false\n</div>"));
    }

    @Test
    public void testLanguage() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Language",null);

        // WHEN
        response = runner.getResponse(request);


        // THEN
        String responseStr = response.getText();
        //Check the LinkForWorkspace
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=en_US\n</div>"));
    }

    @Test
    public void testLink() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Link",null);

        // WHEN
        response = runner.getResponse(request);


        // THEN
        String responseStr = response.getText();
        //Check the LinkForWorkspace
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=/foo/bar/paragraphs/1\n</div>"));
    }

    @Ignore
    //TODO: Define the testCase
    @Test
    public void testLinkForProperty() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("LinkForProperty",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
    }

    @Test
    public void testLinkForWorkspace() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("LinkForWorkspace",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the LinkForWorkspace
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=/foo/bar/paragraphs/0\n</div>"));
    }

    @Test
    public void testPage() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Page",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the page
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=/foo/bar\n</div>"));
    }

    @Test
    public void testParent() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Parent",null);

        // WHEN
        response = runner.getResponse(request);

        // THEN
        String responseStr = response.getText();
        //Check the parent with nodeTypeName
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=/foo/bar\n</div>"));
        //Check the parent with nodeTypeName = ''
        assertThat(responseStr, containsString("<div id=\"2\">\n    res=/foo/bar/paragraphs\n</div>"));
        //Check the parent with nodeTypeName = null
        assertThat(responseStr, containsString("<div id=\"3\">\n    res=/foo/bar/paragraphs\n</div>"));
    }

    @Test
    public void testSiblings() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("Siblings",null);

        // WHEN
        response = runner.getResponse(request);


        // THEN
        String responseStr = response.getText();
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=info.magnolia.cms.util.SiblingsHelper"));
    }

    @Test
    public void testMetadataProperty() throws Exception {
        // GIVEN
        WebRequest request = initTestCase("MetadataProperty",null);

        // WHEN
        response = runner.getResponse(request);


        // THEN
        String responseStr = response.getText();
        assertThat(responseStr, containsString("<div id=\"1\">\n    res=testParagraph1"));
    }

}
