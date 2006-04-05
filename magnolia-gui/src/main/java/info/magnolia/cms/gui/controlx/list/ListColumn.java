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
package info.magnolia.cms.gui.controlx.list;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.gui.controlx.impl.AbstractControl;

/**
 * This represents a column in a list.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class ListColumn extends AbstractControl{
    /**
     * 
     */
    public static final String RENDER_TYPE = "listColumn";

    /**
     * The name of the column.
     */
    private String name;
    
    /**
     * The label showed 
     */
    private String label;
    
    /**
     * Width of the table
     */
    private String width;
    
    /**
     * Show a separator after this column
     */
    private boolean separator;
    
    /**
     * Create a new column.
     * @param name
     * @param label
     * @param width
     * @param separator
     */
    public ListColumn(String name, String label, String width, boolean separator) {
        this.setRenderType(RENDER_TYPE);
        this.setName(name);
        this.setLabel(label);
        this.setWidth(width);
        this.setSeparator(separator);
    }
    
    /**
     * Empty Constructor. Used for anonymous classes.
     */
    public ListColumn() {
    }

    /**
     * Get the list control this column belongs to.
     * @return the list control
     */
    public ListControl getListControl(){
        return (ListControl) this.getParent();
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        if(StringUtils.isEmpty(this.label)){
            return this.getName();
        }
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

    
    /**
     * @return Returns the separator.
     */
    public boolean isSeparator() {
        return this.separator;
    }

    
    /**
     * @param separator The separator to set.
     */
    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

    
    /**
     * @return Returns the width.
     */
    public String getWidth() {
        return this.width;
    }

    
    /**
     * @param width The width to set.
     */
    public void setWidth(String width) {
        this.width = width;
    }

    /**
     * Called by the renderer
     * @return the object to render
     */
    public Object getValue(){
        return this.getListControl().getIteratorValue(this.getName());
    }

}
