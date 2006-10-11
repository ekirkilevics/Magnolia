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
                        + MessagesManager.getDefaultLocale().getLanguage()
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
