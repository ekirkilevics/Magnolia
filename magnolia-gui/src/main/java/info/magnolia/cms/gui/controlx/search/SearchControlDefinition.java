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

import org.apache.commons.lang.StringUtils;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class SearchControlDefinition {

    /**
     * The name of the definition
     */
    private String name;

    /**
     * Normaly the same as the name of the field, but used to build the query
     */
    private String column;

    /**
     * Display name
     */
    private String label;

    /**
     * Used for the rendering in javascript.
     */
    private String type;

    /**
     * Default type is edit.
     */
    public SearchControlDefinition() {
        setType("edit");
    }

    public SearchControlDefinition(String name, String label) {
        this(name, label, "edit");
    }

    public SearchControlDefinition(String name, String label, String type) {
        setName(name);
        setLabel(label);
        setType(type);
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

    public SearchControl getSearchControlInstance(String value, String constraint) {
        if (this.type.equals("date")) {
            return new DateSearchControl(this, value, constraint);
        }
        else {
            return new SearchControl(this, value, constraint);
        }
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
        if (StringUtils.isEmpty(this.getColumn())) {
            this.setColumn(name);
        }
        // avoid broken javascripts
        this.name = StringUtils.replace(name, ":", "_");
    }

    /**
     * @return Returns the column.
     */
    public String getColumn() {
        return this.column;
    }

    /**
     * @param column The column to set.
     */
    public void setColumn(String column) {
        this.column = column;
    }

}
