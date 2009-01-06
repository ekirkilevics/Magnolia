/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.gui.controlx.search;

import info.magnolia.cms.gui.query.DateSearchQueryParameter;
import info.magnolia.cms.gui.query.SearchQueryExpression;
import info.magnolia.cms.gui.query.SearchQueryOperator;
import info.magnolia.cms.gui.query.StringSearchQueryParameter;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;


/**
 * @author Sameer Charles $Id$ package private helper class
 * Implement this class if you need any further operations in future <b>NOTE</b> : its a very simple in-order binary
 * traversal, order of operation is not preserved
 */
public class QueryBuilder {

    /**
     * search model using which query will be created
     */
    private RepositorySearchListModel model;

    /**
     * Use JCR order by clause to sort the items. Default is false.
     */
    private boolean useJCROrderBy = false;

    /**
     * package private
     * @param model
     */
    public QueryBuilder(RepositorySearchListModel model) {
        this.model = model;
    }

    /**
     * get SQL statement based on SearchQuery
     * @return SQL statement
     */
    public String getSQLStatement() {
        StringBuffer statement = new StringBuffer("select * from ");
        statement.append(this.model.getNodeType());

        String where = buildWhereClause();
        if (where.length() > 0) {
            statement.append(" where ").append(where);
        }
        String orderBy = buildOrderByClause();
        if (orderBy.length() > 0) {
            statement.append(" order by ").append(orderBy);
        }

        return statement.toString();
    }

    /**
     * Build the complete where clause
     */
    protected String buildWhereClause() {
        StringBuffer where = new StringBuffer();
        if (StringUtils.isNotEmpty(this.model.getSearchPath())) {
            where.append(" jcr:path like '");
            where.append(this.model.getSearchPath());
            where.append("%'");
        }
        if(this.model.getQuery() !=null){
            where.append(buildWhereClause(this.model.getQuery().getRootExpression()));
        }
        return where.toString();
    }

    /**
     * Order clause
     */
    protected String buildOrderByClause() {
        StringBuffer orderBy = new StringBuffer();
        if(useJCROrderBy){
            if (StringUtils.isNotEmpty(this.model.getGroupBy()) && StringUtils.isNotEmpty(this.model.getSortBy())) {
                orderBy.append(this.model.getGroupBy());
                orderBy.append(" ");
                orderBy.append(this.model.getGroupByOrder());
                orderBy.append(", ");
                orderBy.append(this.model.getSortBy());
                orderBy.append(" ");
                orderBy.append(this.model.getSortByOrder());
            }
            else if (StringUtils.isNotEmpty(this.model.getGroupBy()) && StringUtils.isEmpty(this.model.getSortBy())) {
                orderBy.append(this.model.getGroupBy());
                orderBy.append(" ");
                orderBy.append(this.model.getGroupByOrder());
            }
            else if (StringUtils.isEmpty(this.model.getGroupBy()) && StringUtils.isNotEmpty(this.model.getSortBy())) {
                orderBy.append(this.model.getSortBy());
                orderBy.append(" ");
                orderBy.append(this.model.getSortByOrder());
            }
        }
        return orderBy.toString();
    }

    /**
     * NOTE : its a very simple in-order binary traversal, order of operation is not preserved
     * @param expression
     */
    protected String buildWhereClause(SearchQueryExpression expression) {
        StringBuffer where = new StringBuffer();
        if (expression != null) {
            where.append(buildWhereClause(expression.getLeft()));
            where.append(" ");
            where.append(toJCRExpression(expression));
            where.append(buildWhereClause(expression.getRight()));
        }
        return where.toString();
    }

    /**
     * Make a jcr expression out of the expression
     * @param expression
     * @return the expression as string
     */
    protected String toJCRExpression(SearchQueryExpression expression) {
        if (expression instanceof SearchQueryOperator) {
            // operator is 1:1 usable in jcr
            return StringUtils.defaultString(((SearchQueryOperator) expression).getOperator());
        }
        else if (expression instanceof StringSearchQueryParameter) {
            return toStringJCRExpression((StringSearchQueryParameter) expression);
        }
        else if (expression instanceof DateSearchQueryParameter) {
            return toDateJCRExpression((DateSearchQueryParameter) expression);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Make a jcr expression out of the expression
     * @param param
     * @return the expression as a string
     */
    protected String toDateJCRExpression(DateSearchQueryParameter param) {
        Date date = param.getValue();
        if (param.getConstraint().equalsIgnoreCase(DateSearchQueryParameter.TODAY)) {
            date = new Date();
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(param.getName());
        if (param.getConstraint().equalsIgnoreCase(DateSearchQueryParameter.BEFORE)) {
            buffer.append(" <= ");
        }
        else if (param.getConstraint().equalsIgnoreCase(DateSearchQueryParameter.AFTER)) {
            buffer.append(" >= ");
        }
        else if (param.getConstraint().equalsIgnoreCase(DateSearchQueryParameter.IS)) {
            buffer.append(" = ");
        }

        buffer.append("TIMESTAMP '");
        buffer.append(DateFormatUtils.format(date, "yyyy-MM-dd"));
        buffer.append("T00:00:00.000");

        TimeZone timezone = TimeZone.getDefault();
        int milis = Math.abs(timezone.getRawOffset());
        if (milis == 0) {
            buffer.append("Z");
        }
        else {
            if (timezone.getRawOffset() > 0) {
                buffer.append("+");
            }
            else {
                buffer.append("-");
            }

            int hours = milis / (1000 * 60 * 60);
            int minutes = (milis - hours * 1000 * 60 * 60) / (1000 * 60);
            DecimalFormat format = new DecimalFormat("00");
            buffer.append(format.format(hours)).append(":").append(format.format(minutes));
        }
        buffer.append("'");
        return buffer.toString();
    }

    /**
     * @param param
     * @return jcr search expression
     */
    protected String toStringJCRExpression(StringSearchQueryParameter param) {
        if (param.getConstraint().equals(StringSearchQueryParameter.CONTAINS)) {
            return "contains(" + param.getName() + ",'" + param.getValue() + "*')";
        }

        else if (param.getConstraint().equals(StringSearchQueryParameter.CONTAINS_NOT)) {
            return "not contains(" + param.getName() + ",'*" + param.getValue() + "*')";
        }

        else if (param.getConstraint().equals(StringSearchQueryParameter.ENDS)) {
            return "contains(" + param.getName() + ",'*" + param.getValue() + "')";
        }

        else if (param.getConstraint().equals(StringSearchQueryParameter.STARTS)) {
            return "contains(" + param.getName() + ",'" + param.getValue() + "*')";
        }

        else if (param.getConstraint().equals(StringSearchQueryParameter.IS)) {
            return param.getName() + " = '" + param.getValue() + "'";
        }

        else if (param.getConstraint().equals(StringSearchQueryParameter.IS_NOT)) {
            return param.getName() + " <> '" + param.getValue() + "'";
        }
        else {
            return param.getName() + " " + param.getConstraint() + " '" + param.getValue() + "'";
        }
    }


    public boolean isUseJCROrderBy() {
        return this.useJCROrderBy;
    }


    public void setUseJCROrderBy(boolean useJCROrderBy) {
        this.useJCROrderBy = useJCROrderBy;
    }

}
