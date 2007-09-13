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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;

import java.util.Date;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CreationDateComparator extends AbstractContentComparator {

    protected int compare(Content c1, Content c2) {
        final Date date1 = c1.getMetaData().getCreationDate().getTime();
        final Date date2 = c2.getMetaData().getCreationDate().getTime();
        return date1.compareTo(date2);
    }

}
