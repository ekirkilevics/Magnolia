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
