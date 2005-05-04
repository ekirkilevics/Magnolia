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
package info.magnolia.module.admininterface.dialogs;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.DialogDialog;
import info.magnolia.cms.servlets.MVCServletHandlerImpl;
import info.magnolia.module.admininterface.DialogMVCHandler;


/**
 * Editing paragraph data
 * @author philipp 
 */
public class ParagraphEditDialog extends DialogMVCHandler {

    private static Logger log = Logger.getLogger(ParagraphEditDialog.class);

    private String paragraph = "";

    /**
     * @param request
     * @param response
     */
    public ParagraphEditDialog(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
        paragraph = params.getParameter("mgnlParagraph");
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#createDialog(info.magnolia.cms.core.Content,
     * info.magnolia.cms.core.Content)
     */
    protected DialogDialog createDialog(Content configNode, Content websiteNode) throws RepositoryException {
        DialogDialog dialog = super.createDialog(configNode, websiteNode);
        dialog.setConfig("paragraph", paragraph);
        return dialog;
    }

    /**
     * Get the configuration of the dialog from the paragraph
     * @see info.magnolia.module.admininterface.DialogMVCHandler#getConfigNode()
     **/
    protected Content getConfigNode() {
        Paragraph para = Paragraph.getInfo(paragraph);

        if (para == null) {
            //out.println(msgs.get("dialog.paragraph.paragraphNotAvailable", new String[]{paragraph}));
            log.error("paragraph not found: " + paragraph);
            return null;
        }

        return para.getDialogContent();
    }
}