/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


/**
 * @author Sameer Charles $Id$
 */
public class DateSearchQueryParameter extends SearchQueryParameter {
    private static final Logger log = LoggerFactory.getLogger(DateSearchQueryParameter.class);

    /**
     * sql constraint "TODAY"
     */
    public static final String TODAY = "today";

    /**
     * sql constraint "AFTER"
     */
    public static final String AFTER = "after";

    /**
     * sql constraint "BEFORE"
     */
    public static final String BEFORE = "before";

    /**
     * sql constraint "IS"
     */
    public static final String IS = "is";

    /**
     * default date format
     */
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * @param name of this parameter
     * @param value
     * @param constraint check SearchQueryParameter constants
     */
    public DateSearchQueryParameter(String name, Date value, String constraint) {
        super(name, value, constraint);
    }

    /**
     * get value
     * @return Date
     */
    public Date getValue() {
        return (Date) this.value;
    }

    /**
     * set value
     * @param value Date
     */
    public void setValue(Date value) {
        this.value = value;
    }

    /**
     * set date format
     * @param format
     */
    public void setDateFormat(SimpleDateFormat format) {
        this.format = format;
    }

    /**
     * get date format
     * @return date format, either the one which has been set or default "yyyy-MM-dd"
     */
    public SimpleDateFormat getDateFormat() {
        return this.format;
    }
}
