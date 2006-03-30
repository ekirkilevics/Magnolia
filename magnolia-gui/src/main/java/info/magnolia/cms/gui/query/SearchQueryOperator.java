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
package info.magnolia.cms.gui.query;

/**
 * @author Sameer Charles
 * $Id :$
 */
public class SearchQueryOperator implements SearchQueryExpression {

    /**
     * Operator value AND
     * */
    public static final String AND = "and";

    /**
     * Operator value OR
     * */
    public static final String OR = "or";

    /**
     * Operator value NOT
     * */
    public static final String NOT = "not";

    /**
     * operator
     * */
    private String operator;

    /**
     * previous pointer
     * */
    private SearchQueryOperator previous;

    /**
     * next pointer
     * */
    private SearchQueryOperator next;

    /**
     * @param operator
     * @param previous pointing to an Expression on left
     * @param next pointing to an Expression on right
     * */
    public SearchQueryOperator(String operator, SearchQueryOperator previous, SearchQueryOperator next) {
        this.operator = operator;
        this.previous = previous;
        this.next = next;
    }

    /**
     * get previous expression
     * @return object on left
     * */
    public SearchQueryOperator getPrevious() {
        return this.previous;
    }

    /**
     * set previous expression
     * @param previous object on left
     * */
    public void setPrevious(SearchQueryOperator previous) {
        this.previous = previous;
    }

    /**
     * get next expression
     * @return object on right
     * */
    public SearchQueryOperator getNext() {
        return next;
    }

    /**
     * set next expression
     * @param next object on left
     * */
    public void setRight(SearchQueryOperator next) {
        this.next = next;
    }

    /**
     * get operator name
     * @return String
     * */
    public String getOperator() {
        return operator;
    }

    /**
     * set operator name
     * @param operator
     * */
    public void setOperator(String operator) {
        this.operator = operator;
    }
}
