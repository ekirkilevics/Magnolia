/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.LoginException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.QueryObjectModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

/**
 * Util to execute queries as simple as possible.
 * @version $Id$
 *
 */
public class QueryUtil {

    private static Logger log = LoggerFactory.getLogger(QueryUtil.class);

    /**
     * Executes a query.
     * @deprecated Since 4.5.4 use search methods.
     */
    public static Collection<Content> query(String repository, String statement) {
        return query(repository, statement, "sql");
    }

    /**
     * Executes a query.
     * @deprecated Since 4.5.4 use search methods.
     */
    public static Collection<Content> query(String repository, String statement, String language) {
        return query(repository, statement, language, ItemType.NT_BASE);
    }

    /**
    * @deprecated Since 4.5.4 use search methods.
    */
    public static Collection<Content> exceptionThrowingQuery(String repository, String statement, String language, String returnItemType) throws RepositoryException {
        return exceptionThrowingQuery(repository, statement, language, returnItemType, Long.MAX_VALUE);
    }

    /**
     * Executes a query, throwing any exceptions that arise as a result.
     * @deprecated Since 4.5.4 use search methods.
     */
    public static Collection<Content> exceptionThrowingQuery(String repository, String statement, String language, String returnItemType,
        long maxResultSize) throws RepositoryException {
        Collection<Content> results = new ArrayList<Content>();
        if(maxResultSize <= 0){
            maxResultSize = Long.MAX_VALUE;
        }
        NodeIterator iterator = search(repository, statement, language, returnItemType);

        long count = 1;
        while(iterator.hasNext() && count <= maxResultSize){
            results.add(ContentUtil.getContent(repository, iterator.nextNode().getPath()));
            count++;
        }
        return results;
    }

    /**
     * @deprecated Since 4.5.4 use search methods.
     */
    public static Collection<Content> query(String repository, String statement, String language, String returnItemType) {
        return query(repository, statement, language, returnItemType, Long.MAX_VALUE);
    }

    /**
     * Executes a query - if an exception is thrown, it is logged and an empty collection is
     * returned.
     * @deprecated Since 4.5.4 use search methods.
     */
    @SuppressWarnings("unchecked")
    // Collections.EMPTY_LIST;
    public static Collection<Content> query(String repository, String statement, String language, String returnItemType, long maxResultSize) {
        try {
            return exceptionThrowingQuery(repository, statement, language, returnItemType, maxResultSize);
        }
        catch (Exception e) {
            log.error("can't execute query [" + statement + "], will return empty collection", e);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @param month 1-12 (as opposed to java.util.Calendar 0-11 notation)
     * @deprecated
     */
    public static String createDateExpression(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day);
        return createDateExpression(cal);
    }

    /**
     * Expression representing a date.
     * @deprecated since 4.5.4 use info.magnolia.cms.util.DateUtil.createDateExpression(calendar)
     */
    public static String createDateExpression(Calendar calendar) {
        return DateUtil.createDateExpression(calendar);
    }

    /**
     * @param month 1-12 (as opposed to java.util.Calendar 0-11 notation)
     * @deprecated
     */
    public static String createDateTimeExpression(int year, int month, int day, int hour, int minutes, int seconds) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hour, minutes, seconds);
        return createDateTimeExpression(cal);
    }

    /**
     * Expression representing a date and time.
     * @deprecated since 4.5.4 use info.magnolia.cms.util.DateUtil.createDateTimeExpression(calendar)
     */
    public static String createDateTimeExpression(Calendar calendar) {
        return DateUtil.createDateTimeExpression(calendar);
    }

    /**
     * @param month 1-12 (as opposed to java.util.Calendar 0-11 notation)
     * @deprecated
     */
    public static String createDateTimeExpressionIgnoreTimeZone(int year, int month, int day, int hour, int minutes, int seconds) {
        Calendar cal = Calendar.getInstance(DateUtils.UTC_TIME_ZONE);
        cal.set(year, month - 1, day, hour, minutes, seconds);
        return createDateTimeExpression(cal);
    }

    /**
     * Do not consider the timezone.
     * @deprecated since 4.5.4 use info.magnolia.cms.util.DateUtil.createDateTimeExpressionIgnoreTimeZone(calendar)
     */
    public static String createDateTimeExpressionIgnoreTimeZone(Calendar calendar) {
        return DateUtil.createDateTimeExpressionIgnoreTimeZone(calendar);
    }
    
    /**
     * Executes the query based on QOM and then pops-up in the node hierarchy until returnItemType is found. If the result
     * is not returnItemType or none of its parents are then next node in result is checked.
     * Duplicate nodes are removed from result.
     * For date/time expressions use <code>DateUtil.create*Expression()</code> methods.
     * @param model
     * @param returnItemType
     * @return Result as NodeIterator
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public static NodeIterator search(QueryObjectModel model, String returnItemType) throws InvalidQueryException, RepositoryException{
        return NodeUtil.filterDuplicates(NodeUtil.filterParentNodeType(model.execute().getNodes(), returnItemType));
    }

    /**
     * Executes the query with given language.Unlike in the old API item type has to be specified in query itself.
     * <code>SELECT * FROM [mgnl:page]</code> example for selecting just pages in JCR SQL2 language.
     * Duplicate nodes are removed from result.
     * For date/time expressions use <code>DateUtil.create*Expression()</code> methods.
     * @param workspace
     * @param statement
     * @param language
     * @return Result as NodeIterator
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public static NodeIterator search(String workspace, String statement, String language) throws InvalidQueryException, RepositoryException{
        Session session = MgnlContext.getJCRSession(workspace);
        QueryManager manager = session.getWorkspace().getQueryManager();
        Query query = manager.createQuery(statement, language);

        return NodeUtil.filterDuplicates(query.execute().getNodes());
    }

    /**
     * Executes the query using JCR SQL2 language. Unlike in the old API item type has to be specified in query itself.
     * <code>SELECT * FROM [mgnl:page]</code> example for selecting just pages.
     * For executing old query use info.magnolia.cms.util.QueryUtil.search(String workspace, String statement, String language)
     * where you specify <code>Query.SQL</code> as the language.
     * For date/time expressions use <code>DateUtil.create*Expression()</code> methods.
     * @param workspace
     * @param statement
     * @return Result as NodeIterator
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public static NodeIterator search(String workspace, String statement) throws InvalidQueryException, RepositoryException{
        return search(workspace, statement, javax.jcr.query.Query.JCR_SQL2);
    }

    /**
     * Searches for statement and then pops-up in the node hierarchy until returnItemType is found. If the result
     * is not returnItemType or none of its parents are then next node in result is checked. Duplicate nodes are
     * removed from result.
     * For date/time expressions use <code>DateUtil.create*Expression()</code> methods.
     * @param workspace
     * @param statement
     * @param language
     * @param returnItemType
     * @return query result as collection of nodes
     * @throws LoginException
     * @throws RepositoryException
     */
    public static NodeIterator search(String workspace, String statement, String language, String returnItemType) throws LoginException, RepositoryException{
        NodeIterator resultIterator = search(workspace, statement, language);

        return NodeUtil.filterDuplicates(NodeUtil.filterParentNodeType(resultIterator, returnItemType));
    }
}