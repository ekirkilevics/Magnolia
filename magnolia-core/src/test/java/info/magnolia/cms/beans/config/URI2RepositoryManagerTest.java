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
package info.magnolia.cms.beans.config;

import static org.mockito.Mockito.*;

import javax.jcr.Node;
import javax.jcr.Property;

import junit.framework.TestCase;
import info.magnolia.cms.core.NodeData;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.link.Link;
import info.magnolia.test.ComponentsTestUtil;

/**
* 
* URI2RepositoryManagerTest.
*/
public class URI2RepositoryManagerTest extends TestCase{

    private NodeData nodeData = mock(NodeData.class);
    private Property property = mock(Property.class);
    private Node node = mock(Node.class);

    public void testGetURIWhenLinkIsEditorBinaryLinkAndPrefixHandleIsSet() throws Exception{
        when(property.getParent()).thenReturn(node);
        when(nodeData.getJCRProperty()).thenReturn(property);
        when(node.isNodeType(NodeTypes.Resource.NAME)).thenReturn(true);
        Link link = new Link();
        link.setHandle("contact/pepa/image_file");
        link.setNodeDataName("file");
        link.setFileName("fileName");
        link.setExtension("ext");
        link.setRepository("data");
        link.setNodeData(nodeData);
        link.setJCRNode(node);

        URI2RepositoryManager uri2RepositoryManager = new URI2RepositoryManager();
        uri2RepositoryManager.addMapping(new URI2RepositoryMapping("/data", "data", "/blabla"));
        ComponentsTestUtil.setInstance(URI2RepositoryManager.class, uri2RepositoryManager);

        assertEquals("/data/contact/pepa/image_file/file/fileName.ext", URI2RepositoryManager.getInstance().getURI(link));
    }

    public void testGetURIWhenLinkIsNotEditorBinaryLinkAndPrefixHandleIsSet() throws Exception{
        when(property.getParent()).thenReturn(node);
        when(nodeData.getJCRProperty()).thenReturn(property);
        when(node.isNodeType(NodeTypes.Resource.NAME)).thenReturn(false);
        Link link = new Link();
        link.setHandle("contact/pepa/image_file");
        link.setNodeDataName("file");
        link.setFileName("fileName");
        link.setExtension("ext");
        link.setRepository("data");
        link.setJCRNode(node);

        URI2RepositoryManager uri2RepositoryManager = new URI2RepositoryManager();
        uri2RepositoryManager.addMapping(new URI2RepositoryMapping("/data", "data", "/blabla"));
        ComponentsTestUtil.setInstance(URI2RepositoryManager.class, uri2RepositoryManager);

        assertEquals("/contact/pepa/image_file/file/fileName.ext", URI2RepositoryManager.getInstance().getURI(link));
    }
}
