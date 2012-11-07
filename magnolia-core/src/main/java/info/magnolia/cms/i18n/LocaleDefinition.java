/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.i18n;

import org.apache.commons.lang.StringUtils;

import java.util.Locale;


/**
 * General language definition. Used to configure the i18n components.
 * @author philipp
 * @version $Id$
 */
public class LocaleDefinition {

    public static LocaleDefinition make(String language, String country, boolean enabled) {
        return new LocaleDefinition(language, country, enabled);
    }

    private String country;

    private String language;

    private boolean enabled;

    private Locale locale;

    public LocaleDefinition() {
    }

    // worked around by making the ctor protected and adding a public static factory method. discuss.
    protected LocaleDefinition(String language, String country, boolean enabled) {
        this.language = language;
        this.country = country;
        this.enabled = enabled;
    }

    /**
     * Creates the locale for this definition if not yet set.
     */
    public Locale getLocale() {
        if (locale == null && getLanguage() != null) {
            locale = new Locale(getLanguage(), StringUtils.defaultString(getCountry()));
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

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Uses the locals toString() method.
     */
    @Override
    public String toString() {
        return getLocale() != null ? getLocale().toString() : "none";
    }

    public String getId() {
        return getLocale().toString();
    }
}
