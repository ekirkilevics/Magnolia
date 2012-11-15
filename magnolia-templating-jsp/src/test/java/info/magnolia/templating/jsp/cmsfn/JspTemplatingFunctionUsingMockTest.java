/**
 * This file Copyright (c) 2012-2012 Magnolia International
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
import info.magnolia.context.MgnlContext;
import info.magnolia.templating.functions.TemplatingFunctions;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;

import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JspTemplatingFunctionUsingMockTest {

    private final String WEBSITE = "website";
    private final String DMS = "dms";

    @Before
    public void setUp() throws RepositoryException{
        Provider<AggregationState> aggregationProvider = new Provider<AggregationState>() {
            @Override
            public AggregationState get() {
                return MgnlContext.getAggregationState();
            }
        };

        TemplatingFunctions templatingFunctions = new TemplatingFunctions(aggregationProvider);
        ComponentsTestUtil.setInstance(TemplatingFunctions.class, templatingFunctions);

        MockUtil.initMockContext();
        MockUtil.setSessionAndHierarchyManager(new MockSession(WEBSITE));
        MockUtil.setSessionAndHierarchyManager(new MockSession(DMS));
    }

    @After
    public void tearDown() {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testGetContentByIdentifier() throws RepositoryException{
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("1");
        String id = addedNode.getIdentifier();

        // WHEN
        Node returnedNode = JspTemplatingFunction.contentByIdentifier(id, WEBSITE);

        // THEN
        assertEquals(addedNode, returnedNode);
    }

    @Test
    public void testGetContentByIdentifierFromAnotherThanDefaultRepo() throws RepositoryException{
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(DMS).getRootNode().addNode("1");
        String id = addedNode.getIdentifier();

        // WHEN
        Node returnedNode = JspTemplatingFunction.contentByIdentifier(id, DMS);

        // THEN
        assertEquals(addedNode, returnedNode);
    }

    @Test
    public void testGetContentByIdentifierWhenRepositoryIsEmpty() throws RepositoryException{
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("1");
        String id = addedNode.getIdentifier();

        // WHEN
        Node returnedNode = JspTemplatingFunction.contentByIdentifier(id, "");

        // THEN
        assertEquals(addedNode, returnedNode);
    }

    @Test
    public void testGetContentByIdentifierWhenRepositoryIsNull() throws RepositoryException{
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("1");
        String id = addedNode.getIdentifier();

        // WHEN
        Node returnedNode = JspTemplatingFunction.contentByIdentifier(id, null);

        // THEN
        assertEquals(addedNode, returnedNode);
    }

    @Test
    public void testGetContent() throws RepositoryException{
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("1");
        String path = addedNode.getPath();

        // WHEN
        Node returnedNode = JspTemplatingFunction.content(path, WEBSITE);

        // THEN
        assertEquals(addedNode, returnedNode);
    }

    @Test
    public void testGetContentFromAnotherThanDefaultRepo() throws RepositoryException{
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(DMS).getRootNode().addNode("1");
        String path = addedNode.getPath();

        // WHEN
        Node returnedNode = JspTemplatingFunction.content(path, DMS);

        // THEN
        assertEquals(addedNode, returnedNode);
    }

    @Test
    public void testGetContentWhenRepositoryIsEmpty() throws RepositoryException{
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("1");
        String path = addedNode.getPath();

        // WHEN
        Node returnedNode = JspTemplatingFunction.content(path, "");

        // THEN
        assertEquals(addedNode, returnedNode);
    }

    @Test
    public void testGetContentWhenRepositoryIsNull() throws RepositoryException{
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("1");
        String path = addedNode.getPath();

        // WHEN
        Node returnedNode = JspTemplatingFunction.content(path, null);

        // THEN
        assertEquals(addedNode, returnedNode);
    }
}
