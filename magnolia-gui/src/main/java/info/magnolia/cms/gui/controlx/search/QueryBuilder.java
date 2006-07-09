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
package info.magnolia.cms.gui.controlx.search;

import info.magnolia.cms.gui.query.DateSearchQueryParameter;
import info.magnolia.cms.gui.query.SearchQueryExpression;
import info.magnolia.cms.gui.query.SearchQueryOperator;
import info.magnolia.cms.gui.query.StringSearchQueryParameter;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

/**
 * @author Sameer Charles
 * $Id$
 *
 * package private helper class
 * Implement this class if you need any further operations in future
 * <b>NOTE</b> : its a very simple in-order binary traversal, order of operation is not preserved
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
        select.append(this.model.getNodeType());
        if (StringUtils.isNotEmpty(this.model.getSearchPath())) {
            this.statement.append(" jcr:path like '");
            this.statement.append(this.model.getSearchPath());
            this.statement.append("%'");
        }
        if (this.statement.length() > 0) {
            select.append(" where");
        }
        this.statement.insert(0,select.toString());
    }

    /**
     *
     * add orgering
     * */
    /*
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
    */

    /**
     * NOTE : its a very simple in-order binary traversal, order of operation is not preserved
     * @param expression
     * */
    private void build(SearchQueryExpression expression) {
        if (expression == null) {
            return;
        }
        this.build(expression.getLeft());
        this.statement.append(" ");
        this.statement.append(toJCRExpression(expression));
        this.build(expression.getRight());
    }
    
    /**
     * Make a jcr expression out of the expression
     * @param expression
     * @return the expression as string
     */
    private String toJCRExpression(SearchQueryExpression expression){
        if(expression instanceof SearchQueryOperator ){
            // operator is 1:1 usable in jcr
            return StringUtils.defaultString(((SearchQueryOperator)expression).getOperator());
        }
        else if(expression instanceof StringSearchQueryParameter){
            return toStringJCRExpression((StringSearchQueryParameter)expression);
        }
        else if(expression instanceof DateSearchQueryParameter){
            return getDateJCRExpression( (DateSearchQueryParameter)expression);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Make a jcr expression out of the expression
     * @param param
     * @return the expression as a string
     */
    private String getDateJCRExpression(DateSearchQueryParameter param) {
        Date date = param.getValue();
        if(param.getConstraint().equalsIgnoreCase(DateSearchQueryParameter.TODAY)){
            date = new Date();
        }
        
        StringBuffer buffer = new StringBuffer();
        buffer.append(param.getName());
        if (param.getConstraint().equalsIgnoreCase(DateSearchQueryParameter.BEFORE)) {
            buffer.append(" <= TIMESTAMP '");
        } else if (param.getConstraint().equalsIgnoreCase(DateSearchQueryParameter.AFTER)) {
            buffer.append(" >= TIMESTAMP '");
        } else if (param.getConstraint().equalsIgnoreCase(DateSearchQueryParameter.IS)) {
            buffer.append(" = TIMESTAMP '");
        }
        buffer.append(DateFormatUtils.format(date,"yyyy-MM-dd"));
        buffer.append("T00:00:00.000Z'");
        return buffer.toString();
    }

    /**
     * @param param
     * @return
     */
    private String toStringJCRExpression(StringSearchQueryParameter param) {
        if (param.getConstraint().equals(StringSearchQueryParameter.CONTAINS)) {
            return "contains(" + param.getName() +",'*" + param.getValue() + "*')";
        }
        
        else if (param.getConstraint().equals(StringSearchQueryParameter.CONTAINS_NOT)) {
            return "not contains(" + param.getName() +",'*" + param.getValue() + "*')";
        }

        else if (param.getConstraint().equals(StringSearchQueryParameter.ENDS)) {
            return "contains(" + param.getName() +",'*" + param.getValue() + "')";
        }
        
        else if (param.getConstraint().equals(StringSearchQueryParameter.STARTS)) {
            return "contains(" + param.getName() +",'" + param.getValue() + "*')";
        }
        
        else if (param.getConstraint().equals(StringSearchQueryParameter.IS)) {
            return param.getName() + " = '" + param.getValue() + "'";
        }
        
        else if (param.getConstraint().equals(StringSearchQueryParameter.IS_NOT)) {
            return param.getName() + " <> '" +param.getValue() + "'";
        }
        return StringUtils.EMPTY;
    }

}
