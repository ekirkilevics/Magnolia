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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;

/**
 * @author Philipp Bracher
 * 
 * Provieds localized strings. You should uses the ContextMessages class if you
 * can provide a request object. Messages will do the job as good as possible
 * without to know the session (user) and all the other contextual things.
 */

public class Messages {

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
    public Messages(String basename) {
        setLocale(Locale.getDefault());
        setBasename(basename);
    }

    /**
     * @param basename name of the bundle
     * @param locale
     */
    public Messages(String basename, Locale locale) {
        setLocale(locale);
        setBasename(basename);
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getBasename() {
        return basename;
    }

    public void setBasename(String basename) {
        this.basename = basename;
        bundle = null;
    }

    public String get(String key) {
        return getBundle().getString(key);
    }

    public String get(String key, String basename) {
        return getBundle(basename).getString(key);
    }
    
    /**
     * Replace the parameters in the string: the entered text {0} is not a valid email
     * 
     * @param key
     * @param args
     * @return
     */
    public String get(String key, Object args[]) {
        return MessageFormat.format(get(key), args);
    }

    public String get(String key, Object args[], String basename) {
        return MessageFormat.format(get(key, basename), args);
    }

    /**
     * Adds Variables to a JS witch can be used with the getMessage(key) method
     * 
     * @return Javascript-Construct of this textes
     * 
     * @todo implement
     */
    public String generateJavaScript() {
        return null;
    }

    /**
     * @return Returns the bundle.
     */
    public ResourceBundle getBundle() {
        if (bundle == null){
            bundle = PropertyResourceBundle.getBundle(getBasename(),
                    getLocale());
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