/**
 *  Copyright 2005 obinary ag.  All rights reserved.
 *  See license distributed with this file and available
 *  online at http://www.magnolia.info/dms-license.html
 */

package info.magnolia.cms.gui.controlx.search;

import info.magnolia.cms.gui.controlx.impl.AbstractControl;


/**
 * This is a real control instance
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class SearchControl extends AbstractControl {

    /**
     * 
     */
    public static final String RENDER_TYPE = "searchControl";

    private SearchControlDefinition definition;

    private Object value;

    private String condition;

    public SearchControl() {
        this.setRenderType(RENDER_TYPE);
    }
    
    /**
     * @return Returns the condition.
     */
    public String getCondition() {
        return condition;
    }

    /**
     * @param condition The condition to set.
     */
    public void setCondition(String condition) {
        this.condition = condition;
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
    public Object getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(Object value) {
        this.value = value;
    }

}
