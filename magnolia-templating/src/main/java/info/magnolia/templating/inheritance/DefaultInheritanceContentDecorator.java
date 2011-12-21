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
package info.magnolia.templating.inheritance;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.jcr.inheritance.InheritanceContentDecorator;
import org.apache.commons.lang.StringUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Provides an inheritance model that can be customized with configuration on the nodes. Inheritance can be completely
 * turned off or inheritance of nodes or properties can be turned off separately.
 *
 * The inheritance sources are found by looking at the node hierarchy, each node that qualifies as an anchor (node type
 * is mgnl:content) and has a node that with the same sub-path as the destination node has to its nearest parent is used.
 *
 * That is, for a destination node /page1/page2/main, the nearest anchor node is /page1/page2, therefor if there is a
 * node /page1/main then that is used as a source.
 *
 * @version $Id$
 */
public class DefaultInheritanceContentDecorator extends InheritanceContentDecorator {

    public DefaultInheritanceContentDecorator(Node destination) throws RepositoryException {
        super(destination);

        if (isInheritanceEnabled(destination)) {

            Node firstAnchor = findFirstAnchor();

            if (firstAnchor != null && firstAnchor.getDepth() != 0) {

                // relativePath is null if the destination and the first anchor is the same node
                String relativePathToAnchor = getPathRelativeToParent(firstAnchor, getDestination());

                Node node = firstAnchor.getParent();
                while (node.getDepth() != 0) {

                    if (isAnchor(node)) {

                        Node source = null;
                        if (relativePathToAnchor == null) {
                            source = node;
                        } else {
                            if (node.hasNode(relativePathToAnchor)) {
                                source = node.getNode(relativePathToAnchor);
                            }
                        }

                        if (source != null) {
                            addSource(source);

                            // if inheritance ends here we dont need to search for more sources
                            if (!isInheritanceEnabled(source)) {
                                break;
                            }
                        }
                    }

                    node = node.getParent();
                }
            }
        }
    }

    protected Node findFirstAnchor() throws RepositoryException {
        Node node = getDestination();
        while (node.getDepth() != 0) {
            if (isAnchor(node)) {
                return node;
            }
            node = node.getParent();
        }
        return null;
    }

    private String getPathRelativeToParent(Node parent, Node child) throws RepositoryException {
        String childPath = child.getPath();
        if (parent.getDepth() == 0) {
            return childPath;
        }
        String parentPathWithTrailingSlash = parent.getPath() + "/";
        if (!childPath.startsWith(parentPathWithTrailingSlash)) {
            return null;
        }
        return StringUtils.removeStart(childPath, parentPathWithTrailingSlash);
    }

    /**
     * True if this node is an anchor. By default true if this node is of type {@link info.magnolia.cms.core.MgnlNodeType#NT_CONTENT}.
     */
    protected boolean isAnchor(Node node) throws RepositoryException {
        return node.isNodeType(MgnlNodeType.NT_CONTENT);
    }

    protected boolean isInheritanceEnabled(Node node) throws RepositoryException {
        return !node.hasProperty("inheritance/enabled") || Boolean.parseBoolean(node.getProperty("inheritance/enabled").getString());
    }

    @Override
    protected boolean inheritsNodes(Node node) throws RepositoryException {
        return !node.hasProperty("inheritance/nodes") || Boolean.parseBoolean(node.getProperty("inheritance/nodes").getString());
    }

    @Override
    protected boolean inheritsProperties(Node node) throws RepositoryException {
        return !node.hasProperty("inheritance/properties") || Boolean.parseBoolean(node.getProperty("inheritance/properties").getString());
    }

    @Override
    protected boolean isSourceChildInherited(Node node) throws RepositoryException {
        return !node.hasProperty("inherited") || Boolean.parseBoolean(node.getProperty("inherited").getString());
    }
}
