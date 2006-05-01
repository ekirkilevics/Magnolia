/**
 *  Copyright 2005 obinary ag.  All rights reserved.
 *  See license distributed with this file and available
 *  online at http://www.magnolia.info/dms-license.html
 */

package info.magnolia.cms.gui.controlx.search;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.gui.controlx.impl.AbstractControl;
import info.magnolia.cms.gui.query.SearchQueryExpression;
import info.magnolia.cms.gui.query.StringSearchQueryParameter;


/**
 * This is a real control instance
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class SearchControl extends AbstractControl {

    public static final String RENDER_TYPE = "searchControl";

    private SearchControlDefinition definition;

    private String value;

    private String constraint;

    public SearchControl() {
        this.setRenderType(RENDER_TYPE);
    }

    /**
     * @param definition
     * @param value
     * @param constraint
     */
    public SearchControl(SearchControlDefinition definition, String value, String condition) {
        this();
        this.definition = definition;
        this.value = value;
        this.constraint = condition;
    }

    /**
     * @return Returns the constraint.
     */
    public String getConstraint() {
        return constraint;
    }

    /**
     * @param constraint The constraint to set.
     */
    public void setConstraint(String condition) {
        this.constraint = condition;
    }

    /**
     * @return Returns the definition.
     */
    public SearchControlDefinition getDefinition() {
        return definition;
    }

    /**
     * @param definition The definition to set.
     */
    public void setDefinition(SearchControlDefinition definition) {
        this.definition = definition;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return
     */
    public SearchQueryExpression getExpression() {
        String value = this.getValue().toString();
        if(StringUtils.isNotEmpty(value)){
            return new StringSearchQueryParameter(this.getDefinition().getColumn(), value, this.getConstraint());
        }
        return null;
    }


}
