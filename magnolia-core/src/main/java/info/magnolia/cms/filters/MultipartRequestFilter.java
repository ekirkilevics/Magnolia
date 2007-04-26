/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.filters;

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Path;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.oreilly.servlet.MultipartRequest;


/**
 * @author Sameer Charles
 * @version $Id$
 */
public class MultipartRequestFilter extends AbstractMagnoliaFilter {

    /**
     * Max file upload size.
     */
    private static final int MAX_FILE_SIZE = 2000000000; // 2GB

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException{

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
        if ((type != null) && type.toLowerCase().startsWith("multipart/form-data")) { //$NON-NLS-1$
            parseParameters(request);
        }
        chain.doFilter(request, response);
    }

    /**
     * Adds all request paramaters as request attributes.
     * @param request HttpServletRequest
     */
    private static void parseParameters(HttpServletRequest request) throws IOException {
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
    }
}
