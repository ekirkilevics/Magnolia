/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.test.mock;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.AccessDeniedException;

import java.util.GregorianCalendar;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class MockObjectTest extends TestCase {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MockObjectTest.class);

    public void testRootNodeOfHierarchyManger(){
        MockHierarchyManager hm = new MockHierarchyManager();
        Content root = hm.getRoot();
        assertEquals(root.getName(), "jcr:root");
    }

    public void testCreatingANode() throws AccessDeniedException, PathNotFoundException, RepositoryException{
        MockHierarchyManager hm = new MockHierarchyManager();
        hm.createContent("/", "test1", ItemType.CONTENTNODE.getSystemName());
        assertEquals(hm.getContent("/test1").getName(), "test1");
    }

    public void testCreatingASubNode() throws AccessDeniedException, PathNotFoundException, RepositoryException{
        MockHierarchyManager hm = new MockHierarchyManager();
        hm.createContent("/test/sub", "test1", ItemType.CONTENTNODE.getSystemName());
        assertEquals(hm.getContent("/test/sub/test1").getName(), "test1");
    }

    public void testGetANodeAddedToASubNode() throws AccessDeniedException, PathNotFoundException, RepositoryException{
        MockHierarchyManager hm = new MockHierarchyManager();
        Content parent = hm.createContent("/test/sub", "test1", ItemType.CONTENTNODE.getSystemName());
        parent.createContent("other", new ItemType("mgnl:test"));
        Content node= hm.getContent("/test/sub/test1/other");

        assertEquals(node.getName(), "other");
        assertEquals(node.getItemType().getSystemName(), "mgnl:test");
        assertEquals(node.getHandle(), "/test/sub/test1/other");
        assertEquals(((MockContent)node).getHierarchyManager(), hm);
    }

    public void testSetABooleanValueOnANodeData(){
        MockNodeData nd = new MockNodeData("test", new Boolean(true));
        assertEquals(true, nd.getBoolean());
    }

    public void testSetAnObjectAndResolvePropertyType(){
        MockNodeData ndBoolean = new MockNodeData("test", new Boolean(true));
        MockNodeData ndLong = new MockNodeData("test", new Long(5));
        MockNodeData ndDate = new MockNodeData("test", new GregorianCalendar(2007,2,14));

        assertEquals(true, ndBoolean.getBoolean());
        assertEquals(PropertyType.BOOLEAN,ndBoolean.getType());

        assertEquals(5, ndLong.getLong());
        assertEquals(PropertyType.LONG,ndLong.getType());

        assertEquals(new GregorianCalendar(2007,2,14), ndDate.getDate());
        assertEquals(PropertyType.DATE,ndDate.getType());

    }


}
