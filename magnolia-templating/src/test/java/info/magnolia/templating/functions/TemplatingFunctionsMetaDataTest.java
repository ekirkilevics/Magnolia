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
package info.magnolia.templating.functions;

import static org.junit.Assert.assertEquals;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.MetaData;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockNode;

import java.util.Calendar;

import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.util.ISO8601;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link info.magnolia.templating.functions.TemplatingFunctions#metaData}.
 */
public class TemplatingFunctionsMetaDataTest {

    private TemplatingFunctions functions;

    @Before
    public void setUp() {
        MockContext ctx = new MockWebContext();
        MgnlContext.setInstance(ctx);
        Provider<AggregationState> aggregationProvider = new Provider<AggregationState>() {
            @Override
            public AggregationState get() {
                return MgnlContext.getAggregationState();
            }
        };
        functions = new TemplatingFunctions(aggregationProvider);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeprecatedTitle() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        // WHEN
        functions.metaData(node, MetaData.TITLE);
        // THEN
        // IllegalArgumentException is thrown
    }

    @Test
    public void testDeprecatedCreationDate() throws RepositoryException {
        // GIVEN
        Calendar calendar = Calendar.getInstance();
        Node node = new MockNode();
        node.setProperty(NodeTypes.Created.CREATED, calendar);
        // WHEN
        String property = functions.metaData(node, MetaData.CREATION_DATE);
        //THEN
        assertEquals(ISO8601.format(calendar), property);
    }

    @Test
    public void testDeprecatedLastModified() throws RepositoryException {
        // GIVEN
        Calendar calendar = Calendar.getInstance();
        Node node = new MockNode();
        node.setProperty(NodeTypes.LastModified.LAST_MODIFIED, calendar);

        // WHEN
        String property = functions.metaData(node, MetaData.LAST_MODIFIED);
        //THEN
        assertEquals(ISO8601.format(calendar), property);
    }

    @Test
    public void testDeprecatedLastAction() throws RepositoryException {
        // GIVEN
        Calendar calendar = Calendar.getInstance();
        Node node = new MockNode();
        node.setProperty(NodeTypes.Activatable.LAST_ACTIVATED, calendar);
        // WHEN
        String property = functions.metaData(node, MetaData.LAST_ACTION);
        //THEN
        assertEquals(ISO8601.format(calendar), property);
    }

    @Test
    public void testDeprecatedAuthorId() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        node.setProperty(NodeTypes.LastModified.LAST_MODIFIED_BY, "testuser");
        // WHEN
        String property = functions.metaData(node, MetaData.AUTHOR_ID);
        //THEN
        assertEquals("testuser", property);
    }

    @Test
    public void testDeprecatedActivatorId() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        node.setProperty(NodeTypes.Activatable.LAST_ACTIVATED_BY, "testuser");
        // WHEN
        String property = functions.metaData(node, MetaData.ACTIVATOR_ID);
        //THEN
        assertEquals("testuser", property);
    }

    @Test
    public void testDeprecatedTemplate() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        node.setProperty(NodeTypes.Renderable.TEMPLATE, "test:pages/main");
        // WHEN
        String property = functions.metaData(node, MetaData.TEMPLATE);
        //THEN
        assertEquals("test:pages/main", property);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeprecatedTemplateType() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        // WHEN
        functions.metaData(node, MetaData.TEMPLATE_TYPE);
        // THEN
        // IllegalArgumentException is thrown
    }

    @Test
    public void testDeprecatedActivated() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        node.setProperty(NodeTypes.Activatable.ACTIVATION_STATUS, true);
        // WHEN
        String property = functions.metaData(node, MetaData.ACTIVATED);
        //THEN
        assertEquals("true", property);
    }

    @Test
    public void testCreated() throws RepositoryException {
        // GIVEN
        Calendar calendar = Calendar.getInstance();
        Node node = new MockNode();
        node.setProperty(NodeTypes.Created.CREATED, calendar);
        // WHEN
        String created = functions.metaData(node, NodeTypes.Created.CREATED);
        //THEN
        assertEquals(ISO8601.format(calendar), created);
    }

    @Test
    public void testCreatedBy() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        node.setProperty(NodeTypes.Created.CREATED_BY, "testuser");
        // WHEN
        String createdBy = functions.metaData(node, NodeTypes.Created.CREATED_BY);
        //THEN
        assertEquals("testuser", createdBy);
    }

    @Test
    public void testLastModified() throws RepositoryException {
        // GIVEN
        Calendar calendar = Calendar.getInstance();
        Node node = new MockNode();
        node.setProperty(NodeTypes.LastModified.LAST_MODIFIED, calendar);
        // WHEN
        String created = functions.metaData(node, NodeTypes.LastModified.LAST_MODIFIED);
        //THEN
        assertEquals(ISO8601.format(calendar), created);
    }

    @Test
    public void testLastModifiedBy() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        node.setProperty(NodeTypes.LastModified.LAST_MODIFIED_BY, "testuser");
        // WHEN
        String createdBy = functions.metaData(node, NodeTypes.LastModified.LAST_MODIFIED_BY);
        //THEN
        assertEquals("testuser", createdBy);
    }

    @Test
    public void testTemplate() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        node.setProperty(NodeTypes.Renderable.TEMPLATE, "test:pages/main");

        // WHEN
        String template = functions.metaData(node, NodeTypes.Renderable.TEMPLATE);
        //THEN
        assertEquals("test:pages/main", template);
    }

    @Test
    public void testLastActivated() throws RepositoryException {
        // GIVEN
        Calendar calendar = Calendar.getInstance();
        Node node = new MockNode();
        node.setProperty(NodeTypes.Activatable.LAST_ACTIVATED, calendar);
        // WHEN
        String lastActivated = functions.metaData(node, NodeTypes.Activatable.LAST_ACTIVATED);
        //THEN
        assertEquals(ISO8601.format(calendar), lastActivated);
    }

    @Test
    public void testLastActivatedBy() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        node.setProperty(NodeTypes.Activatable.LAST_ACTIVATED_BY, "testuser");
        // WHEN
        String lastActivatedBy = functions.metaData(node, NodeTypes.Activatable.LAST_ACTIVATED_BY);
        //THEN
        assertEquals("testuser", lastActivatedBy);
    }

    @Test
    public void testActivationStatus() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        node.setProperty(NodeTypes.Activatable.ACTIVATION_STATUS, false);
        // WHEN
        String activationStatus = functions.metaData(node, NodeTypes.Activatable.ACTIVATION_STATUS);
        //THEN
        assertEquals("0", activationStatus);
    }


    @Test
    public void testDeleted() throws RepositoryException {
        // GIVEN
        Calendar calendar = Calendar.getInstance();
        Node node = new MockNode();
        node.setProperty(NodeTypes.Deleted.DELETED, calendar);
        // WHEN
        String deleted = functions.metaData(node, NodeTypes.Deleted.DELETED);
        //THEN
        assertEquals(ISO8601.format(calendar), deleted);
    }

    @Test
    public void testDeletedBy() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        node.setProperty(NodeTypes.Deleted.DELETED_BY, "testuser");
        // WHEN
        String deletedBy = functions.metaData(node, NodeTypes.Deleted.DELETED_BY);
        //THEN
        assertEquals("testuser", deletedBy);
    }

    @Test
    public void testDeletedComment() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        node.setProperty(NodeTypes.Deleted.COMMENT, "a comment");
        // WHEN
        String comment = functions.metaData(node, NodeTypes.Deleted.COMMENT);
        //THEN
        assertEquals("a comment", comment);
    }

    @Test
    public void testVersionableComment() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        node.setProperty(NodeTypes.Versionable.COMMENT, "a comment");
        // WHEN
        String comment = functions.metaData(node, NodeTypes.Versionable.COMMENT);
        //THEN
        assertEquals("a comment", comment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownPropertyThrowsException() throws RepositoryException {
        // GIVEN
        Node node = new MockNode();
        // WHEN
        functions.metaData(node, "unknownProperty");
        // THEN
        // IllegalArgumentException is thrown
    }
}
