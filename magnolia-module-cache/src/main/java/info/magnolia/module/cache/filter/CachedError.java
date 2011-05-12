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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contract for cached errors providing access to the error codes.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CachedError implements CachedEntry, Serializable {
    private static Logger log = LoggerFactory.getLogger(CachedError.class);

    private final int statusCode;

    private String originalUrl;

    private long timestamp;

    public CachedError(int statusCode, String originalUrl) {
        this.statusCode = statusCode;
        this.originalUrl = originalUrl;
        this.timestamp = System.currentTimeMillis();
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public void replay(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!response.isCommitted()) {
            response.sendError(getStatusCode());
        } else {
            //this usually happens first time the error occurs and is put in cache - since setting page as error causes it to be committed
            // TODO: is there a better work around to make sure we do not swallow some exception accidentally?
            log.debug("Failed to serve cached error due to response already committed.");
        }
    }

    public String getOriginalURL() {
        return originalUrl;
    }

    public long getLastModificationTime() {
        return timestamp;
    }
}
