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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

/**
 * Util to execute queries as simple as possible.
 * @author Philipp Bracher
 * @version $Id$
 *
 */
public class QueryUtil {
    private static Logger log = LoggerFactory.getLogger(QueryUtil.class);

    /**
     * Executes a query.
     * @param repository
     * @param statement
     * @return
     */
    public static Collection<Content> query(String repository, String statement){
        return query(repository,statement, Query.SQL);
    }

    /**
     * Executes a query.
     * @param repository
     * @param statement
     * @param language
     * @return
     */
    public static Collection<Content> query(String repository, String statement, String language){
        return query(repository,statement,language, ItemType.NT_BASE);
    }

    /**
     * Executes a query, throwing any exceptions that arise as a result.
     */
    public static Collection<Content> exceptionThrowingQuery(String repository, String statement, String language, String returnItemType) throws RepositoryException {
        QueryManager qm = MgnlContext.getQueryManager(repository);
        Query query= qm.createQuery(statement, language);
        QueryResult result = query.execute();
        return result.getContent(returnItemType);
    }

    /**
     * Executes a query - if an exception is thrown, it is logged and an empty collection is returned.
     * @param repository
     * @param statement
     * @param language
     * @param returnItemType
     * @return
     */
    @SuppressWarnings("unchecked") //Collections.EMPTY_LIST;
    public static Collection<Content> query(String repository, String statement, String language, String returnItemType) {
        try {
            return exceptionThrowingQuery(repository, statement, language, returnItemType);
        } catch (Exception e) {
            log.error("can't execute query [" + statement  + "], will return empty collection", e);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @param month 1-12 (as opposed to java.util.Calendar 0-11 notation)
     */
    public static String createDateExpression(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day);
        return createDateExpression(cal);
    }

    /**
     * Expression representing a date
     */
    public static String createDateExpression(Calendar calendar) {
        return "DATE '" + DateFormatUtils.format(calendar.getTimeInMillis(), "yyyy-MM-dd", calendar.getTimeZone()) + "'";
    }

    /**
     * @param month 1-12 (as opposed to java.util.Calendar 0-11 notation)
     */
    public static String createDateTimeExpression(int year, int month, int day, int hour, int minutes, int seconds) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hour, minutes, seconds);
        return createDateTimeExpression(cal);
    }

    /**
     * Expression representing a date and time
     */
    public static String createDateTimeExpression(Calendar calendar) {
        calendar.set(Calendar.MILLISECOND, 0);
        StringBuffer str = new StringBuffer("TIMESTAMP '");
        str.append(DateFormatUtils.format(calendar.getTime(), "yyyy-MM-dd'T'HH:mm:ss.SSSZ", calendar.getTimeZone()));
        str.insert(str.length()-2, ":");
        str.append("'");
        return str.toString();
    }

    /**
     * @param month 1-12 (as opposed to java.util.Calendar 0-11 notation)
     */
    public static String createDateTimeExpressionIgnoreTimeZone(int year, int month, int day, int hour, int minutes, int seconds) {
        Calendar cal = Calendar.getInstance(DateUtils.UTC_TIME_ZONE);
        cal.set(year, month - 1, day, hour, minutes, seconds);
        return createDateTimeExpression(cal);
    }

    /**
     * Do not consider the timezone.
     */
    public static String createDateTimeExpressionIgnoreTimeZone(Calendar calendar) {
        Calendar utc = Calendar.getInstance(DateUtils.UTC_TIME_ZONE);
        utc.setTimeInMillis(calendar.getTimeInMillis() + calendar.getTimeZone().getRawOffset());
        return createDateTimeExpression(utc);
    }
}
