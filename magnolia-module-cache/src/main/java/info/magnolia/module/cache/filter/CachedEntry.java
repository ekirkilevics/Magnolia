/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.module.cache.filter;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A cached entry used to store content produced by the filter chain. The cache can be replayed.
 * @version $Revision: $ ($Author: $)
 */
public interface CachedEntry extends Serializable {

    /**
     * @param chain a cache entry might want to delegate to the filter chain, see {@link DelegatingBlobCachedEntry#replay(HttpServletRequest, HttpServletResponse, FilterChain)}
     */
    void replay(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;
    
    /**
     * Entry might be required to produce url used to create it in the first place.
     * @return URL that triggered cache entry creation.
     */
    String getOriginalURL();

    /**
     * Produce last modification date of the cache entry. If no other time can be discerned, the time of entry creation should be returned.
     * @return time when cached entry was last modified. This time should reflect real modification time of the cached content.
     */
    public long getLastModificationTime();
}
