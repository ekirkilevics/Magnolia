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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import info.magnolia.cms.util.UnicodeNormalizer;

/**
 * Performs unicode normalization (NFC) for request parameters. Also URL decodes request headers.
 *
 * @author tmattsson
 * @see info.magnolia.cms.filters.UnicodeNormalizationFilter
 * @see info.magnolia.cms.util.UnicodeNormalizer
 */
public class UnicodeNormalizationRequestWrapper extends HttpServletRequestWrapper {

    public UnicodeNormalizationRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        return UnicodeNormalizer.normalizeNFC(super.getParameter(name));
    }

    @Override
    public Map getParameterMap() {
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        for (Map.Entry<String, String[]> entry : ((Map<String, String[]>) super.getParameterMap()).entrySet()) {
            parameters.put(entry.getKey(), UnicodeNormalizer.normalizeNFC(entry.getValue()));
        }
        return parameters;
    }

    @Override
    public String[] getParameterValues(String name) {
        return UnicodeNormalizer.normalizeNFC(super.getParameterValues(name));
    }

    /**
     * Introduced for MAGNOLIA-3233.
     */
    @Override
    public String getHeader(String name) {
        return urlDecodeIfPossible(super.getHeader(name));
    }

    /**
     * Introduced for MAGNOLIA-3233.
     */
    @Override
    public Enumeration getHeaders(String name) {
        Set<String> decodedHeaders = new HashSet<String>();
        Enumeration headers = super.getHeaders(name);
        if (headers != null) {
            while (headers.hasMoreElements()) {
                decodedHeaders.add(urlDecodeIfPossible((String) headers.nextElement()));
            }
        }
        return Collections.enumeration(decodedHeaders);
    }

    /**
     * Performs URL decoding using the requests character encoding if it is supported.
     */
    private String urlDecodeIfPossible(String value) {
        if (value == null)
            return null;
        try {
            return URLDecoder.decode(value, getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
