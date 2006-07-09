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

package info.magnolia.module.dms;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.dms.beans.Document;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * Servlet to handle document downloads.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DMSDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 222L;

    private static Logger log = Logger.getLogger(DMSDownloadServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            process(request, response);
        }
        catch (Exception e) {
            log.error("error during download", e);
        }
    }

    /**
     * Get the requested resource and copy it to the ServletOutputStream, bit by bit.
     * @param req HttpServletRequest as given by the servlet container
     * @param res HttpServletResponse as given by the servlet container
     * @param doc
     * @throws IOException standard servlet exception
     */
    private void handleResourceRequest(HttpServletRequest req, HttpServletResponse res, Document doc) throws Exception {
        InputStream is = doc.getFileStream();
        res.setContentLength((int) doc.getFileSize());

        if (is != null) {
            // todo always send as is, find better way to discover if resource could be compressed
            sendUnCompressed(is, res);
            is.close();
            return;
        }
    }

    private void sendUnCompressed(java.io.InputStream is, HttpServletResponse res) throws Exception {
        ServletOutputStream os = res.getOutputStream();
        byte[] buffer = new byte[8192];
        int read = 0;
        while ((read = is.read(buffer)) > 0) {
            os.write(buffer, 0, read);
        }
        os.flush();
        os.close();
    }

    public void process(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HierarchyManager hm = MgnlContext.getHierarchyManager(DMSModule.getInstance().getRepository());
        Document doc;
        String versionName = request.getParameter("mgnlVersion");

        // pass all the informations to the ResourceDispatcher
        String path = StringUtils.substringAfter(request.getRequestURI(), request.getContextPath());
        path = StringUtils.substringBeforeLast(path, ".");
        if (path.startsWith("/dms/")) {
            path = path.replaceFirst("/dms/", "/");

            // check if there is a name appended (not the nodename)
            if (!hm.isExist(path)) {
                if (hm.isExist(StringUtils.substringBeforeLast(path, "/"))) {
                    path = StringUtils.substringBeforeLast(path, "/");
                    doc = new Document(hm.getContent(path), versionName);
                }
                else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }

            // redirect so that the nice filename is shown
            else {
                doc = new Document(hm.getContent(path), versionName);
                String url = request.getRequestURI() + "/" + doc.getEncodedFileName();
                if (StringUtils.isNotEmpty(versionName)) {
                    url += "?mgnlVersion=" + versionName;
                }
                response.sendRedirect(url);
            }
        }
        else if (path.startsWith("/dms-static/")) {
            String uuid = path.replaceFirst("/dms-static/", "");
            uuid = StringUtils.substringBefore(uuid, "/");
            Content node;
            try {
                node = hm.getContentByUUID(uuid);
            }
            catch (Exception e) {
                log.error("can't find file by uuid [" + uuid + "]", e);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            doc = new Document(node, versionName);
        }
        else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // set mime/type
        response.setContentType(doc.getMimeType());
        response.setHeader("Content-Disposition", "attachment; filename=");

        // stream the file
        handleResourceRequest(request, response, doc);
    }

}
