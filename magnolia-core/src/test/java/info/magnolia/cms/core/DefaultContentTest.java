/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.AccessManagerImpl;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.PropertiesImportExport;
import info.magnolia.test.RepositoryTestCase;
import static org.easymock.EasyMock.*;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Property;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.commons.io.IOUtils;
import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DefaultContentTest extends RepositoryTestCase {
    
    
    public interface ExceptionThrowingCallback {
        void call() throws Exception;
    }

    public void testReadingANodeData() throws IOException, RepositoryException{
        Content content = getTestContent();
        NodeData nodeData = content.getNodeData("nd1");
        assertEquals("hello", nodeData.getString());
        assertEquals(true, nodeData.isExist());
        
    }

    public void testThatReadingANonExistingNodeDataDoesNotFail() throws IOException, RepositoryException{
        Content content = getTestContent();
        // this should not fail
        NodeData nodeData = content.getNodeData("nd2");
        assertEquals(false, nodeData.isExist());
    }
    
    public void testSettingAnExistingNodeData() throws IOException, RepositoryException{
        Content content = getTestContent();
        // this should not fail
        Value value = createValue("test");
        NodeData nodeData = content.setNodeData("nd1", value);
        assertEquals("test", nodeData.getString());
    }

    
    public void testSettingANonExistingNodeDataCreatesANewNodeData() throws IOException, RepositoryException{
        Content content = getTestContent();
        Value value = createValue("test");
        // does not exist yet
        NodeData nodeData = content.setNodeData("nd2", value);
        assertEquals("test", nodeData.getString());

    }

    // This would probably make more sense
    /*
     public void testSettingANonExistingNodeDataFails() throws IOException, RepositoryException{
        Content content = getTestContent();
        // this should not fail
        try {
            content.setNodeData("nd2", "test");
        }
        catch (PathNotFoundException e) {
            return;
        }
        fail("Should throw an exception!");
    }
     */

    public void testCreatingAnEmptyNodeData() throws IOException, RepositoryException{
        Content content = getTestContent();
        // this should not fail
        NodeData nodeData = content.createNodeData("nd2");
        assertEquals("", nodeData.getString());
        assertEquals(true, nodeData.isExist());
    }

    // FIXME? in older versions we created an empty string property
    
//    public void testCreatingAnEmptyNodeDataIgnoresTheType() throws IOException, RepositoryException{
//        Content content = getTestContent();
//        NodeData nodeData = content.createNodeData("nd2", PropertyType.BOOLEAN);
//        // fact is that the type is ignored
//        assertEquals(PropertyType.STRING, nodeData.getType());
//    }

    public void testCreatingAnEmptyNodeDataSetsADefaultValueIfPossible() throws IOException, RepositoryException {
        Content content = getTestContent();
        NodeData nodeData = content.createNodeData("nd2", PropertyType.BOOLEAN);
        assertEquals(true, nodeData.isExist());
        assertEquals(PropertyType.BOOLEAN, nodeData.getType());
    }

    public void testCreatingAndSettingANodeData() throws IOException, RepositoryException{
        Content content = getTestContent();
        // this should not fail
        NodeData nodeData = content.createNodeData("nd2", "test");
        assertEquals("test", nodeData.getString());
    }

    public void testCreatingAndSettingABooleanNodeData() throws IOException, RepositoryException{
        Content content = getTestContent();
        // this actually creates a string property having an empty string value 
        NodeData nodeData = content.createNodeData("nd2");
        // now setting a boolean value
        nodeData.setValue(true);
        assertEquals(true, nodeData.getBoolean());
        
        nodeData = content.createNodeData("nd3", true);
        assertEquals(true, nodeData.getBoolean());
    }

    public void testCreatingAnExistingNodeDataDoesNotFail() throws IOException, RepositoryException{
        Content content = getTestContent();
        NodeData nodeData = content.createNodeData("nd1", "other");
        assertEquals("other", nodeData.getString());
    }
    
    public void testCreatingAndReadingABinaryNodeData() throws IOException, RepositoryException{
        Content content = getTestContent();
        String binaryContent = "the content";
        NodeData nodeData = content.createNodeData("nd2", PropertyType.BINARY);
        nodeData.setValue(IOUtils.toInputStream(binaryContent));
//        nodeData.setAttribute(FileProperties.PROPERTY_FILENAME, "filename");
        nodeData.setAttribute(FileProperties.PROPERTY_CONTENTTYPE, "text/plain");
        nodeData.setAttribute(FileProperties.PROPERTY_LASTMODIFIED, Calendar.getInstance());
        
        
        content.save();
        nodeData = content.getNodeData("nd2");
        assertEquals(binaryContent, IOUtils.toString(nodeData.getStream()));
        //assertEquals("filename", nodeData.getAttribute(FileProperties.PROPERTY_FILENAME));
    }
    
    // This would probably make more sense
    /*
    public void testCreatingAnExistingNodeDataFails() throws IOException, RepositoryException{
        Content content = getTestContent();
        // this should not fail
        try {
            NodeData nodeData = content.createNodeData("nd1", "test");
        }
        catch (PathNotFoundException e) {
            return;
        }
        fail("Should throw an exception!");
    }
    */

    
    private Content getTestContent() throws IOException, RepositoryException {
        String contentProperties = 
            "/mycontent.@type=mgnl:content\n" +
            "/mycontent.nd1=hello";
        
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
        new PropertiesImportExport().createContent(hm.getRoot(), IOUtils.toInputStream(contentProperties));
        hm.save();
        Content content = hm.getContent("/mycontent");
        return content;
    }

    public void testPermissionCheckedOnDeleteNodeData() throws Exception {
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
        // create the content while we have full permissions
        final Content node = hm.createContent("/", "foo", ItemType.CONTENTNODE.getSystemName());
        node.createNodeData("bar").setValue("test");

        AccessManager am = new AccessManagerImpl();
        setPermission(am, "/*", Permission.READ);
        ((DefaultHierarchyManager) hm).setAccessManager(am);

        // test that we can read
        assertTrue(node.hasNodeData("bar"));
        assertEquals("test", node.getNodeData("bar").getString());
        
        mustFailWithAccessDeniedException(new ExceptionThrowingCallback() {
            public void call() throws Exception {
                node.setNodeData("bar", "other");
            }

        }, "should not be allowed to set a value");

        mustFailWithAccessDeniedException(new ExceptionThrowingCallback() {
            public void call() throws Exception {
                node.delete("bar");
            }

        }, "should not be allowed to delete a nodedata");

        mustFailWithAccessDeniedException(new ExceptionThrowingCallback() {
            public void call() throws Exception {
                node.deleteNodeData("bar");
            }

        }, "should not be allowed to delete a nodedata");
    }

    private void mustFailWithAccessDeniedException(ExceptionThrowingCallback callback, String msg) throws Exception {
        try{
            callback.call();
        }
        catch (AccessDeniedException e) {
            // this expected
            return;
        }
        fail(msg);

    }

    private void setPermission(AccessManager am, String path, long permissionValue) {
        ArrayList<Permission> permissions = (ArrayList<Permission>) am.getPermissionList();
        if(permissions == null){
            permissions = new ArrayList<Permission>();
        }
        
        PermissionImpl permission = new PermissionImpl();
        permission.setPattern(new SimpleUrlPattern(path));
        permission.setPermissions(permissionValue);
        permissions.add(permission);
        am.setPermissionList(permissions);
    }

    public void testIsNodeTypeForNodeChecksPrimaryType() throws RepositoryException {
        final Node node = createMock(Node.class);
        final Property nodeTypeProp = createStrictMock(Property.class);
        expect(node.getProperty(ItemType.JCR_PRIMARY_TYPE)).andReturn(nodeTypeProp).times(2);
        expect(node.isNodeType((String)anyObject())).andAnswer(new IAnswer<Boolean>(){
            public Boolean answer() throws Throwable {
                return getCurrentArguments()[0].equals("foo");
            }
        }).times(2);
        expect(nodeTypeProp.getString()).andReturn("foo").times(2);
        replay(node, nodeTypeProp);

        final DefaultContent c = new DefaultContent();
        c.setNode(node);
        assertTrue(c.isNodeType(node, "foo"));
        assertFalse(c.isNodeType(node, "bar"));
        verify(node, nodeTypeProp);
    }

    public void testIsNodeTypeForNodeCheckFrozenTypeIfWereNotLookingForFrozenNodes() throws RepositoryException {
        doTestIsNodeTypeForNodeCheckFrozenTypeIfWereNotLookingForFrozenNodes(true, "foo", "foo");
        doTestIsNodeTypeForNodeCheckFrozenTypeIfWereNotLookingForFrozenNodes(false, "bar", "foo");
    }

    private void doTestIsNodeTypeForNodeCheckFrozenTypeIfWereNotLookingForFrozenNodes(boolean expectedResult, String requiredType, String returnedType) throws RepositoryException {
        final Node node = createStrictMock(Node.class);
        final Property nodeTypeProp = createStrictMock(Property.class);
        final Property nodeFrozenTypeProp = createStrictMock(Property.class);

        expect(node.getProperty(ItemType.JCR_PRIMARY_TYPE)).andReturn(nodeTypeProp);
        expect(nodeTypeProp.getString()).andReturn(ItemType.NT_FROZENNODE);
        expect(node.getProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE)).andReturn(nodeFrozenTypeProp);
        expect(nodeFrozenTypeProp.getString()).andReturn(returnedType);

        replay(node, nodeTypeProp, nodeFrozenTypeProp);
        final DefaultContent c = new DefaultContent();
        c.setNode(node);
        assertEquals(expectedResult, c.isNodeType(node, requiredType));

        verify(node, nodeTypeProp, nodeFrozenTypeProp);
    }

    public void testIsNodeTypeForNodeDoesNotCheckFrozenTypeIfTheRequestedTypeIsFrozenType()throws RepositoryException {
        final Node node = createStrictMock(Node.class);
        final Property nodeTypeProp = createStrictMock(Property.class);
        expect(node.getProperty(ItemType.JCR_PRIMARY_TYPE)).andReturn(nodeTypeProp);
        expect(nodeTypeProp.getString()).andReturn(ItemType.NT_FROZENNODE);
        expect(node.isNodeType(ItemType.NT_FROZENNODE)).andReturn(true);

        replay(node, nodeTypeProp);
        final DefaultContent c = new DefaultContent();
        c.setNode(node);
        assertTrue(c.isNodeType(node, ItemType.NT_FROZENNODE));
        verify(node, nodeTypeProp);
    }

    private Value createValue(Object valueObj) throws RepositoryException, UnsupportedRepositoryOperationException {
        ValueFactory valueFactory = MgnlContext.getHierarchyManager("website").getWorkspace().getSession().getValueFactory();
        return NodeDataUtil.createValue(valueObj, valueFactory);
    }

}
