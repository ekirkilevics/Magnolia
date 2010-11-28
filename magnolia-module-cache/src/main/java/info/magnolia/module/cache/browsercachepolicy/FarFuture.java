/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.cache.browsercachepolicy;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.cache.BrowserCachePolicyResult;
import info.magnolia.module.cache.CachePolicyResult;


/**
 * Uses the *.identifier.cache.ext pattern to determine if a file can be cached for ever.
 * @author pbaerfuss
 * @version $Id$
 *
 */
public class FarFuture extends FixedDuration {

    public FarFuture() {
        // a year
        setExpirationMinutes(60*24*365);
    }

    public BrowserCachePolicyResult canCacheOnClient(CachePolicyResult cachePolicyResult) {
        String uri = MgnlContext.getAggregationState().getOriginalBrowserURI();
        if(isFarFutureURI(uri)){
            return super.canCacheOnClient(cachePolicyResult);
        }
        return BrowserCachePolicyResult.NO_CACHE;
    }

    private boolean isFarFutureURI(String uri) {
        return uri.matches(".*\\.cache\\.[^\\.]*");
    }

}
