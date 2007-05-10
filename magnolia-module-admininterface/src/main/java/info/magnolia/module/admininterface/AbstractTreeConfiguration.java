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
package info.magnolia.module.admininterface;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.EmptyMessages;

/**
 * A base implementation of AdminTreeConfiguration that provides the i18n messages. 
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractTreeConfiguration implements AdminTreeConfiguration {
    private Messages messages;

    public void setMessages(Messages m) {
        this.messages = m;
    }

    public Messages getMessages() {
        if (messages == null) {
            messages = new EmptyMessages();
        }
        return messages;
    }
}
