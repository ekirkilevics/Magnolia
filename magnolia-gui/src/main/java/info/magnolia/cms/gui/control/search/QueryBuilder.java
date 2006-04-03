/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.control.search;

import info.magnolia.cms.gui.query.*;
import org.apache.commons.lang.StringUtils;

/**
 * @author Sameer Charles
 * $Id$
 *
 * package private helper class
 * at the moment only simple AND operations are supported
 * Implement this class if you need any further operations in future
 */
class QueryBuilder {

    /**
     * statement
     * */
    private StringBuffer statement = new StringBuffer();

    /**
     * search model using which query will be created
     * */
    private RepositorySearchListModel model;

    /**
     * package private
     * @param model
     * */
    protected QueryBuilder(RepositorySearchListModel model) {
        this.model = model;
        this.build(this.model.getQuery().getRootExpression());
        this.addSelect();
        this.addOrder();
    }

    /**
     * get SQL statement based on SearchQuery
     * @return SQL statement
     * */
    protected String getSQLStatement() {
        return this.statement.toString();
    }

    /**
     * prepend select statement
     * */
    private void addSelect() {
        StringBuffer select = new StringBuffer("select * from ");
        select.append(this.model.getSelectNodeType());
        if (StringUtils.isNotEmpty(this.model.getSearchPath())) {
            this.statement.append(" jcr:path like '");
            this.statement.append(this.model.getSearchPath());
            this.statement.append("%'");
        }
        if (this.statement.length() > 0) {
            select.append(" where");
        }
        this.statement.insert(0,select);
    }

    /**
     *
     * add orgering
     * */
    private void addOrder() {
        if (StringUtils.isNotEmpty(this.model.getGroupBy()) && StringUtils.isNotEmpty(this.model.getSortBy())) {
            statement.append(" order by ");
            statement.append(this.model.getGroupBy());
            statement.append(" ");
            statement.append(this.model.getGroupByOrder());
            statement.append(", ");
            statement.append(this.model.getSortBy());
            statement.append(" ");
            statement.append(this.model.getSortByOrder());
        } else if (StringUtils.isNotEmpty(this.model.getGroupBy()) && StringUtils.isEmpty(this.model.getSortBy())) {
            statement.append(" order by ");
            statement.append(this.model.getGroupBy());
            statement.append(" ");
            statement.append(this.model.getGroupByOrder());
        } else if (StringUtils.isEmpty(this.model.getGroupBy()) && StringUtils.isNotEmpty(this.model.getSortBy())) {
            statement.append(" order by ");
            statement.append(this.model.getSortBy());
            statement.append(" ");
            statement.append(this.model.getSortByOrder());
        }
    }

    /**
     * build statement, assuming SearchQueryExpression is of type SearchQueryOperator
     * NOTE : it treats all operators as AND, a very simple query builder
     * @param expression
     * */
    private void build(SearchQueryExpression expression) {
        if (expression == null)
            return;
        this.build(expression.getLeft());
        this.statement.append(" ");
        this.statement.append(expression.toString());
        this.build(expression.getRight());
    }

}
