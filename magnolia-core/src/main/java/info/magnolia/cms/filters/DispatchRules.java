/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.cms.filters;

import info.magnolia.cms.util.DispatcherType;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Similar to the dispatch mapping in the web.xml. Defines if a filter is executed on forwards,
 * includes, requests and errors.
 * @version $Id$
 *
 */
public class DispatchRules {

    private final SimpleConcurrentEnumMap<DispatcherType, DispatchRule> dispatchRules = new SimpleConcurrentEnumMap<DispatcherType, DispatchRule>(
        DispatcherType.class);

    public DispatchRules() {
        dispatchRules.put(DispatcherType.REQUEST, new DispatchRule(true, true));
        dispatchRules.put(DispatcherType.FORWARD, new DispatchRule(true, false));
        dispatchRules.put(DispatcherType.INCLUDE, new DispatchRule(false, false));
        dispatchRules.put(DispatcherType.ERROR, new DispatchRule(true, false));
    }

    public void setRequest(DispatchRule rule) {
        this.dispatchRules.put(DispatcherType.REQUEST, rule);
    }

    public void setForward(DispatchRule rule) {
        this.dispatchRules.put(DispatcherType.FORWARD, rule);
    }

    public void setInclude(DispatchRule rule) {
        this.dispatchRules.put(DispatcherType.INCLUDE, rule);
    }

    public void setError(DispatchRule rule) {
        this.dispatchRules.put(DispatcherType.ERROR, rule);
    }

    public DispatchRule getDispatchRule(DispatcherType dispatcherType) {
        return dispatchRules.get(dispatcherType);
    }

    /**
     * Simple thread-safe key-value-store for use with an enum as key. Not a complete java.util.Map
     * implementation. Optimized for read access. Faster read access than both ConcurrentHashMap and
     * EnumMap wrapped in a synchronizing wrapper.
     */
    private static class SimpleConcurrentEnumMap<K extends Enum, V> {

        private final List<V> values = new CopyOnWriteArrayList<V>();

        private SimpleConcurrentEnumMap(Class<K> keyType) {
            this(keyType, null);
        }

        public SimpleConcurrentEnumMap(Class<K> keyType, V defaultValue) {
            // Initial fill of the list to prevent IndexOutOfBoundsException later on
            values.addAll(Collections.nCopies(keyType.getEnumConstants().length, defaultValue));
        }

        public void put(K key, V value) {
            values.set(key.ordinal(), value);
        }

        public V get(K key) {
            return values.get(key.ordinal());
        }
    }

}
