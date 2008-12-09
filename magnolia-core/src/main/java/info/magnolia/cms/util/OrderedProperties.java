/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;

/**
 * Subclass of java.util.Properties which keeps the order in which properties were loaded.
 *
 * <strong>Warning:</strong> only the java.util.Map interface methods have been
 * overloaded, so be weary when using java.util.Properties specific methods. (load, save,
 * getProperty and setProperty are working.) (getProperty had to be explicitely overloaded
 * too)
 *
 * @author philipp
 * @version $Id:  $
 */
public class OrderedProperties extends Properties {
    private final LinkedHashMap map = new LinkedHashMap();

    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    public Object get(Object key) {
        return map.get(key);
    }

    public String getProperty(String key) {
        return (String) get(key);
    }

    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    public Set entrySet() {
        return this.map.entrySet();
    }

    public Set keySet() {
        return this.map.keySet();
    }

    public Collection values() {
        return this.map.values();
    }

    public int size() {
        return this.map.size();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }
}
