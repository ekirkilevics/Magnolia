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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
    private Set<String> allowedTypes = new HashSet<String>();

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
            this.allowedTypes.add(nodeType);
        }
    }

    /**
     * Remove from allow list.
     * @param nodeType
     */
    public void removeAllowType(String nodeType) {
        if (nodeType != null) {
            this.allowedTypes.remove(nodeType);
        }
    }

    /**
     * True if given nodeType is allowed.
     */
    public boolean isAllowed(String nodeType) {
        boolean allowed = this.allowedTypes.contains(nodeType);
        if (this.reverse) {
            return !allowed;
        }

        return allowed;

    }

    /**
     * Get a string representation of this rule.
     * @return string representation
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        Iterator<String> typeIterator = allowedTypes.iterator();
        while (typeIterator.hasNext()) {
            buffer.append(typeIterator.next());
            buffer.append(",");
        }
        return buffer.toString();
    }

    /**
     * Set reverse.
     */
    public void reverse() {
        this.reverse = true;
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        final ObjectInputStream.GetField field = ois.readFields();
        final Object o = field.get("allowedTypes", null);
        if (o instanceof String[]){
            Set<String> typesCol = new HashSet<String>();
            typesCol.addAll(Arrays.asList((String[]) o));
            this.allowedTypes = typesCol;
        }
        if(o instanceof HashSet){
            this.allowedTypes = (Set<String>) o;
        }
        this.reverse = field.get("reverse", false);
    }
}
