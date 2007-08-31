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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides localized strings. You should uses the ContextMessages class if you can provide a request object.
 * AbstractMessagesImpl will do the job as good as possible without to know the session (user) and all the other
 * contextual things. Endusers will use the MessageManager to resolve messages.
 * @author Philipp Bracher
 */

public abstract class AbstractMessagesImpl implements Messages {

    protected Logger log = LoggerFactory.getLogger(AbstractMessagesImpl.class);

    /**
     * Name of the javascript object used to make the messages public to the javascripts
     */
    public static final String JS_OBJECTNAME = "mgnlMessages"; //$NON-NLS-1$

    /**
     * The name of the bundle
     */
    protected String basename = MessagesManager.DEFAULT_BASENAME;

    /**
     * The current locale
     */
    protected Locale locale;

    /**
     * The current bundle. Subclasses will overwrite getBundle()
     */
    protected ResourceBundle bundle;

    /**
     * @param basename name of the bundle
     * @param locale the locale
     */
    protected AbstractMessagesImpl(String basename, Locale locale) {
        this.locale = locale;
        this.basename = basename;
    }

    /**
     * @return current locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * If no basename is provided this method returns DEFAULT_BASENAME
     * @return current basename
     */
    public String getBasename() {
        return basename;
    }

    /**
     * Replace the parameters in the string: the entered text {0} is not a valid email
     * @param key the key
     * @param args the replacement strings
     * @return message
     */
    public String get(String key, Object[] args) {
        return MessageFormat.format(get(key), args);
    }

    /**
     * You can provide a default value if the key is not found
     * @param key key
     * @param defaultMsg the default message
     * @return the message
     */
    public String getWithDefault(String key, String defaultMsg) {
        String msg = get(key);
        if (msg.startsWith("???")) { //$NON-NLS-1$
            msg = defaultMsg;
        }
        return msg;
    }

    /**
     * With default value and replacement strings
     * @param key key
     * @param args replacement strings
     * @param defaultMsg default message
     * @return message
     */
    public String getWithDefault(String key, Object[] args, String defaultMsg) {
        return MessageFormat.format(getWithDefault(key, defaultMsg), args);
    }

    /**
     * True if the basename and the locale are the same
     */
    public boolean equals(Object arg0) {
        return StringUtils.equals(((AbstractMessagesImpl) arg0).basename, this.basename)
            && this.locale.equals(((AbstractMessagesImpl) arg0).locale);
    }

    /**
     * Nice string
     */
    public String toString() {
        return this.basename + "(" + this.locale + ")";
    }

}