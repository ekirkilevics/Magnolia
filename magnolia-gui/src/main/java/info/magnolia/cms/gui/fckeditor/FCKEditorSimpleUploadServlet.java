/**
 * FCKeditor - The text editor for internet
 * Copyright (C) 2003-2005 Frederico Caldeira Knabben
 * 
 * Licensed under the terms of the GNU Lesser General Public License:
 * 		http://www.opensource.org/licenses/lgpl-license.php
 * 
 * For further information visit:
 * 		http://www.fckeditor.net/
 * 
 * File Name: SimpleUploaderServlet.java
 * 	Java File Uploader class.
 * 
 * Version:  2.3
 * Modified: 2005-08-11 16:29:00
 * 
 * File Authors:
 * 		Simone Chiaretta (simo@users.sourceforge.net)
 */

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
package info.magnolia.cms.gui.fckeditor;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.util.RequestFormUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.doomdark.uuid.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Servlet to upload files. This is based on the SimpleUploaderServlet of the FCKeditor connector package<br>
 * This servlet accepts just file uploads, eventually with a parameter specifying file type
 * @author Simone Chiaretta (simo@users.sourceforge.net)
 * @author Philipp Bracher
 */

public class FCKEditorSimpleUploadServlet extends HttpServlet {

    private static final long serialVersionUID = -8512828615271068088L;

    private static Logger log = LoggerFactory.getLogger(FCKEditorSimpleUploadServlet.class);

    private static Hashtable allowedExtensions;

    private static Hashtable deniedExtensions;

    /**
     * Initialize the servlet.<br>
     * Retrieve from the servlet configuration the "baseDir" which is the root of the file repository:<br>
     * If not specified the value of "/UserFiles/" will be used.<br>
     * Also it retrieve all allowed and denied extensions to be handled.
     */
    public void init() throws ServletException {
        allowedExtensions = new Hashtable(3);
        deniedExtensions = new Hashtable(3);

        allowedExtensions.put("file", stringToArrayList(getInitParameter("AllowedExtensionsFile")));
        deniedExtensions.put("file", stringToArrayList(getInitParameter("DeniedExtensionsFile")));

        allowedExtensions.put("image", stringToArrayList(getInitParameter("AllowedExtensionsImage")));
        deniedExtensions.put("image", stringToArrayList(getInitParameter("DeniedExtensionsImage")));

        allowedExtensions.put("flash", stringToArrayList(getInitParameter("AllowedExtensionsFlash")));
        deniedExtensions.put("flash", stringToArrayList(getInitParameter("DeniedExtensionsFlash")));
    }

    /**
     * Manage the Upload requests.<br>
     * The servlet accepts commands sent in the following format:<br>
     * simpleUploader?Type=ResourceType<br>
     * <br>
     * It store the file (renaming it in case a file with the same name exists) and then return an HTML file with a
     * javascript command in it.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter out = response.getWriter();

        String typeStr = request.getParameter("type");

        String retVal = "0";
        String newName = "";
        String fileUrl = "";
        String errorMessage = "";

        RequestFormUtil form = new RequestFormUtil(request);

        Document doc = form.getDocument("NewFile");

        if (extIsAllowed(typeStr, doc.getExtension())) {

            try {
                // now copy the files to the special fck tmp folder
                String uuid = UUIDGenerator.getInstance().generateTimeBasedUUID().toString();
                FileUtils.copyFileToDirectory(doc.getFile(), new File(Path.getTempDirectoryPath()
                    + "/fckeditor/"
                    + uuid));
                doc.delete();

                // the document will now point to the copied file
                doc = new Document(new File(Path.getTempDirectoryPath()
                    + "/fckeditor/"
                    + uuid
                    + "/"
                    + doc.getFile().getName()), doc.getType());

                // save it to the session
                FCKEditorTmpFiles.addDocument(request, doc, uuid);

                // make the temporary url ready for the editor
                fileUrl = request.getContextPath() + "/tmp/fckeditor/" + uuid + "/" + doc.getFile().getName();

            }
            catch (Exception ex) {
                log.error("can't upload the file", ex);
                retVal = "203";
            }

        }
        else {
            log.info("Tried to upload a not allowed file [" + doc.getFileNameWithExtension() + "]");
            retVal = "202";
            errorMessage = "";
        }

        out.println("<script type=\"text/javascript\">");
        out.println("window.parent.OnUploadCompleted("
            + retVal
            + ",'"
            + fileUrl
            + "','"
            + newName
            + "','"
            + errorMessage
            + "');");
        out.println("</script>");
        out.flush();
        out.close();
    }

    /**
     * Helper function to convert the configuration string to an ArrayList.
     */

    private ArrayList stringToArrayList(String str) {
        String[] strArr = str.split("\\|");

        ArrayList tmp = new ArrayList();
        if (str.length() > 0) {
            for (int i = 0; i < strArr.length; ++i) {
                tmp.add(strArr[i].toLowerCase());
            }
        }
        return tmp;
    }

    /**
     * Helper function to verify if a file extension is allowed or not allowed.
     */

    private boolean extIsAllowed(String fileType, String ext) {

        ext = ext.toLowerCase();

        ArrayList allowList = (ArrayList) allowedExtensions.get(fileType);
        ArrayList denyList = (ArrayList) deniedExtensions.get(fileType);

        if (allowList.size() == 0)
            if (denyList.contains(ext))
                return false;
            else
                return true;

        if (denyList.size() == 0)
            if (allowList.contains(ext))
                return true;
            else
                return false;

        return false;
    }
}
