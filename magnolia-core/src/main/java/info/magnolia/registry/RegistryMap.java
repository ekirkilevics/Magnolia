/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.registry;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Thread safe map intended to be used for registries. Provides an atomic operation <code>removeAndPutAll</code> that is
 * used to remove a set of previously added values before adding a collection of new ones. Read operations are blocked
 * until it completes guaranteeing proper visibility.
 * <p/>
 * It is common for entities in registries to also hold their identifier. The method <code>keyFormValue</code> can be
 * overridden to get the identifier from the value. This removes the need to package a set of entities that should be
 * added in a Map before calling {@link #removeAndPutAll(java.util.Collection, java.util.Map)}.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @version $Id$
 */
public class RegistryMap<K, V> {

    private final Map<K, V> map = new HashMap<K, V>();

    public synchronized V get(K key) {
        return map.get(key);
    }

    public synchronized V getRequired(K key) throws RegistrationException {
        V value = map.get(key);
        if (value == null) {
            throw new RegistrationException("Entry for [" + key + "] not found in registry, available entries are: " +
                    (map.isEmpty() ? "<none>" : StringUtils.join(map.keySet(), ", ")));
        }
        return value;
    }

    public synchronized void put(K key, V value) {
        map.put(key, value);
    }

    public synchronized void put(V value) {
        map.put(keyFromValue(value), value);
    }

    public synchronized void remove(K key) {
        map.remove(key);
    }

    public synchronized void removeAndPutAll(Collection<K> toRemove, Map<K, V> toPut) {
        // Fail early if the keyFromValue method hasn't been overridden
        if (!toPut.isEmpty()) {
            keyFromValue(toPut.values().iterator().next());
        }
        for (K key : toRemove) {
            map.remove(key);
        }
        map.putAll(toPut);
    }

    public synchronized Set<K> removeAndPutAll(Collection<K> toRemove, Collection<V> toPut) {
        if (!toPut.isEmpty()) {
            keyFromValue(toPut.iterator().next());
        }
        for (K key : toRemove) {
            map.remove(key);
        }
        HashSet<K> keys = new HashSet<K>();
        for (V value : toPut) {
            K key = keyFromValue(value);
            map.put(key, value);
            keys.add(key);
        }
        return keys;
    }

    public synchronized Collection<K> keySet() {
        return new ArrayList<K>(map.keySet());
    }

    public synchronized Collection<V> values() {
        return new ArrayList<V>(map.values());
    }

    protected K keyFromValue(V value) {
        throw new UnsupportedOperationException("keyFromValue");
    }
}
