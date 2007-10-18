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
