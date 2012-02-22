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
package info.magnolia.rendering.template.configured;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.jcr.MockNode;
import org.junit.After;
import org.junit.Test;

import javax.jcr.Node;
import java.util.Comparator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link ConfiguredInheritance}.
 *
 * @version $Id$
 */
public class ConfiguredInheritanceTest {

    @After
    public void tearDown() throws Exception {
        Components.setComponentProvider(null);
    }

    @Test
    public void testInheritsPropertiesWhenSetToAll() throws Exception {
        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setProperties(ConfiguredInheritance.PROPERTIES_ALL);
        assertTrue(inheritance.isInheritsProperties());
    }

    @Test
    public void testDoesNotInheritPropertiesWhenSetToNone() throws Exception {
        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setProperties(ConfiguredInheritance.PROPERTIES_NONE);
        assertFalse(inheritance.isInheritsProperties());
    }

    @Test
    public void testDoesNotInheritPropertiesWhenSetToBlank() throws Exception {
        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setProperties("");
        assertFalse(inheritance.isInheritsProperties());
    }

    @Test
    public void testDoesNotInheritPropertiesWhenSetToUnknownValue() throws Exception {
        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setProperties("unknown-value");
        assertFalse(inheritance.isInheritsProperties());
    }

    @Test
    public void testDoesNotInheritPropertiesWhenInheritanceDisabled() throws Exception {
        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(false);
        inheritance.setProperties(ConfiguredInheritance.PROPERTIES_ALL);
        assertFalse(inheritance.isInheritsProperties());
    }

    @Test
    public void testInheritsComponentsWhenSetToAll() throws Exception {
        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setComponents(ConfiguredInheritance.COMPONENTS_ALL);
        assertTrue(inheritance.isInheritsComponents());
    }

    @Test
    public void testInheritsComponentsWhenSetToFiltered() throws Exception {
        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setComponents(ConfiguredInheritance.COMPONENTS_FILTERED);
        assertTrue(inheritance.isInheritsComponents());
    }

    @Test
    public void testDoesNotInheritComponentsWhenSetToNone() throws Exception {
        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setComponents(ConfiguredInheritance.COMPONENTS_NONE);
        assertFalse(inheritance.isInheritsComponents());
    }

    @Test
    public void testDoesNotInheritComponentsWhenSetToBlank() throws Exception {
        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setComponents("");
        assertFalse(inheritance.isInheritsComponents());
    }

    @Test
    public void testDoesNotInheritComponentsWhenSetToUnknownValue() throws Exception {
        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setComponents("unknown-value");
        assertFalse(inheritance.isInheritsComponents());
    }

    @Test
    public void testDoesNotInheritComponentsWhenInheritanceDisabled() throws Exception {
        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(false);

        inheritance.setComponents(ConfiguredInheritance.COMPONENTS_ALL);
        assertFalse(inheritance.isInheritsComponents());

        inheritance.setComponents(ConfiguredInheritance.COMPONENTS_FILTERED);
        assertFalse(inheritance.isInheritsComponents());
    }

    @Test
    public void testReturnsSetNodeComparator() throws Exception {
        Components.setComponentProvider(new MockComponentProvider());

        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setNodeComparatorClass(TestNodeComparator.class);
        assertTrue(inheritance.getComponentComparator() instanceof TestNodeComparator);
    }

    @Test
    public void testReturnsNodeDepthComparatorByDefault() throws Exception {
        Components.setComponentProvider(new MockComponentProvider());

        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        assertTrue(inheritance.getComponentComparator() instanceof ConfiguredInheritance.NodeDepthComparator);
    }

    @Test
    public void testReturnsSetNodePredicate() throws Exception {
        Components.setComponentProvider(new MockComponentProvider());

        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setPredicateClass(TestNodePredicate.class);
        assertTrue(inheritance.getComponentPredicate() instanceof TestNodePredicate);
    }

    @Test
    public void testReturnsFilteredComponentInheritancePredicate() throws Exception {
        Components.setComponentProvider(new MockComponentProvider());

        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setComponents(ConfiguredInheritance.COMPONENTS_FILTERED);
        assertTrue(inheritance.getComponentPredicate() instanceof ConfiguredInheritance.FilteredComponentInheritancePredicate);
    }

    @Test
    public void testReturnsIncludeEverythingComponentPredicateWhenComponentsSetToAll() throws Exception {
        Components.setComponentProvider(new MockComponentProvider());

        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setComponents(ConfiguredInheritance.COMPONENTS_ALL);
        assertTrue(inheritance.getComponentPredicate() instanceof ConfiguredInheritance.AllComponentsInheritancePredicate);
    }

    @Test
    public void testReturnsIncludeNothingComponentPredicateWhenComponentsSetToNone() throws Exception {
        Components.setComponentProvider(new MockComponentProvider());

        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setComponents(ConfiguredInheritance.COMPONENTS_NONE);
        assertTrue(inheritance.getComponentPredicate() instanceof ConfiguredInheritance.InheritNothingInheritancePredicate);
    }

    @Test
    public void testReturnsIncludeNothingComponentPredicateWhenComponentsSetToBlank() throws Exception {
        Components.setComponentProvider(new MockComponentProvider());

        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setComponents("");
        assertTrue(inheritance.getComponentPredicate() instanceof ConfiguredInheritance.InheritNothingInheritancePredicate);
    }

    @Test
    public void testReturnsIncludeNothingComponentPredicateWhenComponentsSetToUnknownValue() throws Exception {
        Components.setComponentProvider(new MockComponentProvider());

        ConfiguredInheritance inheritance = new ConfiguredInheritance();
        inheritance.setEnabled(true);
        inheritance.setComponents("unknown-value");
        assertTrue(inheritance.getComponentPredicate() instanceof ConfiguredInheritance.InheritNothingInheritancePredicate);
    }

    @Test
    public void testFilteredComponentInheritancePredicate() throws Exception {
        ConfiguredInheritance.FilteredComponentInheritancePredicate predicate = new ConfiguredInheritance.FilteredComponentInheritancePredicate();
        MockNode node = new MockNode();
        node.setPrimaryType(MgnlNodeType.NT_COMPONENT);

        assertFalse(predicate.evaluateTyped(node));

        node.setProperty(ConfiguredInheritance.FilteredComponentInheritancePredicate.INHERITED_PROPERTY_NAME, "true");
        assertTrue(predicate.evaluateTyped(node));

        node.setProperty(ConfiguredInheritance.FilteredComponentInheritancePredicate.INHERITED_PROPERTY_NAME, "true");
        assertTrue(predicate.evaluateTyped(node));

        node.setProperty(ConfiguredInheritance.FilteredComponentInheritancePredicate.INHERITED_PROPERTY_NAME, "false");
        assertFalse(predicate.evaluateTyped(node));

        node.setProperty(ConfiguredInheritance.FilteredComponentInheritancePredicate.INHERITED_PROPERTY_NAME, "");
        assertFalse(predicate.evaluateTyped(node));
    }

    @Test
    public void testNodeDepthComparator() throws Exception {
        ConfiguredInheritance.NodeDepthComparator comparator = new ConfiguredInheritance.NodeDepthComparator();
        MockNode parent = new MockNode();
        Node child = parent.addNode("child");
        assertTrue(comparator.compare(parent, child) < 0);
        assertTrue(comparator.compare(child, parent) > 0);
        assertTrue(comparator.compare(child, child) == 0);
        assertTrue(comparator.compare(parent, parent) == 0);
    }

    @Test
    public void testNodeDepthComparatorForSiblings() throws Exception {
        ConfiguredInheritance.NodeDepthComparator comparator = new ConfiguredInheritance.NodeDepthComparator();
        MockNode parent = new MockNode();
        Node child1 = parent.addNode("child1");
        Node child2 = parent.addNode("child2");
        Node child3 = parent.addNode("child3");

        assertTrue(comparator.compare(child1, child1) == 0);
        assertTrue(comparator.compare(child1, child2) < 0);
        assertTrue(comparator.compare(child1, child3) < 0);

        assertTrue(comparator.compare(child2, child2) == 0);
        assertTrue(comparator.compare(child2, child1) > 0);
        assertTrue(comparator.compare(child2, child3) < 0);

        assertTrue(comparator.compare(child3, child3) == 0);
        assertTrue(comparator.compare(child3, child1) > 0);
        assertTrue(comparator.compare(child3, child2) > 0);
    }

    public static class TestNodeComparator implements Comparator<Node> {

        @Override
        public int compare(Node o1, Node o2) {
            return 0;
        }
    }

    public static class TestNodePredicate extends AbstractPredicate<Node> {

        @Override
        public boolean evaluateTyped(Node node) {
            return true;
        }
    }
}
