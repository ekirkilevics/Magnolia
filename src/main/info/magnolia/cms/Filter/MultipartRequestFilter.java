/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */




package info.magnolia.cms.Filter;


import com.oreilly.servlet.MultipartRequest;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.util.Path;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;


/**
 * User: sameercharles
 * Date: Apr 28, 2003
 * Time: 11:20:59 AM
 * @author Sameer Charles
 * @version 1.1
 */


public class MultipartRequestFilter extends BaseFilter {


    private static final int MAX_FILE_SIZE = 200000000; // 200MB


    /**
     * default constructor
     */
    public MultipartRequestFilter() {}



    /**
     *
     *
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
                                                    throws IOException, javax.servlet.ServletException {
        HttpServletRequest request = (HttpServletRequest)req;
        String type = null;
        String type1 = request.getHeader("Content-Type");
        String type2 = request.getContentType();
        if (type1 == null && type2 != null) {
          type = type2;
        }
        else if (type2 == null && type1 != null) {
          type = type1;
        }
        else if (type1 != null && type2 != null) {
          type = (type1.length() > type2.length() ? type1 : type2);
        }

        if ((type!=null) && type.toLowerCase().startsWith("multipart/form-data")) {
            parseParameters(request);
        }
        filterChain.doFilter(req,res);
    }



    /**
     * <p>adds all request paramaters as request attribultes</p>
     *
     *
     * @param req , HttpServletRequest
     */
    private static void parseParameters(ServletRequest req) throws IOException {
        MultipartForm form = new MultipartForm();

        MultipartRequest multi = new
                MultipartRequest(req,Path.getTempDirectoryPath(),MAX_FILE_SIZE);
        Enumeration params = multi.getParameterNames();
        while (params.hasMoreElements()) {
            String name = (String)params.nextElement();
            String value = multi.getParameter(name);
            form.addParameter(name, value);
            String[] s = multi.getParameterValues(name);
            if (s != null) {
                form.addparameterValues(name,s);
            }
        }
        Enumeration files = multi.getFileNames();
        while (files.hasMoreElements()) {
            String name = (String)files.nextElement();
            form.addDocument(name,
                  multi.getFilesystemName(name),
                  multi.getContentType(name),
                  multi.getFile(name));
        }

        req.setAttribute("multipartform",form);

    }






}
