/**
 * This file Copyright (c) 2008-2009 Magnolia International
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
package info.magnolia.module.cache.ehcache;

import info.magnolia.module.cache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class EhCacheWrapper implements Cache {
    private final Ehcache ehcache;

    public EhCacheWrapper(Ehcache ehcache) {
        this.ehcache = ehcache;
    }

    public Object get(Object key) {
        final Element element = ehcache.get(key);
        return element != null ? element.getObjectValue() : null;
    }

    public boolean hasElement(Object key) {
        // we can't use isKeyInCache(), as it does not check for the element's expiration
        // which may lead to unexpected results, since get() and getQuiet() do check
        // for expiration and return null if the element was expired.
        // return ehcache.isKeyInCache(key);

        // we can't use getQuiet, as it's non-blocking
        return ehcache.get(key) != null;
    }

    public void put(Object key, Object value) {
        final Element element = new Element(key, value);
        ehcache.put(element);
    }

    public void remove(Object key) {
        ehcache.remove(key);
    }

    public void clear() {
        ehcache.removeAll();
    }

    public Ehcache getWrappedEhcache() {
        return ehcache;
    }

}
