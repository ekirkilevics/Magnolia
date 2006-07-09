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
public abstract class SearchQueryParameter extends AbstractExpressionImpl {

    /**
     * parameter name
     */
    protected String name;

    /**
     * parameter value, this could be either String or Date
     */
    protected Object value;

    /**
     * constraint for this parameter
     */
    protected String constraint;

    /**
     * @param name of this parameter
     * @param value
     * @param constraint check constraint constants
     */
    public SearchQueryParameter(String name, Object value, String constraint) {
        this.name = name;
        this.value = value;
        this.constraint = constraint;
    }

    /**
     * get name of the parameter field
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * set parameter field name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get constraint name
     * @return constraint
     */
    public String getConstraint() {
        return constraint;
    }

    /**
     * set constraint
     * @param constraint
     */
    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

}
