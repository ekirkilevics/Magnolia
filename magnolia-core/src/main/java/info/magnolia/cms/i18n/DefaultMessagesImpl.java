/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.i18n;

import info.magnolia.cms.util.ClasspathResourcesUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.IOUtils;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DefaultMessagesImpl extends AbstractMessagesImpl {

    /**
     * @param basename
     * @param locale
     */
    protected DefaultMessagesImpl(String basename, Locale locale) {
        super(basename, locale);
    }

    /**
     * Get the message from the bundle
     * @param key the key
     * @return message
     */
    public String get(String key) {
        if(key == null){
            return "??????";
        }
        try {
            return getBundle().getString(key);
        }
        catch (MissingResourceException e) {
            return "???" + key + "???"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * @return Returns the bundle for the current basename
     */
    protected ResourceBundle getBundle() {
        if (bundle == null) {
            InputStream stream = null;
            try {
                // TODO : isnt this what ResourceBundle does? except maybe for some ClasspathResourcesUtil magic ? 
                stream = ClasspathResourcesUtil.getStream("/"
                    + StringUtils.replace(basename, ".", "/")
                    + "_"
                    + getLocale().getLanguage()
                    + "_"
                    + getLocale().getCountry()
                    + ".properties", false);
                if (stream == null) {
                    stream = ClasspathResourcesUtil.getStream("/"
                        + StringUtils.replace(basename, ".", "/")
                        + "_"
                        + getLocale().getLanguage()
                        + ".properties", false);
                }
                if (stream == null) {
                    stream = ClasspathResourcesUtil.getStream("/"
                        + StringUtils.replace(basename, ".", "/")
                        + "_"
                        + MessagesManager.getInstance().getDefaultLocale().getLanguage()
                        + ".properties", false);
                }
                if (stream == null) {
                    stream = ClasspathResourcesUtil.getStream("/"
                        + StringUtils.replace(basename, ".", "/")
                        + ".properties", false);
                }

                if (stream != null) {
                    bundle = new PropertyResourceBundle(stream);
                }
                else {
                    bundle = ResourceBundle.getBundle(getBasename(), getLocale());
                }
            }
            catch (IOException e) {
                log.error("can't load messages for " + basename);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        return bundle;
    }

    public void reload() throws Exception {
        this.bundle = null;
    }

    /**
     * Iterate over the keys
     */
    public Iterator keys() {
        return IteratorUtils.asIterator(this.getBundle().getKeys());
    }

}
