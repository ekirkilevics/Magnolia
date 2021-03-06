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
package info.magnolia.rendering.template.configured;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.template.InheritanceConfiguration;

import java.util.Comparator;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Defines behavior for inheritance. Allows for enabling
 *
 * @version $Id$
 */
public class ConfiguredInheritance implements InheritanceConfiguration {

    public static final String COMPONENTS_ALL = "all";
    public static final String COMPONENTS_NONE = "none";
    public static final String COMPONENTS_FILTERED = "filtered";

    public static final String PROPERTIES_ALL = "all";
    public static final String PROPERTIES_NONE = "none";

    private Boolean enabled = false;
    private String components = COMPONENTS_FILTERED;
    private String properties = PROPERTIES_ALL;
    private Class<? extends AbstractPredicate<Node>> predicateClass;
    private Class<? extends Comparator<Node>> nodeComparatorClass;

    @Override
    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setComponents(String components) {
        this.components = components;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    @Override
    public Boolean isInheritsProperties() {
        return isEnabled() != null && isEnabled() && StringUtils.equalsIgnoreCase(StringUtils.trim(properties), PROPERTIES_ALL);
    }

    @Override
    public Boolean isInheritsComponents() {
        return isEnabled() != null && isEnabled() && (StringUtils.equalsIgnoreCase(StringUtils.trim(components), COMPONENTS_ALL) || StringUtils.equalsIgnoreCase(StringUtils.trim(components), COMPONENTS_FILTERED));
    }

    @Override
    public AbstractPredicate<Node> getComponentPredicate() {
        if (isEnabled() == null || !isEnabled()) {
            return new InheritNothingInheritancePredicate();
        }
        if (predicateClass != null) {
            return Components.newInstance(predicateClass);
        }
        if (StringUtils.equalsIgnoreCase(StringUtils.trim(components), COMPONENTS_ALL)) {
            return new AllComponentsAndResourcesInheritancePredicate();
        }
        if (StringUtils.equalsIgnoreCase(StringUtils.trim(components), COMPONENTS_FILTERED)) {
            return new FilteredComponentInheritancePredicate();
        }
        return new InheritNothingInheritancePredicate();
    }

    public void setPredicateClass(Class<? extends AbstractPredicate<Node>> predicateClass) {
        this.predicateClass = predicateClass;
    }

    @Override
    public Comparator<Node> getComponentComparator() {
        if (nodeComparatorClass != null) {
            return Components.newInstance(nodeComparatorClass);
        }
        return new NodeDepthComparator();
    }

    public void setNodeComparatorClass(Class<? extends Comparator<Node>> nodeComparatorClass) {
        this.nodeComparatorClass = nodeComparatorClass;
    }

    /**
     * Predicate for component inheritance that includes only nodes with a a property named 'inheritable' that needs to
     * be present and set to 'true'.
     */
    public static class FilteredComponentInheritancePredicate extends AbstractPredicate<Node> {

        public static final String INHERITED_PROPERTY_NAME = "inheritable";

        @Override
        public boolean evaluateTyped(Node node) {
            try {
                return NodeUtil.isNodeType(node, NodeTypes.Component.NAME) && (node.hasProperty(INHERITED_PROPERTY_NAME) && Boolean.parseBoolean(node.getProperty(INHERITED_PROPERTY_NAME).getString()));
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }
    }

    /**
     * Predicate for component inheritance that includes all components.
     */
    public static class AllComponentsAndResourcesInheritancePredicate extends AbstractPredicate<Node> {

        @Override
        public boolean evaluateTyped(Node node) {
            try {
                return NodeUtil.isNodeType(node, NodeTypes.Component.NAME) || NodeUtil.isNodeType(node, NodeTypes.Resource.NAME);
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }
    }

    /**
     * Predicate for component inheritance that includes no components.
     */
    public static class InheritNothingInheritancePredicate extends AbstractPredicate<Node> {

        @Override
        public boolean evaluateTyped(Node node) {
            return false;
        }
    }

    /**
     * Comparator for ordering nodes by depth placing nodes deeper in the hierarchy after those further up and ordering
     * nodes on the same level by the order they appear as siblings.
     */
    public static class NodeDepthComparator implements Comparator<Node> {

        @Override
        public int compare(Node lhs, Node rhs) {
            try {
                if (lhs.getDepth() != rhs.getDepth())
                    return lhs.getDepth() - rhs.getDepth();
                return getSiblingIndex(lhs) - getSiblingIndex(rhs);
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }

        private int getSiblingIndex(Node node) throws RepositoryException {
            if (node.getDepth() == 0) {
                return 0;
            }
            int index = 0;
            NodeIterator nodes = node.getParent().getNodes();
            while (nodes.hasNext()) {
                if (NodeUtil.isSame(node, nodes.nextNode())) {
                    return index;
                }
                index++;
            }
            return -1;
        }
    }
}
