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

import java.util.Iterator;
import java.util.Locale;


/**
 * Storage of messages - key value pairs.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public interface Messages {

    /**
     * @return current locale
     */
    public Locale getLocale();

    /**
     * If no basename is provided this method returns DEFAULT_BASENAME.
     * @return current basename
     */
    public String getBasename();

    /**
     * Get the message from the bundle.
     * @param key the key
     * @return message
     */
    public String get(String key);

    /**
     * Replace the {n} parameters in the string.
     * @see java.text.MessageFormat#format(String, Object...)
     */
    public String get(String key, Object[] args);

    /**
     * You can provide a default value if the key is not found.
     * @param key key
     * @param defaultMsg the default message
     * @return the message
     */
    public String getWithDefault(String key, String defaultMsg);

    /**
     * With default value and replacement strings.
     * @param key key
     * @param args replacement strings
     * @param defaultMsg default message
     * @return message
     */
    public String getWithDefault(String key, Object[] args, String defaultMsg);

    /**
     * Iterate over the keys.
     * @return iterator
     */
    public Iterator keys();

    /**
     * Reload the messages.
     * @throws Exception
     */
    public void reload() throws Exception;

}
