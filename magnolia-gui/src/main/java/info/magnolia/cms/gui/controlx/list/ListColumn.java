/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.gui.controlx.list;

import info.magnolia.cms.gui.controlx.impl.AbstractControl;


/**
 * This represents a column in a list.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ListColumn extends AbstractControl {

    /**
     * 
     */
    public static final String RENDER_TYPE = "listColumn";

    /**
     * The columnName of the column.
     */
    private String columnName;

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
     * Empty Constructor. Used for anonymous classes.
     */
    public ListColumn() {
        this.setRenderType(RENDER_TYPE);
    }

    /**
     * Create a new column.
     * @param columnName
     * @param label
     * @param width
     * @param separator
     */
    public ListColumn(String columnName, String label, String width, boolean separator) {
        this();
        this.setName(columnName);
        this.setColumnName(columnName);
        this.setLabel(label);
        this.setWidth(width);
        this.setSeparator(separator);
    }

    /**
     * Get the list control this column belongs to.
     * @return the list control
     */
    public ListControl getListControl() {
        return (ListControl) this.getParent();
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        if (this.label == null) {
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
    public Object getValue() {
        return this.getListControl().getIteratorValue(this.getColumnName());
    }

    /**
     * @return Returns the columnName.
     */
    public String getColumnName() {
        if(this.columnName == null){
            return this.getName();
        }
        return this.columnName;
    }

    /**
     * @param columnName The columnName to set.
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

}
