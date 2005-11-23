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
package info.magnolia.cms.exchange;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class defines the rules to be used by the activation content aggregator
 * this is simply a collection of node types
 *
 * @author Sameer Charles
 * @version $Revision: 1633 $ ($Author: scharles $)
 */
public class Rule {


    /**
     * list of node types allowed
     * */
    private List allowedTypes = new ArrayList();

    /**
     * reverse rule
     * */
    private boolean reverse = false;

    /**
     * Default
     * */
    public Rule () {}

    /**
     * generate list from string array
     * @param allowedTypes
     * */
    public Rule (String[] allowedTypes) {
        for (int index=0; index<allowedTypes.length; index++) {
            this.addAllowType(allowedTypes[index]);
        }
    }

    /**
     * generate list from the string
     * @param allowedTypes
     * @param separator
     * */
    public Rule (String allowedTypes, String separator) {
        String[] types = StringUtils.split(allowedTypes, separator);
        for (int index=0; index<types.length; index++) {
            this.addAllowType(types[index]);
        }
    }

    /**
     * add to allow list
     * @param nodeType
     * */
    public void addAllowType(String nodeType) {
        if (nodeType != null) {
            this.allowedTypes.add(nodeType);
        }
    }

    /**
     * remove from allow list
     * @param nodeType
     * */
    public void removeAllowType(String nodeType) {
        if (nodeType != null) {
            this.allowedTypes.remove(nodeType);
        }
    }

    /**
     * is allowed
     * @param nodeType
     * @return true if given nodeType is allowed
     * */
    public boolean isAllowed(String nodeType) {
        if (this.reverse) {
            if (this.allowedTypes.contains(nodeType)) {
                return false;
            }
            return true;
        } else if (this.allowedTypes.contains(nodeType)) {
            return true;
        }
        return false;
    }

    /**
     * get a string representation of this rule
     * @return string representation
     * */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        Iterator typeIterator =  this.allowedTypes.iterator();
        while (typeIterator.hasNext()) {
            buffer.append((String) typeIterator.next());
            buffer.append(",");
        }
        return new String(buffer);
    }

    /**
     * set reverse
     * */
    public void reverse() {
        this.reverse = true;
    }

}
