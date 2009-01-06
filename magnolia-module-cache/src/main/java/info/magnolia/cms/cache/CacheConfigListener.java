/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.cache;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;


/**
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0 $Id$
 *
 * @deprecated since 3.6, cache config is using the standard module mechanism, see info.magnolia.module.cache.CacheModule
 */
class CacheConfigListener implements EventListener {

    private CacheManager cacheManager;

    private CacheConfig cacheConfig;

    protected CacheConfigListener(CacheManager cacheManager, CacheConfig cacheConfig) {
        this.cacheManager = cacheManager;
        this.cacheConfig = cacheConfig;
    }

    public void onEvent(EventIterator events) {
        try {
            this.cacheConfig.reload();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        // this.cacheManager.setConfigNode(this.content); // FIXME
        this.cacheManager.restart();
    }
}
