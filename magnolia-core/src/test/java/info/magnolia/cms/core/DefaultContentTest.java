/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.AccessManagerImpl;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.PropertiesImportExport;
import info.magnolia.repository.Provider;
import info.magnolia.test.RepositoryTestCase;
import static org.easymock.EasyMock.*;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Property;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.commons.io.IOUtils;
import org.easymock.IAnswer;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DefaultContentTest extends RepositoryTestCase {

    public interface ExceptionThrowingCallback {
        void call() throws Exception;
    }

    public void testAddMixin() throws IOException, RepositoryException{
        final Content content = getTestContent();
        final String repoName = content.getHierarchyManager().getName();
        final String mixDeleted = "mgnl:deleted";
        final Provider repoProvider = ContentRepository.getRepositoryProvider(repoName);
        final String mgnlMixDeleted = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<nodeTypes" + " xmlns:rep=\"internal\""
        + " xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\"" + " xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\""
        + " xmlns:mgnl=\"http://www.magnolia.info/jcr/mgnl\"" + " xmlns:jcr=\"http://www.jcp.org/jcr/1.0\">" + "<nodeType name=\"" + mixDeleted
        + "\" isMixin=\"true\" hasOrderableChildNodes=\"true\" primaryItemName=\"\">" + "<supertypes>" + "<supertype>nt:base</supertype>"
        + "</supertypes>" + "</nodeType>" + "</nodeTypes>";

        try {
            repoProvider.registerNodeTypes(new ByteArrayInputStream(mgnlMixDeleted.getBytes()));
        } catch (RepositoryException e) {
            // ignore, either it's already registered and test will pass, or type can't be registered and test should fail
        }

        assertTrue(content.getJCRNode().canAddMixin(mixDeleted));
        content.addMixin(mixDeleted);
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


    public void testCreatingAnEmptyNodeData() throws IOException, RepositoryException{
        Content content = getTestContent();
        // this should not fail
        NodeData nodeData = content.createNodeData("nd2");
        assertEquals("", nodeData.getString());
        assertEquals(true, nodeData.isExist());
    }

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

    public void testThatReadingANonExistingNodeDataReturnsAnEmptyNodeData() throws IOException, RepositoryException{
        Content content = getTestContent();
        NodeData nd = content.getNodeData("nirvana");
        assertEquals("nirvana", nd.getName());
        assertEquals("", nd.getString());
        assertEquals(false, nd.getBoolean());
        assertEquals(0, nd.getLong());
        assertEquals("", nd.getAttribute("other"));
    }

    public void testThatReadingANonExistingNodeDataReturnsAnEmptyNodeDataWhichIsUnmutable() throws IOException, RepositoryException{
        Content content = getTestContent();
        NodeData nd = content.getNodeData("nirvana");
        try{
            nd.setValue("value");
        }
        catch(ItemNotFoundException e){
            return;
        }
        fail("should throw an exception");
    }

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
        List<Permission> permissions = am.getPermissionList();
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

    public void testNameFilteringWorksForBothBinaryAndNonBinaryProperties() throws Exception {
        /**
         * TODO: uncomment following lines and fix to run with JCR 2.0
         */
//        String contentProperties = StringUtils.join(Arrays.asList(
//                "/somepage/mypage@type=mgnl:content",
//                "/somepage/mypage/paragraphs@type=mgnl:contentNode",
//                "/somepage/mypage/paragraphs/0@type=mgnl:contentNode",
//                "/somepage/mypage/paragraphs/0@type=mgnl:contentNode",
//
//                // 2 regular props
//                "/somepage/mypage/paragraphs/0/attention=booyah",
//                "/somepage/mypage/paragraphs/0/imaginary=date:2009-10-14T08:59:01.227-04:00",
//
//                // 3 binaries
//                "/somepage/mypage/paragraphs/0/attachment1@type=mgnl:resource",
//                "/somepage/mypage/paragraphs/0/attachment1.fileName=hello",
//                "/somepage/mypage/paragraphs/0/attachment1.extension=gif",
//                "/somepage/mypage/paragraphs/0/attachment1.jcr\\:data=X",
//                "/somepage/mypage/paragraphs/0/attachment1.jcr\\:mimeType=image/gif",
//                "/somepage/mypage/paragraphs/0/attachment1.jcr\\:lastModified=",
//
//                "/somepage/mypage/paragraphs/0/attachment2@type=mgnl:resource",
//                "/somepage/mypage/paragraphs/0/attachment2.fileName=test",
//                "/somepage/mypage/paragraphs/0/attachment2.extension=jpeg",
//                "/somepage/mypage/paragraphs/0/attachment2.jcr\\:data=X",
//                "/somepage/mypage/paragraphs/0/attachment2.jcr\\:mimeType=image/jpeg",
//                "/somepage/mypage/paragraphs/0/attachment2.jcr\\:lastModified=",
//
//                "/somepage/mypage/paragraphs/0/image3@type=mgnl:resource",
//                "/somepage/mypage/paragraphs/0/image3.fileName=third",
//                "/somepage/mypage/paragraphs/0/image3.extension=png",
//                "/somepage/mypage/paragraphs/0/image3.jcr\\:data=X",
//                "/somepage/mypage/paragraphs/0/image3.jcr\\:mimeType=image/png",
//                "/somepage/mypage/paragraphs/0/image3.jcr\\:lastModified=",
//
//                // and more which should not match
//                "/somepage/mypage/paragraphs/0/foo=bar",
//                "/somepage/mypage/paragraphs/0/mybool=boolean:true",
//                "/somepage/mypage/paragraphs/0/rand@type=mgnl:resource",
//                "/somepage/mypage/paragraphs/0/rand.fileName=randdddd",
//                "/somepage/mypage/paragraphs/0/rand.extension=png",
//                "/somepage/mypage/paragraphs/0/rand.jcr\\:data=X",
//                "/somepage/mypage/paragraphs/0/rand.jcr\\:mimeType=image/png",
//                "/somepage/mypage/paragraphs/0/rand.jcr\\:lastModified="
//        ), "\n");
//        final HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
//        new PropertiesImportExport().createContent(hm.getRoot(), IOUtils.toInputStream(contentProperties));
//        hm.save();
//
//        final Content content = hm.getContent("/somepage/mypage/paragraphs/0");
//        final Collection<NodeData> props = content.getNodeDataCollection("att*|ima*");
//        assertEquals(5, props.size());
//
//        // sort by name
//        final TreeSet<NodeData> sorted = new TreeSet<NodeData>(new Comparator<NodeData>() {
//            public int compare(NodeData o1, NodeData o2) {
//                return o1.getName().compareTo(o2.getName());
//            }
//        });
//        sorted.addAll(props);
//        // sanity check - just recheck we still have 5 elements
//        assertEquals(5, sorted.size());
//        final Iterator<NodeData> it = sorted.iterator();
//        final NodeData a = it.next();
//        final NodeData b = it.next();
//        final NodeData c = it.next();
//        final NodeData d = it.next();
//        final NodeData e = it.next();
//        assertEquals("attachment1", a.getName());
//        assertEquals(PropertyType.BINARY, a.getType());
//        assertEquals("attachment2", b.getName());
//        assertEquals(PropertyType.BINARY, b.getType());
//        assertEquals("image3", d.getName());
//        assertEquals(PropertyType.BINARY, d.getType());
//        assertEquals("image3", d.getName());
//        assertEquals(PropertyType.BINARY, d.getType());
//
//        assertEquals("attention", c.getName());
//        assertEquals(PropertyType.STRING, c.getType());
//        assertEquals("booyah", c.getString());
//        assertEquals("imaginary", e.getName());
//        assertEquals(PropertyType.DATE, e.getType());
//        assertEquals(true, e.getDate().before(Calendar.getInstance()));
    }

    public void testStringPropertiesCanBeRetrievedByStreamAndViceVersa() throws Exception {
        /**
         * TODO: uncomment following lines and fix to run with JCR 2.0
         */
//        String contentProperties = StringUtils.join(Arrays.asList(
//                "/hello/foo=bar",
//                // a binary
//                "/hello/bin@type=mgnl:resource",
//                "/hello/bin.fileName=hello",
//                "/hello/bin.extension=gif",
//                "/hello/bin.jcr\\:data=some-data",
//                "/hello/bin.jcr\\:mimeType=image/gif",
//                "/hello/bin.jcr\\:lastModified="), "\n");
//        final HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
//        new PropertiesImportExport().createContent(hm.getRoot(), IOUtils.toInputStream(contentProperties));
//        hm.save();
//
//        final Content content = hm.getContent("/hello");
//        final NodeData st = content.getNodeData("foo");
//        assertEquals(PropertyType.STRING, st.getType());
//        assertEquals("bar", st.getString());
//        assertEquals("bar", IOUtils.toString(st.getStream()));
//
//        final NodeData bin = content.getNodeData("bin");
//        assertEquals(PropertyType.BINARY, bin.getType());
//        assertEquals("some-data", IOUtils.toString(bin.getStream()));
//        assertEquals("some-data", bin.getString());
    }

    public void testModDate() throws IOException, RepositoryException{
        Content content = getTestContent();
        Calendar modDate = content.getMetaData().getModificationDate();
        Calendar creationDate = content.getMetaData().getCreationDate();
        assertNotNull(modDate);
        assertEquals(creationDate, modDate);
        content.setNodeData("test", false);
        content.save();
        modDate = content.getMetaData().getModificationDate();
        assertNotNull(modDate);
        assertNotSame(creationDate, modDate);
    }

    private Value createValue(Object valueObj) throws RepositoryException, UnsupportedRepositoryOperationException {
        ValueFactory valueFactory = MgnlContext.getHierarchyManager("website").getWorkspace().getSession().getValueFactory();
        return NodeDataUtil.createValue(valueObj, valueFactory);
    }

}
