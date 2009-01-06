/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import java.util.Hashtable;
import java.util.Map;

public class MapAttributeStrategy implements AttributeStrategy {

    private static final long serialVersionUID = 222L;

    private Map map = new Hashtable();

    public MapAttributeStrategy() {
    }

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


    public Map getMap() {
        return map;
    }


    public void setMap(Map map) {
        this.map = map;
    }

}
