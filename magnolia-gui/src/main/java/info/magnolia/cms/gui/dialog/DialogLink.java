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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.i18n.MessagesManager;


/**
 * @author Vinzenz Wyser
 * @version $Revision$ ($Author$)
 */
public class DialogLink extends DialogEditWithButton {

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogLink() {
    }

    /**
     * Customize the dialog.
     * @see info.magnolia.cms.gui.dialog.DialogEditWithButton#doBeforeDrawHtml()
     */
    protected void doBeforeDrawHtml() {
        super.doBeforeDrawHtml();
        
        String extension = this.getConfigValue("extension"); //$NON-NLS-1$
        String label = MessagesManager.get(this.getRequest(), "dialog.link.internal"); //$NON-NLS-1$
        this.getButton().setLabel(label);
        this.getButton().setSaveInfo(false);
        String repository = this.getConfigValue("repository", ContentRepository.WEBSITE); //$NON-NLS-1$
        this.getButton().setOnclick(
            "mgnlDialogLinkOpenBrowser('" + this.getName() + "','" + repository + "','" + extension + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    }
}
