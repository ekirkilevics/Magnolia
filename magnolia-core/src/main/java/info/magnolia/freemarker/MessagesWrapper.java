/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.freemarker;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesChain;
import info.magnolia.cms.i18n.MessagesManager;

import java.util.List;
import java.util.Locale;

/**
 * Utility class that has methods which allow passing multiple parameters
 * from a freemarker template to a message string using the square bracket
 * syntax (e.g. ${i18n.get('message', ['param1', 'param2']}). There are
 * convenience methods which allow selecting the message bundle directly
 * from within the template as well (by passing the basename parameter).
 *
 * @see {info.magnolia.freemarker.FreemarkerHelperTest for more syntax details.
 * @author vsteller
 * @version $Revision: $ ($Author: $)
 */
public class MessagesWrapper {
    private final Messages messages;
    private final Locale locale;

    MessagesWrapper(String basename, Locale locale) {
        final Messages msg = MessagesManager.getMessages(basename, locale);
        final Messages defMsg = MessagesManager.getMessages(locale);
        this.messages = new MessagesChain(msg).chain(defMsg);
        this.locale = locale;
    }

    public String get(String key) {
        return this.get(key, this.messages);
    }

    public String get(String key, List args) {
        return this.get(key, args, this.messages);
    }

    // TODO : this behaves differently than the constructor: no fallback to default resource bundle
    public String get(String key, String basename) {
        return this.get(key, MessagesManager.getMessages(basename, locale));
    }

    // TODO : this behaves differently than the constructor: no fallback to default resource bundle
    public String get(String key, List args, String basename) {
        return this.get(key, args, MessagesManager.getMessages(basename, locale));
    }

    // TODO : not tested
    public String getWithDefault(String key, String defaultMsg) {
        return this.getWithDefault(key, defaultMsg, this.messages);
    }

    // TODO : not tested
    public String getWithDefault(String key, String defaultMsg, String basename) {
        return this.getWithDefault(key, defaultMsg, MessagesManager.getMessages(basename, locale));
    }

    // TODO : not tested
    public String getWithDefault(String key, List args, String defaultMsg) {
        return this.getWithDefault(key, args, defaultMsg, this.messages);
    }

    // TODO : not tested
    public String getWithDefault(String key, List args, String defaultMsg, String basename) {
        return this.getWithDefault(key, defaultMsg, MessagesManager.getMessages(basename, locale));
    }

    protected String get(String key, Messages messages) {
        return messages.get(key);
    }

    protected String get(String key, List args, Messages messages) {
        Object[] argsArray = new Object[args.size()];
        return messages.get(key, args.toArray(argsArray));
    }

    protected String getWithDefault(String key, String defaultMsg, Messages messages) {
        return messages.getWithDefault(key, defaultMsg);
    }

    protected String getWithDefault(String key, List args, String defaultMsg, Messages messages) {
        Object[] argsArray = new Object[args.size()];
        return messages.getWithDefault(key, args.toArray(argsArray), defaultMsg);
    }
}
