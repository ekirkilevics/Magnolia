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
package info.magnolia.commands.impl;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;

public class VersionCommandTest extends RepositoryTestCase {

    private String COMMENT_PROPERTY = RepositoryConstants.NAMESPACE_PREFIX + ":" + Context.ATTRIBUTE_COMMENT;

    private Node node;
    private Node childNode;
    private VersionCommand cmd;
    private Context ctx;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        ctx = mock(Context.class);

        node = MgnlContext.getJCRSession("website").getRootNode().addNode("home-test", MgnlNodeType.NT_PAGE);
        childNode = node.addNode("child-test", MgnlNodeType.NT_PAGE);
        node.getSession().save();

        cmd = new VersionCommand();
        cmd.setComment("comment");
        cmd.setRepository("website");
        cmd.setUuid(node.getIdentifier());
        cmd.setRule(new Rule(new String[]{MgnlNodeType.NT_CONTENTNODE, MgnlNodeType.NT_METADATA }));
    }

    @Test
    public void testHadleWithVersionCommentWhenRecursiveFalse() throws  RepositoryException{
        // GIVEN
        cmd.setRecursive(false);

        // WHEN
        assertTrue(cmd.execute(ctx));

        // THEN
        Node nodeVersionMetaData = VersionManager.getInstance().getVersion(node, "1.0");
        assertTrue(nodeVersionMetaData.hasProperty(COMMENT_PROPERTY));
        assertEquals("comment", nodeVersionMetaData.getProperty(COMMENT_PROPERTY).getString());

        assertFalse(node.hasProperty(COMMENT_PROPERTY));
    }

    @Test
    public void testHadleWithVersionCommentWhenWhenRecursiveTrue() throws  RepositoryException{
        // GIVEN
        cmd.setRecursive(true);

        // WHEN
        assertTrue(cmd.execute(ctx));

        // THEN
        Node nodeVersionMetaData = VersionManager.getInstance().getVersion(node, "1.0");
        assertTrue(nodeVersionMetaData.hasProperty(COMMENT_PROPERTY));
        assertEquals("comment", nodeVersionMetaData.getProperty(COMMENT_PROPERTY).getString());
        Node childVersionMetaData = VersionManager.getInstance().getVersion(childNode, "1.0");
        assertTrue(childVersionMetaData.hasProperty(COMMENT_PROPERTY));
        assertEquals("comment", childVersionMetaData.getProperty(COMMENT_PROPERTY).getString());

        assertFalse(node.hasProperty(COMMENT_PROPERTY));
        assertFalse(childNode.hasProperty(COMMENT_PROPERTY));
    }

    @Test
    public void testWhenVersionCommentIsNullAndNodeAlreadyHasVersionComment() throws  RepositoryException{
        // GIVEN
        node.setProperty(COMMENT_PROPERTY, "already presented comment");
        cmd.setComment(null);
        cmd.setRecursive(false);

        // WHEN
        assertTrue(cmd.execute(ctx));

        // THEN
        Node nodeVersionMetaData = VersionManager.getInstance().getVersion(node, "1.0");
        assertTrue(nodeVersionMetaData.hasProperty(COMMENT_PROPERTY));
        assertEquals(StringUtils.EMPTY, nodeVersionMetaData.getProperty(COMMENT_PROPERTY).getString());

        // the removal of metadata property just replaces its value with an
        // EMPTY string
        assertTrue(StringUtils.EMPTY.equals(node.getProperty(COMMENT_PROPERTY).getString()));
    }
}
