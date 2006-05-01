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

import java.util.Collection;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.ListOrderedMap;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class SearchConfigImpl implements SearchConfig {

    private OrderedMap controlDefinitions = new ListOrderedMap();

    /**
     * @return Returns the controls.
     */
    public Collection getControlDefinitions() {
        return this.controlDefinitions.values();
    }

    public void addControlDefinition(SearchControlDefinition def) {
        controlDefinitions.put(def.getName(), def); 
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchConfig#getControlDefinition(java.lang.String)
     */
    public SearchControlDefinition getControlDefinition(String name) {
        return (SearchControlDefinition) this.controlDefinitions.get(name);
    }

}
