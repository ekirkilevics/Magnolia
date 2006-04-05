/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.controlx.search;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class SearchControlDefinition {
    
    public String name;

    public String label;
    
    public String type;

    public SearchControlDefinition(String name, String label) {
        setName(name);
        setName(label);
    }

    public String getJsField() {
        return this.getName()
            + ": {name:'"
            + this.getName()
            + "', label:'"
            + this.getLabel()
            + "', type:'"
            + this.getType()
            + "'}";
    }
    
    public SearchControl getSearchControlInstance(String value, String constraint){
        return null;
    }

    
    /**
     * @return Returns the type.
     */
    public String getType() {
        return this.type;
    }

    
    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    
    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return this.label;
    }

    
    /**
     * @param label The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

}
