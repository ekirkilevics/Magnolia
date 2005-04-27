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

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;

import org.apache.log4j.Logger;


/**
 * @author Philipp Bracher Provides localized strings. You should uses the ContextMessages class if you can provide a
 * request object. Messages will do the job as good as possible without to know the session (user) and all the other
 * contextual things. Endusers will use the MessageManager to resolve messages.
 */

public class Messages {

    public static String JS_OBJECTNAME = "mgnlMessages";

    protected static Logger log = Logger.getLogger(Messages.class);

    // never use this directly: subclasses can overrite geter
    private String basename;

    // never use this directly: subclasses can overrite geter
    private Locale locale;

    /**
     * Subclasses will overwrite getBundle()
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
     * @param locale
     */
    protected Messages(String basename, Locale locale) {
        this.locale = locale;
        this.basename = basename;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getBasename() {
        if (basename == null)
            return MessagesManager.DEFAULT_BASENAME;
        return basename;
    }

    protected void setBasename(String basename) {
        this.basename = basename;
    }
    
    public String get(String key) {
        try {
            return getBundle().getString(key);
        }
        catch (MissingResourceException e) {
            return "???" + key + "???";
        }
    }

    /**
     * Replace the parameters in the string: the entered text {0} is not a valid email
     * @param key
     * @param args
     * @return
     */
    public String get(String key, Object args[]) {
        return MessageFormat.format(get(key), args);
    }

    public String getWithDefault(String key, String defaultMsg) {
        String msg;
        try {
            msg = getBundle().getString(key);
            if (msg.startsWith("???")) {
                msg = defaultMsg;
            }

        }
        catch (MissingResourceException e) {
            msg = defaultMsg;
        }
        return msg;
    }

    public String getWithDefault(String key, Object args[], String defaultMsg) {
        return MessageFormat.format(getWithDefault(key, defaultMsg), args);
    }

    /**
     * Adds Variables to a JS witch can be used with the getMessage(key) method
     * @return Javascript-Construct of this textes
     */
    public String generateJavaScript() {
        StringBuffer str = new StringBuffer();
        ResourceBundle bundle = getBundle();

        str.append("/* ###################################\n");
        str.append("### Generated Messages\n");
        str.append("################################### */\n\n");

        Enumeration en = bundle.getKeys();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();

            if (key.startsWith("js.")) {
                String msg = ((String) bundle.getObject(key)).replaceAll("'", "\\\\'").replaceAll("\n", "\\\\n");
                str.append(JS_OBJECTNAME + ".add('" + key + "','" + msg + "','" + getBasename() + "');");
                str.append("\n");
            }
        }
        return str.toString();
    }

    public static String javaScriptString(String str) {
        return str.replaceAll("'", "\\\\'").replaceAll("\n", "\\\\n");
    }

    /**
     * @return Returns the bundle.
     */
    public ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = PropertyResourceBundle.getBundle(getBasename(), getLocale());
        }
        return bundle;
    }

    public ResourceBundle getBundle(String basename) {
        return PropertyResourceBundle.getBundle(basename, getLocale());
    }

    public ResourceBundle getBundle(String basename, Locale locale) {
        return PropertyResourceBundle.getBundle(basename, getLocale());
    }
}