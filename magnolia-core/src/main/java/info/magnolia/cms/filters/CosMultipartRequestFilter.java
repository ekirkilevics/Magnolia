/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Path;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.oreilly.servlet.MultipartRequest;


/**
 * @author Sameer Charles
 * @version $Id$
 */
public class CosMultipartRequestFilter extends AbstractMgnlFilter {

    /**
     * Max file upload size.
     */
    private static final int MAX_FILE_SIZE = 2000000000; // 2GB

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        String type = null;
        String type1 = request.getHeader("Content-Type"); //$NON-NLS-1$
        String type2 = request.getContentType();
        if (type1 == null && type2 != null) {
            type = type2;
        }
        else if (type2 == null && type1 != null) {
            type = type1;
        }
        else if (type1 != null) {
            type = (type1.length() > type2.length() ? type1 : type2);
        }
        boolean isMultipart = (type != null) && type.toLowerCase().startsWith("multipart/form-data"); 
        if (isMultipart) { 
            MultipartForm mpf = parseParameters(request);
            request = new MultipartRequestWrapper(request, mpf);
            MgnlContext.push(request, response);
        }
        chain.doFilter(request, response);
        if (isMultipart) { 
            MgnlContext.pop();
        }
    }

    /**
     * Adds all request paramaters as request attributes.
     * @param request HttpServletRequest
     */
    private static MultipartForm parseParameters(HttpServletRequest request) throws IOException {
        MultipartForm form = new MultipartForm();
        String encoding = StringUtils.defaultString(request.getCharacterEncoding(), "UTF-8"); //$NON-NLS-1$
        MultipartRequest multi = new MultipartRequest(
            request,
            Path.getTempDirectoryPath(),
            MAX_FILE_SIZE,
            encoding,
            null);
        Enumeration params = multi.getParameterNames();
        while (params.hasMoreElements()) {
            String name = (String) params.nextElement();
            String value = multi.getParameter(name);
            form.addParameter(name, value);
            String[] s = multi.getParameterValues(name);
            if (s != null) {
                form.addparameterValues(name, s);
            }
        }
        Enumeration files = multi.getFileNames();
        while (files.hasMoreElements()) {
            String name = (String) files.nextElement();
            form.addDocument(name, multi.getFilesystemName(name), multi.getContentType(name), multi.getFile(name));
        }
        request.setAttribute(MultipartForm.REQUEST_ATTRIBUTE_NAME, form);
        return form;
    }

    static class MultipartRequestWrapper extends HttpServletRequestWrapper {

        private MultipartForm form;

        /**
         * @param request
         */
        public MultipartRequestWrapper(HttpServletRequest request, MultipartForm form) {
            super(request);
            this.form = form;
        }

        /**
         * {@inheritDoc}
         */

        public String getParameter(String name) {
            String value = form.getParameter(name);
            return value;
        }

        /**
         * {@inheritDoc}
         */

        public Map getParameterMap() {
            return form.getParameters();
        }

        /**
         * {@inheritDoc}
         */

        public Enumeration getParameterNames() {
            return form.getParameterNames();
        }

        /**
         * {@inheritDoc}
         */

        public String[] getParameterValues(String name) {
            String[] value = form.getParameterValues(name);
            return value;
        }
    }
}
