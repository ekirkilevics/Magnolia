/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
     * Name of the javascript object used to make the messages public to the javascripts.
     */
    public static final String JS_OBJECTNAME = "mgnlMessages"; //$NON-NLS-1$

    /**
     * The name of the bundle.
     */
    protected String basename = MessagesManager.DEFAULT_BASENAME;

    /**
     * The current locale.
     */
    protected Locale locale;

    /**
     * The current bundle. Subclasses will overwrite getBundle().
     */
    protected ResourceBundle bundle;

    protected AbstractMessagesImpl(String basename, Locale locale) {
        this.locale = locale;
        this.basename = basename;
    }

    /**
     * Returns the current locale.
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * If no basename is provided this method returns DEFAULT_BASENAME.
     * @return current basename
     */
    @Override
    public String getBasename() {
        return basename;
    }

    /**
     * Replace the {n} parameters in the string.
     * @see java.text.MessageFormat#format(String, Object[])
     */
    @Override
    public String get(String key, Object[] args) {
        return MessageFormat.format(get(key), args);
    }

    /**
     * You can provide a default value if the key is not found.
     * @param key key
     * @param defaultMsg the default message
     * @return the message
     */
    @Override
    public String getWithDefault(String key, String defaultMsg) {
        String msg = get(key);
        if (msg.startsWith("???")) { //$NON-NLS-1$
            msg = defaultMsg;
        }
        return msg;
    }

    /**
     * With default value and replacement strings.
     * @param key key
     * @param args replacement strings
     * @param defaultMsg default message
     * @return message
     */
    @Override
    public String getWithDefault(String key, Object[] args, String defaultMsg) {
        return MessageFormat.format(getWithDefault(key, defaultMsg), args);
    }

    /**
     * True if the basename and the locale are the same.
     */
    @Override
    public boolean equals(Object arg0) {
        if (arg0 == null || !(arg0 instanceof AbstractMessagesImpl)) {
            return false;
        }
        return StringUtils.equals(((AbstractMessagesImpl) arg0).basename, this.basename)
            && this.locale.equals(((AbstractMessagesImpl) arg0).locale);
    }

    /**
     * Nice string.
     */
    @Override
    public String toString() {
        return this.basename + "(" + this.locale + ")";
    }

}
