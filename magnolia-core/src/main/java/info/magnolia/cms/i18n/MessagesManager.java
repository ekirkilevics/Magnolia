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
package info.magnolia.cms.i18n;

import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;

import java.util.Collection;
import java.util.Locale;


/**
 * From this class you get the i18n messages. You should pass a a request, but if you can't the getMessages method will
 * handle it properly. The get() methods are easy to use.
 * @author philipp
 * @author molaschi
 */
public abstract class MessagesManager {

    /**
     * Use this locale if no other provided.
     */
    public static final String FALLBACK_LOCALE = "en"; //$NON-NLS-1$

    /**
     * Use this basename if no other is provided.
     */
    public static final String DEFAULT_BASENAME = "info.magnolia.module.admininterface.messages"; //$NON-NLS-1$

    /**
     * The node name where the configuration for i18n is stored.
     */
    public static final String I18N_CONFIG_PATH = "/server/i18n/system"; //$NON-NLS-1$

    /**
     * The name of the property to store the current system language.
     */
    public static final String FALLBACK_NODEDATA = "fallbackLanguage"; //$NON-NLS-1$

    /**
     * Under this node all the available languages are stored. They are showed in the user dialog.
     */
    public static final String LANGUAGES_NODE_NAME = "languages"; //$NON-NLS-1$

    public static MessagesManager getInstance() {
        return Components.getSingleton(MessagesManager.class);
    }

    public static Messages getMessages() {
        return getMessages(null, getCurrentLocale());
    }

    public static Messages getMessages(String basename) {
        return getMessages(basename, getCurrentLocale());
    }

    public static Messages getMessages(Locale locale) {
        return getMessages(null, locale);
    }

    public static Messages getMessages(String basename, Locale locale) {
        return getInstance().getMessagesInternal(basename, locale);
    }

    public static String get(String key) {
        return getMessages().get(key);
    }

    /**
     * Get a message with parameters inside: the value {0} must be a number.
     * @param key key to find
     * @param args replacement strings
     * @return message
     */
    public static String get(String key, Object[] args) {
        return getMessages().get(key, args);
    }

    /**
     * Use a default string.
     * @param key key to find
     * @param defaultMsg default message
     * @return message
     */
    public static String getWithDefault(String key, String defaultMsg) {
        return getMessages().getWithDefault(key, defaultMsg);
    }

    /**
     * Get a message with parameters inside: the value {0} must be a number. Use a default message.
     * @param key key to find
     * @param args replacement strings
     * @param defaultMsg default message
     * @return message
     */
    public static String getWithDefault(String key, Object[] args, String defaultMsg) {
        return getMessages().getWithDefault(key, args, defaultMsg);
    }

    private static Locale getCurrentLocale() {
        return MgnlContext.getInstance().getLocale();
    }

    public abstract void init();

    public abstract Collection getAvailableLocales();

    public abstract Locale getDefaultLocale();

    protected abstract Messages getMessagesInternal(String basename, Locale locale);

    public abstract void reload();
}
