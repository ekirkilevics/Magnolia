/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
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

        return new SearchControl(this, value, constraint);
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
