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

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;


/**
 * Provides localized strings. You should uses the ContextMessages class if you can provide a request object. Messages
 * will do the job as good as possible without to know the session (user) and all the other contextual things. Endusers
 * will use the MessageManager to resolve messages.
 * @author Philipp Bracher
 */

public class Messages {

    /**
     * The log4j logger
     */
    private static Logger log = Logger.getLogger(Messages.class);

    /**
     * Name of the javascript object used to make the messages public to the javascripts
     */
    public static final String JS_OBJECTNAME = "mgnlMessages"; //$NON-NLS-1$

    /**
     * The name of the bundle
     */
    private String basename;

    /**
     * The current locale
     */
    private Locale locale;

    /**
     * The current bundle. Subclasses will overwrite getBundle()
     */
    private ResourceBundle bundle;

    /**
     * Used by sublcasses. Do not use without knowledge
     */
    protected Messages() {

    }

    /**
     * @param basename the name of the bundle
     */
    protected Messages(String basename) {
        this.locale = Locale.getDefault();
        this.basename = basename;
    }

    /**
     * @param basename name of the bundle
     * @param locale the locale
     */
    protected Messages(String basename, Locale locale) {
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
        if (basename == null) {
            return MessagesManager.DEFAULT_BASENAME;
        }
        return basename;
    }

    /**
     * @param basename set the name of the bundle
     */
    protected void setBasename(String basename) {
        this.basename = basename;
    }

    /**
     * Get the message from the bundle
     * @param key the key
     * @return message
     */
    public String get(String key) {
        try {
            return getBundle().getString(key);
        }
        catch (MissingResourceException e) {
            return "???" + key + "???"; //$NON-NLS-1$ //$NON-NLS-2$
        }
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
        String msg;
        try {
            msg = getBundle().getString(key);
            if (msg.startsWith("???")) { //$NON-NLS-1$
                msg = defaultMsg;
            }

        }
        catch (MissingResourceException e) {
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
     * Adds Variables to a JS witch can be used with the getMessage(key) method
     * @return Javascript-Construct of this textes
     */
    public String generateJavaScript() {
        StringBuffer str = new StringBuffer();
        ResourceBundle bundle = getBundle();

        str.append("/* ###################################\n"); //$NON-NLS-1$
        str.append("### Generated Messages\n"); //$NON-NLS-1$
        str.append("################################### */\n\n"); //$NON-NLS-1$

        Enumeration en = bundle.getKeys();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();

            if (key.startsWith("js.")) { //$NON-NLS-1$
                String msg = ((String) bundle.getObject(key)).replaceAll("'", "\\\\'").replaceAll("\n", "\\\\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                str.append(JS_OBJECTNAME + ".add('" + key + "','" + msg + "','" + getBasename() + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                str.append("\n"); //$NON-NLS-1$
            }
        }
        return str.toString();
    }

    /**
     * Make the string save for javascript (escape special characters).
     * @param str string to escape
     * @return escaped string
     */
    public static String javaScriptString(String str) {
        return str.replaceAll("'", "\\\\'").replaceAll("\n", "\\\\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    /**
     * @return Returns the bundle for the current basename
     */
    public ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(getBasename(), getLocale());
        }
        return bundle;
    }

    /**
     * return the bundle for the provided basename
     * @param basename basename
     * @return bundle
     */
    public ResourceBundle getBundle(String basename) {
        return ResourceBundle.getBundle(basename, getLocale());
    }

    /**
     * Get the bundle for a defined local
     * @param basename basename
     * @param locale locale
     * @return bundle
     */
    public ResourceBundle getBundle(String basename, Locale locale) {
        return ResourceBundle.getBundle(basename, getLocale());
    }

    public void reloadBundles() throws Exception {
        reloadBundle(getBundle());
    }

    protected void reloadBundle(ResourceBundle bund) throws Exception {
        try {
            Class klass = bund.getClass().getSuperclass();
            Field field;
            field = klass.getDeclaredField("cacheList");
            field.setAccessible(true);
            sun.misc.SoftCache cache = (sun.misc.SoftCache) field.get(null);
            cache.clear();
            if (log.isInfoEnabled())
                log.info("Cleaning messages for locale:" + bund.getLocale() + "...");
        }
        catch (Exception e) {
            log.error("Error while cleaning messages ...");
            throw e;
        }
    }
}