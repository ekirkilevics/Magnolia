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
package info.magnolia.cms.gui.fckeditor;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

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
     * @param uuid
     * @return the document
     */
    public static Document getDocument(String uuid) {
        return (Document) getTmpFiles().get(uuid);
    }

    /**
     * Add a document to the session
     * @param doc
     * @param uuid
     */
    public static void addDocument(Document doc, String uuid) {
        getTmpFiles().put(uuid, doc);
    }

    /**
     * Remove a document.
     * @param uuid
     */
    public static void removeDocument(String uuid) {

    }

    /**
     * Get the map holding the document objects
     * @return the map
     */
    private static Map getTmpFiles() {
        Map fckTmpFiles = (Map) MgnlContext.getAttribute(ATTRIBUTE_FCK_TEMPFILES, Context.SESSION_SCOPE);
        if (fckTmpFiles == null) {
            fckTmpFiles = new HashMap();
            MgnlContext.setAttribute(ATTRIBUTE_FCK_TEMPFILES, fckTmpFiles, Context.SESSION_SCOPE);
        }
        return fckTmpFiles;
    }
}
