/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */





package info.magnolia.cms.util;

import jdsl.core.ref.ComparableComparator;

import java.util.Date;

import info.magnolia.cms.core.Content;


/**
 * Date: Apr 10, 2003
 * Time: 09:00:12 AM
 * @author Marcel Salathe
 * @version 1.1
 */


public class DateComparator extends ComparableComparator{
    public int compare(Object o, Object o1) throws ClassCastException {
        Date date1 = ((Content)o).getMetaData().getCreationDate().getTime();
        Date date2 = ((Content)o1).getMetaData().getCreationDate().getTime();
		return super.compare(date1, date2);
    }

    public boolean isComparable(Object o) {
        return true;
    }
}
