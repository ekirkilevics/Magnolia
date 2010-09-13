/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.cms.core.version;

import static info.magnolia.cms.beans.runtime.FileProperties.PROPERTY_CONTENTTYPE;
import static info.magnolia.cms.beans.runtime.FileProperties.PROPERTY_LASTMODIFIED;
import static info.magnolia.cms.core.ItemType.CONTENTNODE;
import static info.magnolia.cms.core.ItemType.NT_RESOURCE;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;

import java.io.IOException;
import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

/**
 * 
 * @author vsteller
 *
 */
public class ContentVersionTest extends RepositoryTestCase {

    public void testRetrievingBinariesFromContentVersion() throws RepositoryException, IOException {
        final HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
        
        // create content with binary
        Content node = hm.createContent("/", "node", CONTENTNODE.getSystemName());
        final NodeData binaryNodeData = node.setNodeData("binary", getClass().getResourceAsStream("/testresource.txt"));
        binaryNodeData.setAttribute(PROPERTY_CONTENTTYPE, "text/plain");
        binaryNodeData.setAttribute(PROPERTY_LASTMODIFIED, Calendar.getInstance());
        hm.save();
        
        // verify that the binary is really there
        assertNotNull(binaryNodeData.getValue());
        
        // add version
        @SuppressWarnings("unused")
        final Version version = node.addVersion();
        
        // get binary from version
        final ContentVersion versionedContent = node.getVersionedContent("1.0");
        final NodeData versionedBinaryNodeData = versionedContent.getNodeData("binary");
        
        // due to http://jira.magnolia-cms.com/browse/MAGNOLIA-3288 the following call to get value returns null. 
        assertNotNull("Expected a non-null binary value to be retrieved from ContentVersion", versionedBinaryNodeData.getValue());
    }
    
    public void testRestoringBinariesFromContentVersion() throws RepositoryException, IOException {
        final HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
        
        // create content with binary
        Content node = hm.createContent("/", "node", CONTENTNODE.getSystemName());
        final NodeData binaryNodeData = node.setNodeData("binary", getClass().getResourceAsStream("/testresource.txt"));
        binaryNodeData.setAttribute(PROPERTY_CONTENTTYPE, "text/plain");
        binaryNodeData.setAttribute(PROPERTY_LASTMODIFIED, Calendar.getInstance());
        hm.save();
        
        // verify that the binary is really there
        assertNotNull(binaryNodeData.getValue());
        
        // add version
        final Version version = node.addVersion();
        
        // delete binary
        binaryNodeData.delete();
        hm.save();
        
        assertFalse("binary shouldn't exist anymore", node.hasNodeData("binary"));
        
        // restore node
        node.restore(version, true);
       
        // get binary from restored node
        final NodeData restoredBinaryNodeData = node.getNodeData("binary");
         
        assertNotNull("Expected a non-null binary value to be retrieved from the restored version", restoredBinaryNodeData.getValue());
    }

    public void testRetrievingBinariesFromContentVersionsChildNode() throws RepositoryException, IOException {
        final HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
        
        // create content with binary
        Content node = hm.createContent("/", "node", CONTENTNODE.getSystemName());
        Content child = node.createContent("child", CONTENTNODE.getSystemName());
        final NodeData binaryNodeData = child.setNodeData("binary", getClass().getResourceAsStream("/testresource.txt"));
        binaryNodeData.setAttribute(PROPERTY_CONTENTTYPE, "text/plain");
        binaryNodeData.setAttribute(PROPERTY_LASTMODIFIED, Calendar.getInstance());
        hm.save();
        
        // verify that the binary is really there
        assertNotNull(binaryNodeData.getValue());
        
        final Rule rule = new Rule();
        rule.addAllowType(CONTENTNODE.getSystemName());
        rule.addAllowType(NT_RESOURCE);
        
        // add version
        @SuppressWarnings("unused")
        final Version version = node.addVersion(rule);
        
        // get binary from version
        final ContentVersion versionedContent = node.getVersionedContent("1.0");
        final Content versionedChild = versionedContent.getContent("child");
        final NodeData versionedBinaryNodeData = versionedChild.getNodeData("binary");
        
        // due to http://jira.magnolia-cms.com/browse/MAGNOLIA-3288 the following call to get value returns null. 
        assertNotNull("Expected a non-null binary value to be retrieved from ContentVersion", versionedBinaryNodeData.getValue());
    }
    
}
