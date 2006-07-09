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
 * @author Sameer Charles $Id$
 */
public abstract class AbstractExpressionImpl implements SearchQueryExpression {

    /**
     * previous pointer
     */
    private SearchQueryExpression left;

    /**
     * next pointer
     */
    private SearchQueryExpression right;

    /**
     * get left expression
     * @return object on left
     */
    public SearchQueryExpression getLeft() {
        return this.left;
    }

    /**
     * set left expression
     * @param left object on left
     */
    public void setleft(SearchQueryExpression left) {
        this.left = left;
    }

    /**
     * checks if has left
     * @return true if left object exist
     */
    public boolean hasLeft() {
        return (this.getLeft() != null);
    }

    /**
     * get right expression
     * @return object on right
     */
    public SearchQueryExpression getRight() {
        return this.right;
    }

    /**
     * set right expression
     * @param right object on right
     */
    public void setRight(SearchQueryExpression right) {
        this.right = right;
    }

    /**
     * checks if has right
     * @return true if right object exist
     */
    public boolean hasRight() {
        return (this.getRight() != null);
    }

}
