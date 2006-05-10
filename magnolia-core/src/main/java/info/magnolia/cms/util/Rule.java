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
import java.util.Iterator;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;


/**
 * This class defines the rules to be used by the activation content aggregator this is simply a collection of node
 * types
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class Rule implements Serializable {

    /**
     * generated Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * list of node types allowed
     */
    private String[] allowedTypes = new String[0];

    /**
     * reverse rule
     */
    private boolean reverse = false;

    /**
     * Default
     */
    public Rule() {
    }

    /**
     * generate list from string array
     * @param allowedTypes
     */
    public Rule(String[] allowedTypes) {
        for (int j = 0; j < allowedTypes.length; j++) {
            this.addAllowType(allowedTypes[j]);
        }
    }

    /**
     * generate list from the string
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
     * add to allow list
     * @param nodeType
     */
    public void addAllowType(String nodeType) {
        if (nodeType != null) {
            ArrayUtils.add(allowedTypes, nodeType);
        }
    }

    /**
     * remove from allow list
     * @param nodeType
     */
    public void removeAllowType(String nodeType) {
        if (nodeType != null) {
            for (int j = 0; j < allowedTypes.length; j++) {
                if (nodeType.equals(allowedTypes[j])) {
                    ArrayUtils.remove(allowedTypes, j);
                    break;
                }
            }
        }
    }

    /**
     * is allowed
     * @param nodeType
     * @return true if given nodeType is allowed
     */
    public boolean isAllowed(String nodeType) {
        boolean allowed = ArrayUtils.contains(allowedTypes, nodeType);
        if (this.reverse) {
            return !allowed;
        }

        return allowed;

    }

    /**
     * get a string representation of this rule
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
     * set reverse
     */
    public void reverse() {
        this.reverse = true;
    }

}
