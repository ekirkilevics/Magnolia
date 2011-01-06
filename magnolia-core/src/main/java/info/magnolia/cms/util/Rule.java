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
import java.util.Iterator;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;


/**
 * This class defines the rules to be used by the activation content aggregator this is simply a collection of node
 * types.
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class Rule implements Serializable {

    /**
     * generated Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * list of node types allowed.
     */
    private String[] allowedTypes = new String[0];

    /**
     * reverse rule.
     */
    private boolean reverse = false;

    public Rule() {
    }

    /**
     * Generate list from string array.
     * @param allowedTypes
     */
    public Rule(String[] allowedTypes) {
        for (int j = 0; j < allowedTypes.length; j++) {
            this.addAllowType(allowedTypes[j]);
        }
    }

    /**
     * Generate list from the string.
     * @param allowedTypes
     * @param separator
     */
    public Rule(String allowedTypes, String separator) {
        String[] types = StringUtils.split(allowedTypes, separator);
        for (int j = 0; j < types.length; j++) {
            this.addAllowType(types[j]);
        }
    }

    /**
     * Add to allow list.
     * @param nodeType
     */
    public void addAllowType(String nodeType) {
        if (nodeType != null) {
            this.allowedTypes = (String[]) ArrayUtils.add(allowedTypes, nodeType);
        }
    }

    /**
     * Remove from allow list.
     * @param nodeType
     */
    public void removeAllowType(String nodeType) {
        if (nodeType != null) {
            for (int j = 0; j < allowedTypes.length; j++) {
                if (nodeType.equals(allowedTypes[j])) {
                    this.allowedTypes = (String[]) ArrayUtils.remove(allowedTypes, j);
                    break;
                }
            }
        }
    }

    /**
     * True if given nodeType is allowed.
     */
    public boolean isAllowed(String nodeType) {
        boolean allowed = ArrayUtils.contains(allowedTypes, nodeType);
        if (this.reverse) {
            return !allowed;
        }

        return allowed;

    }

    /**
     * Get a string representation of this rule.
     * @return string representation
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        Iterator typeIterator = IteratorUtils.arrayIterator(allowedTypes);
        while (typeIterator.hasNext()) {
            buffer.append((String) typeIterator.next());
            buffer.append(",");
        }
        return new String(buffer);
    }

    /**
     * Set reverse.
     */
    public void reverse() {
        this.reverse = true;
    }

}
