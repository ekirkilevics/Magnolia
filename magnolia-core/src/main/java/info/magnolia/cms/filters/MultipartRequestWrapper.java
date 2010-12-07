/**
 * This file Copyright (c) 2010 Magnolia International
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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import info.magnolia.cms.beans.runtime.MultipartForm;

/**
 * HttpServletRequestWrapper that adds additional parameters passed in using multipart submit. The added parameters are
 * taken from a {@link info.magnolia.cms.beans.runtime.MultipartForm} object. The wrapper is applied in the filter chain
 * either by {@link info.magnolia.cms.filters.CosMultipartRequestFilter} or
 * {@link info.magnolia.cms.filters.MultipartRequestFilter}.
 *
 * @author tmattsson
 * @see info.magnolia.cms.beans.runtime.MultipartForm
 * @see info.magnolia.cms.filters.MultipartRequestFilter
 * @see info.magnolia.cms.filters.CosMultipartRequestFilter
 */
public class MultipartRequestWrapper extends HttpServletRequestWrapper {

    private final MultipartForm form;

    public MultipartRequestWrapper(HttpServletRequest request, MultipartForm form) {
        super(request);
        this.form = form;
    }

    public String getParameter(String name) {
        String value = form.getParameter(name);
        if (value == null)
            value = super.getParameter(name);
        return value;
    }

    public Map getParameterMap() {
        Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        parameterMap.putAll(super.getParameterMap());
        parameterMap.putAll(form.getParameters());
        return parameterMap;
    }

    public Enumeration getParameterNames() {
        Set<String> parameterNames = new HashSet<String>(form.getParameters().keySet());
        Enumeration enumeration = super.getParameterNames();
        while (enumeration.hasMoreElements())
            parameterNames.add((String) enumeration.nextElement());
        return Collections.enumeration(parameterNames);
    }

    public String[] getParameterValues(String name) {
        String[] value = form.getParameterValues(name);
        if (value == null)
            value = super.getParameterValues(name);
        return value;
    }
}
