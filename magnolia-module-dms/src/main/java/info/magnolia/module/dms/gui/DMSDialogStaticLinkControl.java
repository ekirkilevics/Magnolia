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
package info.magnolia.module.dms.gui;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.DialogStatic;
import info.magnolia.module.dms.beans.Document;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Renders the static link useable for download.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DMSDialogStaticLinkControl extends DialogStatic {

    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {

        super.init(request, response, websiteNode, configNode);
        if (websiteNode != null) {
            Document doc = Document.getCurrent(request);
            this.setValue(doc.getStaticLink());
        }
    }
}
