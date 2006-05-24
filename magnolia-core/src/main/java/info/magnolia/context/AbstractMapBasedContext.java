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
package info.magnolia.context;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a simple Map based implementation. Ignores scopes!
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public abstract class AbstractMapBasedContext extends AbstractContext {
    
    /**
     * The map containing the values
     */
    private Map map = new HashMap();
    
    /**
     * Use the Map.put()
     */
    public void setAttribute(String name, Object value, int scope) {
        this.map.put(name, value);
    }
    
    /**
     * Use the Map.get()
     */
    public Object getAttribute(String name, int scope) {
        return this.map.get(name);
    }
    
    /**
     * use the Map.remove()
     */
    public void removeAttribute(String name, int scope) {
        this.map.remove(name);
    }
    
    /**
     * Ignore scope and return the inner map
     */
    public Map getAttributes(int scope) {
        return this.getAttributes();
    }
    
    /**
     * Returns the inner map
     */
    public Map getAttributes() {
        return this.map;
    }

}
