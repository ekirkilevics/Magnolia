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
package info.magnolia.module.cache.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Calculates how long a shared cache may hold a response based on its response headers. The most restrictive policy
 * gets used while respecting the precedence rules dictated by RFC-2616. More specifically:
 * <ul>
 *     <li>Cache-Control: s-maxage has precedence over</li>
 *     <li>Cache-Control: max-age which in turn has precedence over</li>
 *     <li>Expires: </li>
 * </ul>
 *
 * <p>Given Cache-Control: max-age=5 and Cache-Control: max-age=15 the most restrictive is 5.</p>
 *
 * <p>Given Cache-Control: max-age=5 and Cache-Control: s-maxage=15 the latter has precedence resulting in 15.</p>
 * 
 * If either of Pragma: no-cache, Cache-Control: no-cache and Cache-Control: private is present the response is
 * considered to be already-expired.
 *
 * Uses Apache HttpClient to parse the headers.
 *
 * @version $Id$
 */
public class ResponseExpirationCalculator {

    private static class HeaderEntry {

        private final String name;
        private final Object value;
        private HeaderElement[] elements;

        public HeaderEntry(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        public HeaderElement[] getElements() {
            if (elements == null) {
                elements = HeaderElement.parseElements(value.toString());
            }
            return elements;
        }

        public String toExternalFormat() {
            return (name != null ? name : "") + ": " + (value != null ? value : "");
        }

        @Override
        public String toString() {
            return toExternalFormat();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ResponseExpirationCalculator.class);

    private final List<HeaderEntry> headers = new ArrayList<HeaderEntry>();

    public boolean addHeader(String name, Object value) {

        if ("Expires".equals(name)) {
            this.headers.add(new HeaderEntry(name, value));
            return true;
        }

        if ("Cache-Control".equals(name)) {
            this.headers.add(new HeaderEntry(name, value));
            return true;
        }

        if ("Pragma".equals(name)) {
            HeaderEntry headerEntry = new HeaderEntry(name, value);
            if (isHeaderWithElement(headerEntry, "Pragma", "no-cache")) {
                this.headers.add(headerEntry);
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the number of seconds the response can be cached where 0 means that the its already expired and must not
     * be cached and -1 means that there's no information on how long it can be cached.
     */
    public int getMaxAgeInSeconds() {

        for (HeaderEntry header : headers) {

            // Pragma no-cache as response header is no specified by HTTP but is widely used
            if (isHeaderWithElement(header, "Pragma", "no-cache")) {
                return 0;
            }

            // RFC-2616 Section 14.9.1 - [...] a cache MUST NOT use the response to satisfy a subsequent request [...]
            if (isHeaderWithElement(header, "Cache-Control", "no-cache")) {
                return 0;
            }

            // RFC-2616 Section 14.9.1 - Indicates that all or part of the response message is intended for a single user and MUST NOT be cached by a shared cache.
            if (isHeaderWithElement(header, "Cache-Control", "private")) {
                return 0;
            }
        }

        int maxAge = -1;

        // RFC-2616 Section 14.9.3 - If a response includes an s-maxage directive, then for a shared cache (but not for a
        // private cache), the maximum age specified by this directive overrides the maximum age specified by either the
        // max-age directive or the Expires header.

        for (HeaderEntry header : headers) {
            HeaderElement element = getHeaderElement(header, "Cache-Control", "s-maxage");
            if (element != null) {
                try {
                    int n = Integer.parseInt(element.getValue());
                    if (maxAge == -1 || n < maxAge) {
                        maxAge = n;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Ignoring unparseable Cache-Control header [" + header.toExternalFormat() +  "]");
                }
            }
        }

        if (maxAge != -1) {
            return maxAge;
        }

        // RFC-2616 Section 14.9.3 - If a response includes both an Expires header and a max-age directive, the max-age
        // directive overrides the Expires header, even if the Expires header is more restrictive.

        for (HeaderEntry header : headers) {
            HeaderElement element = getHeaderElement(header, "Cache-Control", "max-age");
            if (element != null) {
                try {
                    int n = Integer.parseInt(element.getValue());
                    if (maxAge == -1 || n < maxAge) {
                        maxAge = n;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Ignoring unparseable Cache-Control header [" + header.toExternalFormat() +  "]");
                }
            }
        }

        if (maxAge != -1) {
            return maxAge;
        }

        // Expires header, RFC-2616 Section 14.21

        for (HeaderEntry header : headers) {
            if ("Expires".equals(header.getName())) {

                Object value = header.getValue();

                if (value instanceof Integer) {
                    long l = (Integer) value - System.currentTimeMillis();
                    return (int) l;
                }
                if (value instanceof Long) {
                    long l = (Long) value - System.currentTimeMillis();
                    return (int) l;
                }
                if (value instanceof String) {
                    String s = (String) value;

                    // RFC2616 Section 14.21 - must treat 0 as already expired
                    if ("0".equals(s)) {
                        return 0;
                    }

                    // A http-date as specified in RFC2616 Section 3.3.1, one of three possible date formats
                    try {
                        Date expires = DateUtil.parseDate(s);
                        int n = (int) (expires.getTime() - System.currentTimeMillis());
                        if (maxAge == -1 || n < maxAge) {
                            maxAge = n;
                        }
                    } catch (DateParseException e) {
                        logger.warn("Ignoring unparseable Expires header [" + header.toExternalFormat() +  "]");
                    }
                }
            }
        }

        return maxAge;
    }

    private boolean isHeaderWithElement(HeaderEntry header, String headerName, String elementName) {
        return getHeaderElement(header, headerName, elementName) != null;
    }

    private HeaderElement getHeaderElement(HeaderEntry header, String headerName, String elementName) {
        if (headerName.equals(header.getName())) {
            HeaderElement[] elements = header.getElements();
            for (HeaderElement element : elements) {
                if (element.getName().equals(elementName)) {
                    return element;
                }
            }
        }
        return null;
    }
}
