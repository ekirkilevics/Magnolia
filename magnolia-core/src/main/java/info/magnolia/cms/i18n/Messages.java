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

import java.util.Iterator;
import java.util.Locale;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public interface Messages {

    /**
     * @return current locale
     */
    public Locale getLocale();

    /**
     * If no basename is provided this method returns DEFAULT_BASENAME
     * @return current basename
     */
    public String getBasename();

    /**
     * Get the message from the bundle
     * @param key the key
     * @return message
     */
    public String get(String key);

    /**
     * Replace the parameters in the string: the entered text {0} is not a valid email
     * @param key the key
     * @param args the replacement strings
     * @return message
     */
    public String get(String key, Object[] args);

    /**
     * You can provide a default value if the key is not found
     * @param key key
     * @param defaultMsg the default message
     * @return the message
     */
    public String getWithDefault(String key, String defaultMsg);

    /**
     * With default value and replacement strings
     * @param key key
     * @param args replacement strings
     * @param defaultMsg default message
     * @return message
     */
    public String getWithDefault(String key, Object[] args, String defaultMsg);
    
    /**
     * Iterate over the keys
     * @return iterator
     */
    public Iterator keys();

    /**
     * Reload the messages
     * @throws Exception
     */
    public void reload() throws Exception;

}