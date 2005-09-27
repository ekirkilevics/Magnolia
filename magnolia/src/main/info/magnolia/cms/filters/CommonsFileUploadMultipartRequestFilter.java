/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.filters;

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Path;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.lang.StringUtils;


/**
 * A <code>Filter</code> that determines if a <code>HttpServletRequest</code> contains multipart content and if so
 * parses it into a request attribute for further processing.
 * @author Andreas Brenk
 * @version $Id$
 */
public class CommonsFileUploadMultipartRequestFilter implements Filter {

    /**
     * Default max file upload size (200 MB).
     */
    private static final int DEFAULT_MAX_FILE_SIZE = 209715200;

    /**
     * Config parameter name for max file size.
     */
    private static final String PARAM_MAX_FILE_SIZE = "maxFileSize";

    /**
     * The maximum size a file upload may have.
     */
    private int maxFileSize = DEFAULT_MAX_FILE_SIZE;

    /**
     * The directory for temporary storage of uploaded files.
     */
    private File tempDir;

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
        String maxFileSize = config.getInitParameter(PARAM_MAX_FILE_SIZE);
        if (maxFileSize != null) {
            this.maxFileSize = Integer.parseInt(maxFileSize);
        }

        this.tempDir = new File(Path.getTempDirectoryPath());
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // unused
    }

    /**
     * Determine if the request has multipart content and if so parse it into a <code>MultipartForm</code> and store
     * it as a request attribute.
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {
        if (request instanceof HttpServletRequest) {
            boolean isMultipartContent = FileUploadBase.isMultipartContent((HttpServletRequest) request);
            if (isMultipartContent) {
                try {
                    parseRequest((HttpServletRequest) request);
                }
                catch (IOException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new ServletException(e);
                }
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Parse the request and store it as a request attribute.
     */
    private void parseRequest(HttpServletRequest request) throws Exception {
        MultipartForm form = new MultipartForm();

        DiskFileUpload upload = newDiskFileUpload();
        List fileItems = upload.parseRequest(request);

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
    }

    /**
     * Create a new <code>DiskFileUpload</code>.
     */
    private DiskFileUpload newDiskFileUpload() {
        DiskFileUpload upload = new DiskFileUpload();
        upload.setSizeMax(this.maxFileSize);
        upload.setRepositoryPath(Path.getTempDirectoryPath());

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

        String[] values = request.getParameterValues(name);
        if (values != null) {
            form.addparameterValues(name, values);
        }
    }

    /**
     * Add the <code>FileItem</code> as a document into the <code>MultipartForm</code>.
     */
    private void addFile(FileItem item, MultipartForm form) throws Exception {
        String atomName = item.getFieldName();
        String fileName = item.getName();
        String type = item.getContentType();
        File file = File.createTempFile(atomName, null, this.tempDir);
        item.write(file);

        form.addDocument(atomName, fileName, type, file);
    }
}
