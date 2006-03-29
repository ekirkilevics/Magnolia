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
     * left pointer
     * */
    private SearchQueryOperator left;

    /**
     * right pointer
     * */
    private SearchQueryOperator right;

    /**
     * @param operator
     * @param left pointing to an Expression on left
     * @param right pointing to an Expression on right
     * */
    public SearchQueryOperator(String operator, SearchQueryOperator left, SearchQueryOperator right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    /**
     * get left expression
     * @return object on left
     * */
    public SearchQueryOperator getLeft() {
        return left;
    }

    /**
     * set left expression
     * @param left object on left
     * */
    public void setLeft(SearchQueryOperator left) {
        this.left = left;
    }

    /**
     * get right expression
     * @return object on right
     * */
    public SearchQueryOperator getRight() {
        return right;
    }

    /**
     * set right expression
     * @param right object on left
     * */
    public void setRight(SearchQueryOperator right) {
        this.right = right;
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
