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
