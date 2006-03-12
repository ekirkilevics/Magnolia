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
import info.magnolia.cms.beans.runtime.MgnlContext;

import java.util.HashMap;
import java.util.Map;


/**
 * This class handles the uploaded files for the fckeditor. The editor uses the FCKEditoSimpleUploadServlet to upload
 * the files. The files are stored in the tmp directory until the dialog gets saved. For each file is the Document
 * object saved in the session.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class FCKEditorTmpFiles {

    /**
     * Used to save the map in the session
     */
    private static final String ATTRIBUTE_FCK_TEMPFILES = "info.magnolia.cms.gui.fckeditor.tmpfiles";

    /**
     * Get a saved document
     * @param request
     * @param uuid
     * @return the document
     */
    public static Document getDocument(String uuid) {
        return (Document) getTmpFiles().get(uuid);
    }

    /**
     * Add a document to the session
     * @param request
     * @param doc
     * @param uuid
     */
    public static void addDocument(Document doc, String uuid) {
        getTmpFiles().put(uuid, doc);
    }

    /**
     * Remove a document.
     * @param request
     * @param uuid
     */
    public static void removeDocument(String uuid) {

    }

    /**
     * Get the map holding the document objects
     * @param request
     * @return the map
     */
    private static Map getTmpFiles() {
        Map fckTmpFiles = (Map) MgnlContext.getAttribute(ATTRIBUTE_FCK_TEMPFILES, MgnlContext.SESSION_SCOPE);
        if (fckTmpFiles == null) {
            fckTmpFiles = new HashMap();
            MgnlContext.setAttribute(ATTRIBUTE_FCK_TEMPFILES, fckTmpFiles, MgnlContext.SESSION_SCOPE);
        }
        return fckTmpFiles;
    }
}
