/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.util;

import java.io.Serializable;


/**
 * Pattern interface, used to match URLs.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public interface UrlPattern extends Serializable {

    /**
     * Does the patter match the given url?
     * @param url url to match
     * @return <code>true</code> if the given URL matches the pattern
     */
    boolean match(String url);

    /**
     * Returns the pattern length. Longer patterns have higher priority.
     * @return pattern length
     */
    int getLength();

    /**
     * A pattern which matches any input.
     */
    UrlPattern MATCH_ALL = new MatchAllPattern();

    /**
     * A default implementation with matches any input.
     */
    public static final class MatchAllPattern implements UrlPattern {

        /**
         * Stable serialVersionUID.
         */
        private static final long serialVersionUID = 222L;

        /**
         * Instantiates a new MatchAllPattern instance. Use the MATCH_ALL constant and don't create new instances.
         */
        protected MatchAllPattern() {
            // protected contructor
        }

        /**
         * @see info.magnolia.cms.util.UrlPattern#match(java.lang.String)
         */
        public boolean match(String str) {
            return true;
        }

        /**
         * @see info.magnolia.cms.util.UrlPattern#getLength()
         */
        public int getLength() {
            return 1;
        }

    }
}
