/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.i18n;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.IteratorUtils;


/**
 * Chains messages
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class MessagesChain extends AbstractMessagesImpl {

    /**
     * The chain
     */
    private List chain = new ArrayList();

    /**
     * Create a chain passing the wrapped head of the chain
     */
    public MessagesChain(Messages head) {
        super(head.getBasename(), head.getLocale());
        chain.add(head);
    }

    /**
     * Append messages to the chain
     * @param messages
     * @return the chain itself
     */
    public Messages chain(Messages messages) {
        chain.add(messages);
        return this;
    }

    /**
     * Get the string searching in the chain
     */
    public String get(String key) {
        for (Iterator iter = chain.iterator(); iter.hasNext();) {
            Messages msgs = (Messages) iter.next();
            String str = msgs.get(key);
            if (!str.startsWith("???")) {
                return str;
            }
        }
        return "???" + key + "???";
    }

    /**
     * Return all keys contained in this chain
     */
    public Iterator keys() {
        Set keys = new HashSet();
        for (Iterator iter = chain.iterator(); iter.hasNext();) {
            Messages msgs = (Messages) iter.next();
            List current = IteratorUtils.toList(msgs.keys());
            keys.addAll(current);
        }
        return keys.iterator();
    }

    /**
     * Reload the chain
     */
    public void reload() throws Exception {
        for (Iterator iter = chain.iterator(); iter.hasNext();) {
            Messages msgs = (Messages) iter.next();
            msgs.reload();
        }
    }
}
