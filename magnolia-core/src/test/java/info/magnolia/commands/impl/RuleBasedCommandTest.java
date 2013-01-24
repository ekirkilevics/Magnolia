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

import info.magnolia.cms.util.Rule;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.RepositoryTestCase;

import java.io.ByteArrayInputStream;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.ValueFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for the RuleBasedCommand class.
 */
public class RuleBasedCommandTest extends RepositoryTestCase {

    public class DummyRuleBasedCommand extends RuleBasedCommand {
        @Override
        public boolean execute(Context context) throws Exception {
            return true;
        }
    }

    private RuleBasedCommand cmd;

    private Node content;
    private Node resource;
    private Node metadata;
    private Node folder;
    private Node page;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        cmd = new DummyRuleBasedCommand();

        Node root = MgnlContext.getJCRSession("website").getRootNode();
        content = root.addNode("content", NodeTypes.ContentNode.NAME);

        resource = root.addNode("resource", NodeTypes.Resource.NAME);
        resource.setProperty("jcr:mimeType", "text/plain");
        ValueFactory factory = root.getSession().getValueFactory();
        Binary bin = factory.createBinary(new ByteArrayInputStream("data".getBytes()));
        resource.setProperty("jcr:data", bin);
        NodeTypes.LastModified.update(resource);

        metadata = root.addNode("metadata", NodeTypes.MetaData.NAME);
        folder = root.addNode("folder", NodeTypes.Folder.NAME);
        page = root.addNode("page", NodeTypes.Page.NAME);

        root.getSession().save();
    }

    @Test
    public void testDefaultRule() throws Exception {
        Rule rule = cmd.getRule();

        // allowed nodes
        assertTrue(rule.isAllowed(content));
        assertTrue(rule.isAllowed(resource));
        assertTrue(rule.isAllowed(metadata));
        // not allowed
        assertFalse(rule.isAllowed(folder));
        assertFalse(rule.isAllowed(page));
    }

    @Test
    public void testSetItemTypes() throws Exception {
        cmd.setItemTypes(NodeTypes.Folder.NAME + "," + NodeTypes.Page.NAME);

        Rule rule = cmd.getRule();

        // allowed nodes
        assertTrue(rule.isAllowed(folder));
        assertTrue(rule.isAllowed(page));
        assertTrue(rule.isAllowed(resource));
        assertTrue(rule.isAllowed(metadata));
        // not allowed
        assertFalse(rule.isAllowed(content));
    }

    @Test
    public void testSetRule() throws Exception {
        Rule rule = new Rule();
        rule.addAllowType(NodeTypes.Page.NAME);
        cmd.setRule(rule);

        rule = cmd.getRule();
        // allowed nodes
        assertTrue(rule.isAllowed(page));
        // not allowed
        assertFalse(rule.isAllowed(resource));
        assertFalse(rule.isAllowed(metadata));
        assertFalse(rule.isAllowed(folder));
        assertFalse(rule.isAllowed(content));
    }

    @Test
    public void testRuleOverridesItemTypes() throws Exception {
        cmd.setItemTypes(NodeTypes.Folder.NAME + "," + NodeTypes.Page.NAME);

        Rule rule = cmd.getRule();

        // allowed nodes
        assertTrue(rule.isAllowed(folder));
        assertTrue(rule.isAllowed(page));
        assertTrue(rule.isAllowed(resource));
        assertTrue(rule.isAllowed(metadata));
        // not allowed
        assertFalse(rule.isAllowed(content));

        // now, replace the rule with our own rule
        rule = new Rule();
        rule.addAllowType(NodeTypes.Page.NAME);
        cmd.setRule(rule);

        // and test
        rule = cmd.getRule();
        // allowed nodes
        assertTrue(rule.isAllowed(page));
        // not allowed
        assertFalse(rule.isAllowed(resource));
        assertFalse(rule.isAllowed(metadata));
        assertFalse(rule.isAllowed(folder));
        assertFalse(rule.isAllowed(content));
    }
}
