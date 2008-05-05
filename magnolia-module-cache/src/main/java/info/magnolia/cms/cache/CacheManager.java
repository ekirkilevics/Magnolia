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
package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.core.Content;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * TODO : we should probably use the AggregationState instead of the HttpServletRequest in the various methods of this
 * interface.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 *
 * @deprecated since 3.6, see the info.magnolia.module.cache package.
 */
public interface CacheManager {

    boolean cacheRequest(String key, CacheableEntry entry, boolean canCompress);

    String getCacheKey(HttpServletRequest request);

    boolean isCacheable(HttpServletRequest request);

    boolean canCompress(HttpServletRequest request);

    void flushAll();

    long getCreationTime(String key);

    void init(Content content) throws ConfigurationException;

    boolean isEnabled();

    boolean isPaused();

    boolean isRunning();

    boolean isStarted();

    void pause();

    void restart();

    void resume();

    void start();

    void stop();

    boolean streamFromCache(String key, HttpServletResponse response, boolean canCompress);
}
