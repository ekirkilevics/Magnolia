/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.i18n;

import java.util.Locale;


/**
 * General language definition. Used to configure the i18n components.
 * @author philipp
 * @version $Id$
 */
public class LanguageDefinition {

    private String country;

    private String language;

    private boolean enabled;

    private Locale locale;

    /**
     * Creates the locale for this definition if not yet set.
     */
    public Locale getLocale() {
        if (locale == null) {
            locale = new Locale(getLanguage(), getCountry());
        }
        return locale;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setLocal(Locale locale) {
        this.locale = locale;
    }

    /**
     * Uses the locals toString() method
     */
    public String toString() {
        return getLocale().toString();
    }
}
