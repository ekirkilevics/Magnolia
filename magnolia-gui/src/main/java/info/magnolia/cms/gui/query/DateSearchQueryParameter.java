/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.query;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles $Id$
 */
public class DateSearchQueryParameter extends SearchQueryParameter {

    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(DateSearchQueryParameter.class);

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
