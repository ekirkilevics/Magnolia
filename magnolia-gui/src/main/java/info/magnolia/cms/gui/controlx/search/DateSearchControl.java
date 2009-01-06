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

import info.magnolia.cms.gui.query.DateSearchQueryParameter;
import info.magnolia.cms.gui.query.SearchQueryExpression;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.context.MgnlContext;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;


/**
 * Special Date Search Control
 * @author philipp
 */
public class DateSearchControl extends SearchControl {

    /**
     * 
     */
    public static final String RENDER_TYPE = "dateSearchControl";

    /**
     * 
     */
    public DateSearchControl() {
        this.setRenderType(RENDER_TYPE);
    }

    /**
     * @param definition
     * @param value
     * @param condition
     */
    public DateSearchControl(SearchControlDefinition definition, String value, String condition) {
        super(definition, value, condition);
        this.setRenderType(RENDER_TYPE);
    }

    /**
     * create the date query expression
     */
    public SearchQueryExpression getExpression() {
        Date date = null;
        if (this.getConstraint().equals(DateSearchQueryParameter.TODAY)) {
            date = new Date();
        }
        else {
            String value = getValue();
            try {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                date = format.parse(value);
            }
            catch (ParseException e) {
                try {
                    DateFormat format = DateFormat.getDateInstance(FastDateFormat.SHORT, MgnlContext.getLocale());
                    date = format.parse(value);
                }
                catch (ParseException e1) {
                    try {
                        DateFormat format = DateFormat.getDateInstance(FastDateFormat.SHORT, MgnlContext.getLocale());
                        date = format.parse(value);
                    }
                    catch (ParseException e2) {
                        AlertUtil.setMessage("The date is not properly formated [" + value + "] ");
                    }
                }
            }
        }

        return new DateSearchQueryParameter(this.getDefinition().getColumn(), date, this.getConstraint());
    }
}
