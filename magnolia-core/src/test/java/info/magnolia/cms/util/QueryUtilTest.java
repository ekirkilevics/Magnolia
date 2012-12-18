/**
 * This file Copyright (c) 2012-2012 Magnolia International
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
package info.magnolia.cms.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.test.RepositoryTestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;

import org.apache.jackrabbit.commons.query.QueryObjectModelBuilder;
import org.apache.jackrabbit.commons.query.sql2.SQL2QOMBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link QueryUtil}.
 */
public class QueryUtilTest extends RepositoryTestCase{

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        File inputFile = new File(getClass().getResource("/website.queryTest.xml").getFile());
        
        InputStream inputStream = new FileInputStream(inputFile);

        DataTransporter.importXmlStream(inputStream, "website", "/", "test-stream", false, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, true, false);
    }

    @Test
    public void testSimpleQuery() throws InvalidQueryException, RepositoryException{
        String statement = "select * from [mgnl:page]";
        NodeIterator result = QueryUtil.search("website", statement);
        Collection<Node> collection = NodeUtil.getCollectionFromNodeIterator(result);
        assertEquals(7, collection.size());
    }

    @Test
    public void testResultContainsOnlyPages() throws InvalidQueryException, RepositoryException{
        String statement = "select * from [mgnl:page]";
        NodeIterator result = QueryUtil.search("website", statement);
        while(result.hasNext()){
            assertEquals(NodeTypes.Page.NAME, result.nextNode().getPrimaryNodeType().getName());
        }
    }

    @Test
    public void testSearchOfNonDefaultNodeType() throws InvalidQueryException, RepositoryException{
        String statement = "select * from [mgnl:component]";
        NodeIterator result = QueryUtil.search("website", statement, Query.JCR_SQL2);
        while(result.hasNext()){
            assertEquals(NodeTypes.Component.NAME, result.nextNode().getPrimaryNodeType().getName());
        }
    }

    @Test
    public void testSearchForUUID() throws InvalidQueryException, RepositoryException{
        String statement = "select * from [nt:base] where [jcr:uuid] = 'c1fcda79-1f81-412d-a748-b9ea34ea97f7'";
        NodeIterator result = QueryUtil.search("website", statement);
        Collection<Node> collection = NodeUtil.getCollectionFromNodeIterator(result);
        assertEquals(1, collection.size());
    }

    @Test
    public void testSearchForInheritedUUID() throws InvalidQueryException, RepositoryException{
        String statement = "select * from [mgnl:area] where [jcr:uuid] = '6ecc7d08-9329-44ca-bfc7-ab7b73f6f64d'";
        NodeIterator result = QueryUtil.search("website", statement, Query.JCR_SQL2);
        Collection<Node> collection = NodeUtil.getCollectionFromNodeIterator(result);
        assertEquals(1, collection.size());
    }

    @Test
    public void testSearchForUUIDInChildNode() throws InvalidQueryException, RepositoryException{
        String statement = "select * from [nt:base] where [jcr:uuid] = '6ecc7d08-9329-44ca-bfc7-ab7b73f6f64d'";
        NodeIterator iterator =  QueryUtil.search("website", statement, Query.JCR_SQL2, NodeTypes.Page.NAME);
        assertEquals("/queryTest", iterator.nextNode().getPath());
    }

    @Test
    public void testSearchForSpecificTemplate() throws InvalidQueryException, RepositoryException{
        String statement = "select * from [nt:base] where [mgnl:template] = 'standard-templating-kit:pages/stkArticle'";
        NodeIterator iterator = QueryUtil.search("website", statement, Query.JCR_SQL2, NodeTypes.Page.NAME);
        assertEquals(2, NodeUtil.getCollectionFromNodeIterator(iterator).size());
    }

    @Test 
    public void testEmptyResult() throws InvalidQueryException, RepositoryException{
        String statement = "select * from [nt:base] where [jcr:uuid] = 'non-existing'";
        NodeIterator result = QueryUtil.search("website", statement);
        assertFalse(result.hasNext());
    }

    @Test
    public void testJQOM() throws InvalidQueryException, RepositoryException{
        Session session = MgnlContext.getJCRSession("website");
        QueryObjectModelFactory factory = session.getWorkspace().getQueryManager().getQOMFactory();
        QueryObjectModelBuilder qom = new SQL2QOMBuilder();
        String statement = "select * from [nt:base] where [jcr:uuid] = 'c1fcda79-1f81-412d-a748-b9ea34ea97f7'";
        QueryObjectModel model = qom.createQueryObjectModel(statement, factory, session.getValueFactory());
        NodeIterator result = QueryUtil.search(model, NodeTypes.Page.NAME);
        Collection<Node> collection = NodeUtil.getCollectionFromNodeIterator(result);
        assertEquals(1, collection.size());
    }

    @Test
    public void testQueryWhenSearchingJustForPages() throws InvalidQueryException, RepositoryException{
        String statement = "select * from [nt:base]";
        NodeIterator result = QueryUtil.search("website", statement, Query.JCR_SQL2, NodeTypes.Page.NAME);
        int count = 0;
        while(result.hasNext()){
            Node node = result.nextNode();
            assertEquals(NodeTypes.Page.NAME, node.getPrimaryNodeType().getName());
            count++;
        }
        assertEquals(7, count);
    }

    @Test
    public void testQueryWhenSearchingJustForComponents() throws InvalidQueryException, RepositoryException{
        String statement = "select * from [nt:base]";
        NodeIterator result = QueryUtil.search("website", statement, Query.JCR_SQL2, NodeTypes.Component.NAME);
        int count = 0;
        while(result.hasNext()){
            assertEquals(NodeTypes.Component.NAME, result.nextNode().getPrimaryNodeType().getName());
            count++;
        }
        assertEquals(5, count);
    }
    
    @Test
    public void testSearchForPagesWhoseComponentIsContainingSpecificText() throws InvalidQueryException, RepositoryException{
        String statement = "select * from [mgnl:component] as t where contains(t.*, 'TestText')";
        NodeIterator result = QueryUtil.search("website", statement, Query.JCR_SQL2, NodeTypes.Page.NAME);
        int count = 0;
        while(result.hasNext()){
            assertEquals(NodeTypes.Page.NAME, result.nextNode().getPrimaryNodeType().getName());
            count++;
        }
        assertEquals(2, count);
    }
    
    @Test
    public void testSearchForAreasContainingImage() throws InvalidQueryException, RepositoryException{
        String statement = "select * from [mgnl:component] where image is not null";
        NodeIterator result = QueryUtil.search("website", statement, Query.JCR_SQL2, NodeTypes.Area.NAME);
        while(result.hasNext()){
            Node node = result.nextNode();
            assertEquals(NodeTypes.Area.NAME, node.getPrimaryNodeType().getName());
            assertEquals("/queryTest/subPage2/subPage2/content", node.getPath());
        }
    }
    
    @Test
    public void testConfirmThatFilteredResultIsReallyTheWantedOne() throws InvalidQueryException, RepositoryException{
        String statement = "select * from [nt:base] as t where contains(t.*, 'Poughkeepsie')";
        NodeIterator result = QueryUtil.search("website", statement);
        //Just executes the query
        assertEquals(NodeTypes.Component.NAME, result.nextNode().getPrimaryNodeType().getName());
        //Executes the query and searches for wanted parent type
        result = QueryUtil.search("website", statement, Query.JCR_SQL2, NodeTypes.Page.NAME);
        assertEquals(NodeTypes.Page.NAME, result.nextNode().getPrimaryNodeType().getName());
    }

    @Test
    public void testQueryBuild(){
        assertEquals("select * from [nt:base] as t where ISDESCENDANTNODE([/site1]) AND contains(t.*, 'area') AND contains(t.*, 'component')", QueryUtil.buildQuery("component,area", "/site1"));
    }
}
