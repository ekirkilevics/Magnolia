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
 * Cached redirect contract providing access to the status code and target location.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CachedRedirect implements CachedEntry, Serializable {

    private final int statusCode;
    private final String location;
    private long timestamp;
    private String originalUrl;
    private int timeToLiveInSeconds = -1;

    public CachedRedirect(int statusCode, String location, String originalUrl, int timeToLiveInSeconds) {
        this.statusCode = statusCode;
        this.location = location;
        this.originalUrl = originalUrl;
        this.timestamp = System.currentTimeMillis();
        this.timeToLiveInSeconds = timeToLiveInSeconds;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public void replay(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // we'll ignore the redirection code for now - especially since the servlet api doesn't really let us choose anyway
        // except if someone sets the header manually ?
        if (!response.isCommitted()) {
            response.sendRedirect(getLocation());
        }
    }

    @Override
    public String getOriginalURL() {
        return originalUrl;
    }

    @Override
    public long getLastModificationTime() {
        return timestamp;
    }

    @Override
    public int getTimeToLiveInSeconds() {
        return timeToLiveInSeconds;
    }
}
