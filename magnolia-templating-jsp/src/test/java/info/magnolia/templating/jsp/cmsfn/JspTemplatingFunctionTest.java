/**
 * This file Copyright (c) 2012 Magnolia International
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

import static org.junit.Assert.assertEquals;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.BootstrapUtil;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.link.LinkUtil;
import info.magnolia.templating.functions.TemplatingFunctions;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;

import javax.inject.Provider;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Test;

public class JspTemplatingFunctionTest extends RepositoryTestCase {

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
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
    }

    @Test
    public void testLink() throws Exception {
        // GIVEN
        BootstrapUtil.bootstrap(new String[] { "/website.01.xml" }, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);

        Node test = MgnlContext.getJCRSession("website").getRootNode().getNode("01");
        ContentMap contentMap = new ContentMap(test);
        // THEN

        // the old way - still works, but is not easily used in templates and not exposed as function
        String link3 = LinkUtil.createLink(ContentUtil.asContent(test).getNodeData("image"));
        assertEquals("/01/image/Cents1-1.jpg", link3);

        // something once can do if (s)he knows about repo structure
        String link = JspTemplatingFunction.linkForProperty(((ContentMap) contentMap.get("image")).getJCRNode().getProperty("jcr:data"));
        assertEquals("/01/image/Cents1-1.jpg", link);

        // old style using ${content.image} should still work (imho)
        // this was partially working in 4.5.0-4.5.2 by generating wrong link and servlet being lenient enough to deliver right binary
        String link1 = JspTemplatingFunction.link((ContentMap) contentMap.get("image"));
        assertEquals("/01/image/Cents1-1.jpg", link1);

        // again should work, but doesn't
        String link2 = JspTemplatingFunction.linkForWorkspace(test.getSession().getWorkspace().getName(), test.getNode("image").getIdentifier());
        assertEquals("/01/image/Cents1-1.jpg", link2);
    }

    @Test
    public void testGetContentByIdentifier() throws RepositoryException{
        try {
            // GIVEN
            MockUtil.initMockContext();
            String repository = "website";
            MockSession session = new MockSession(repository);
            MockUtil.setSessionAndHierarchyManager(session);
            Node rootNode = session.getRootNode();
            Node addedNode = rootNode.addNode("1");
            String id = addedNode.getIdentifier();

            //THEN

            //get content by identifier when repository was provided
            Node returnedNode1 = JspTemplatingFunction.contentByIdentifier(id, repository);
            assertEquals(addedNode, returnedNode1);

            //get content by identifier when repository was empty -> will taken the default (website)
            Node returnedNode2 = JspTemplatingFunction.contentByIdentifier(id, "");
            assertEquals(addedNode, returnedNode2);
        } finally {
            MgnlContext.setInstance(null);
        }
    }
}
