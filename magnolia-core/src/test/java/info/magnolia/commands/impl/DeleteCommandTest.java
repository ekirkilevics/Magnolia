/**
 * This file Copyright (c) 2013 Magnolia International
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for the DeleteCommand.
 * 
 */
public class DeleteCommandTest extends RepositoryTestCase {

    private static final String WEBSITE = "website";

    private Node node;
    private Node firstLevel;
    private Node secondLevel;
    private DeleteCommand cmd;
    private Context ctx;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        ctx = mock(Context.class);

        node = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("delete-test", NodeTypes.Content.NAME);
        firstLevel = node.addNode("first-level", NodeTypes.Content.NAME);
        secondLevel = firstLevel.addNode("second-level", NodeTypes.Content.NAME);
        node.getSession().save();

        cmd = new DeleteCommand();
    }

    @Test
    public void testDeleteLeaveNode() throws RepositoryException {
        // GIVEN
        cmd.setRepository(WEBSITE);
        cmd.setPath(secondLevel.getPath());

        // WHEN
        assertTrue(cmd.execute(ctx));

        // THEN
        assertTrue(node.hasNode("first-level"));
        assertFalse(node.hasNode("first-level/second-level"));
    }

    @Test
    public void testDeleteNodeWithSubnodes() throws RepositoryException {
        // GIVEN
        cmd.setRepository(WEBSITE);
        cmd.setPath(firstLevel.getPath());

        // WHEN
        assertTrue(cmd.execute(ctx));

        // THEN
        assertFalse(node.hasNode("first-level"));
        assertFalse(node.hasNode("first-level/second-level"));
    }

    @Test
    public void testDeleteNonexistentNode() throws RepositoryException {
        // GIVEN
        cmd.setRepository(WEBSITE);
        cmd.setPath("/some/non/existent/path");

        // WHEN
        assertFalse(cmd.execute(ctx));

        // THEN
        // just check, whether the existing nodes are untouched
        assertTrue(node.hasNode("first-level"));
        assertTrue(node.hasNode("first-level/second-level"));
    }
}
