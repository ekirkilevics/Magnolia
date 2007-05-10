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
package info.magnolia.cms.i18n;

import org.apache.commons.collections.IteratorUtils;

import java.util.Iterator;

/**
 * A null-pattern implementation of the messages interface.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class EmptyMessages extends AbstractMessagesImpl {

    public EmptyMessages() {
        super(null, null);
    }

    public String get(String key) {
        return key;
    }

    public Iterator keys() {
        return IteratorUtils.EMPTY_ITERATOR;
    }

    public void reload() throws Exception {
    }
}
