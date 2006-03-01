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
package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.DialogDialog;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Editing paragraph data
 * @author philipp
 */
public class ParagraphEditDialog extends ConfiguredDialog {

    private static Logger log = LoggerFactory.getLogger(ParagraphEditDialog.class);

    private String paragraph = StringUtils.EMPTY;

    public ParagraphEditDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);
        paragraph = params.getParameter("mgnlParagraph"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#createDialog(info.magnolia.cms.core.Content,
     * info.magnolia.cms.core.Content)
     */
    protected DialogDialog createDialog(Content configNode, Content websiteNode) throws RepositoryException {
        DialogDialog dialog = super.createDialog(configNode, websiteNode);
        dialog.setConfig("paragraph", paragraph); //$NON-NLS-1$
        return dialog;
    }

    /**
     * Get the configuration of the dialog from the paragraph
     */
    public static Content getConfigNode(HttpServletRequest request, String paragraph) {
        Paragraph para = Paragraph.getInfo(paragraph);

        if (para == null) {
            // out.println(msgs.get("dialog.paragraph.paragraphNotAvailable", new String[]{paragraph}));
            log.error("paragraph not found: " + paragraph); //$NON-NLS-1$
            return null;
        }

        // @todo FIXME! this should return the dialog node
        return null;
    }

    /**
     * @return Returns the paragraph.
     */
    public String getParagraph() {
        return paragraph;
    }
}