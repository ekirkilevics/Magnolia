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
package info.magnolia.context;

import java.util.HashMap;
import java.util.Map;


/**
 * This is a simple Map based implementation. Ignores scopes!
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractMapBasedContext extends AbstractContext {

    /**
     * The map containing the values.
     */
    private Map<String, Object> map = new HashMap<String, Object>();


    public AbstractMapBasedContext() {
    }

    public AbstractMapBasedContext(Map<String, Object> map) {
        super();
        this.map = map;
    }

    /**
     * Use the <code>Map.put()</code>.
     */
    public void setAttribute(String name, Object value, int scope) {
        this.map.put(name, value);
    }

    /**
     * Use the <code>Map.get()</code>.
     */
    public Object getAttribute(String name, int scope) {
        return this.map.get(name);
    }

    /**
     * use the <code>Map.remove()</code>.
     */
    public void removeAttribute(String name, int scope) {
        this.map.remove(name);
    }

    /**
     * Ignore scope and return the inner map.
     */
    public Map<String, Object> getAttributes(int scope) {
        return this.getAttributes();
    }

    /**
     * Returns the inner map.
     */
    public Map<String, Object> getAttributes() {
        return this.map;
    }


    public Map<String, Object> getMap() {
        return map;
    }


    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

}
