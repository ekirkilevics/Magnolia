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
        @Override
        public boolean match(String str) {
            return true;
        }

        /**
         * @see info.magnolia.cms.util.UrlPattern#getLength()
         */
        @Override
        public int getLength() {
            return 1;
        }

    }
}
