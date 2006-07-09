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
package info.magnolia.cms.gui.controlx.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class SelectSearchControlDefinition extends SearchControlDefinition {

    /**
     * @param name
     * @param label
     */
    public SelectSearchControlDefinition(String name, String label) {
        super(name, label, "select");
    }

    // array
    public OrderedMap options = new ListOrderedMap();

    public String getJsField() {

        List pairs = new ArrayList();
        for (MapIterator iter = this.getOptions().orderedMapIterator(); iter.hasNext();) {
            iter.next();
            String key = (String) iter.getKey();
            String value = (String) iter.getValue();
            pairs.add("'" + key + "': '" + value + "'");
        }

        String str = super.getJsField();
        str = StringUtils.removeEnd(str, "}");
        str += ",options: {";
        str += StringUtils.join(pairs.iterator(), ",");
        str += "}}";
        return str;
    }

    /**
     * @return Returns the options.
     */
    public OrderedMap getOptions() {
        return this.options;
    }

    public void addOption(String value, String label) {
        this.getOptions().put(value, label);
    }
}
