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
package info.magnolia.cms.filters;

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Path;
import info.magnolia.context.MgnlContext;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A <code>Filter</code> that determines if a <code>HttpServletRequest</code> contains multipart content and if so
 * parses it into a request attribute for further processing. This implementation uses jakarta commons-fileupload for
 * parsing multipart requests. Maximum file size can be configured using the "maxFileSize" init parameter, defaulting to
 * 2 GB.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class MultipartRequestFilter extends AbstractMgnlFilter {
    private static final Logger log = LoggerFactory.getLogger(MultipartRequestFilter.class);

    /**
     * Default max file upload size (2 GB).
     */
    private static final int DEFAULT_MAX_FILE_SIZE = 2000000000; // 2GB

    /**
     * Config parameter name for max file size.
     */
    private static final String PARAM_MAX_FILE_SIZE = "maxFileSize";

    /**
     * The maximum size a file upload may have.
     */
    private long maxFileSize = DEFAULT_MAX_FILE_SIZE;

    /**
     * The directory for temporary storage of uploaded files.
     */
    private File tempDir;

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
        super.init(config);
        String maxFileSize = config.getInitParameter(PARAM_MAX_FILE_SIZE);
        if (maxFileSize != null) {
            this.maxFileSize = Long.parseLong(maxFileSize);
        }

        this.tempDir = new File(Path.getTempDirectoryPath());
    }

    /**
     * Determine if the request has multipart content and if so parse it into a <code>MultipartForm</code> and store
     * it as a request attribute.
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        boolean isMultipartContent = FileUploadBase.isMultipartContent(new ServletRequestContext(request));
        if (isMultipartContent) {
            MultipartForm mpf = parseRequest(request);
            // wrap the request
            request = new MultipartRequestWrapper(request, mpf);
            MgnlContext.push(request, response);
        }
        chain.doFilter(request, response);
        if(isMultipartContent){
            MgnlContext.pop();
        }
    }

    /**
     * Parse the request and store it as a request attribute.
     */
    private MultipartForm parseRequest(HttpServletRequest request) throws IOException, ServletException {
        MultipartForm form = new MultipartForm();

        ServletFileUpload upload = newServletFileUpload();

        final List fileItems;
        try {
            fileItems = upload.parseRequest(request);
        } catch (FileUploadException e) {
            throw new ServletException("Could not upload files:" + e.getMessage(), e);
        }

        for (Iterator fileItemIterator = fileItems.iterator(); fileItemIterator.hasNext();) {
            FileItem item = (FileItem) fileItemIterator.next();
            if (item.isFormField()) {
                addField(request, item, form);
            }
            else {
                addFile(item, form);
            }
        }

        request.setAttribute(MultipartForm.REQUEST_ATTRIBUTE_NAME, form);
        return form;
    }

    /**
     * Create a new <code>DiskFileUpload</code>.
     */
    private ServletFileUpload newServletFileUpload() {
        ServletFileUpload upload = new ServletFileUpload();
        upload.setSizeMax(this.maxFileSize);
        DiskFileItemFactory fif = new DiskFileItemFactory();
        fif.setRepository(Path.getTempDirectory());
        upload.setFileItemFactory(fif);

        return upload;
    }

    /**
     * Add the <code>FileItem</code> as a paramter into the <code>MultipartForm</code>.
     */
    private void addField(HttpServletRequest request, FileItem item, MultipartForm form) {
        String name = item.getFieldName();

        String value;
        try {
            String encoding = StringUtils.defaultString(request.getCharacterEncoding(), "UTF-8");
            value = item.getString(encoding);
        }
        catch (UnsupportedEncodingException ex) {
            value = item.getString();
        }

        form.addParameter(name, value);

        String[] values = form.getParameterValues(name);
        if (values == null) {
            form.addparameterValues(name, new String[]{value});
        }
        else {
            form.addparameterValues(name, (String[]) ArrayUtils.add(values, value));
        }
    }

    /**
     * Add the <code>FileItem</code> as a document into the <code>MultipartForm</code>.
     */
    private void addFile(FileItem item, MultipartForm form) throws IOException {
        String atomName = item.getFieldName();
        String fileName = item.getName();
        String type = item.getContentType();
        File file = File.createTempFile(atomName, null, this.tempDir);
        try {
            item.write(file);
        } catch (Exception e) {
            log.error("Could not write uploaded file: " + e.getMessage(), e);
            throw new IOException("Could not write uploaded file: " + e.getMessage());
        }

        form.addDocument(atomName, fileName, type, file);
    }

    static class MultipartRequestWrapper extends HttpServletRequestWrapper {
        private final MultipartForm form;

        public MultipartRequestWrapper(HttpServletRequest request, MultipartForm form) {
            super(request);
            this.form = form;
        }

        /**
         * {@inheritDoc}
         */
        public String getParameter(String name) {
            String value = form.getParameter(name);
            log.debug("getParameter: {}={}", name, value);
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
            log.debug("getParameterValues: {}={}", name, value);
            return value;
        }

    }
}
