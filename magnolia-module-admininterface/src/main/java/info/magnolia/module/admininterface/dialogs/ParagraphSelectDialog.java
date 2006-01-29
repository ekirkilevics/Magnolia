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
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.ControlSuper;
import info.magnolia.cms.gui.dialog.DialogBox;
import info.magnolia.cms.gui.dialog.DialogButtonSet;
import info.magnolia.cms.gui.dialog.DialogDialog;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.dialog.DialogHidden;
import info.magnolia.cms.gui.dialog.DialogStatic;
import info.magnolia.cms.gui.dialog.DialogTab;
import info.magnolia.cms.i18n.TemplateMessagesUtil;
import info.magnolia.module.admininterface.DialogMVCHandler;

import java.io.IOException;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * If there are more than one paragraph available you have first to choose one.
 * @author philipp
 */
public class ParagraphSelectDialog extends DialogMVCHandler {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ParagraphSelectDialog.class);

    private String paragraph = StringUtils.EMPTY;

    public ParagraphSelectDialog(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        paragraph = params.getParameter("mgnlParagraph"); //$NON-NLS-1$
    }

    /**
     * @see .DialogMVCHandler#createDialog(Content, Content)
     */
    protected DialogDialog createDialog(Content configNode, Content websiteNode) throws RepositoryException {
        DialogDialog dialog = DialogFactory.getDialogDialogInstance(request, response, null, null);

        dialog.setConfig("paragraph", paragraph); //$NON-NLS-1$

        dialog.setLabel(msgs.get("dialog.paragraph.createNew")); //$NON-NLS-1$
        dialog.setConfig("saveLabel", msgs.get("buttons.ok")); //$NON-NLS-1$ //$NON-NLS-2$

        DialogHidden h1 = DialogFactory.getDialogHiddenInstance(request, response, null, null);
        h1.setName("mgnlParagraphSelected"); //$NON-NLS-1$
        h1.setValue("true"); //$NON-NLS-1$
        h1.setConfig("saveInfo", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.addSub(h1);

        DialogTab tab = dialog.addTab();

        DialogStatic c0 = DialogFactory.getDialogStaticInstance(request, response, null, null);

        c0.setConfig("line", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        c0.setValue(msgs.get("dialog.paragraph.select")); //$NON-NLS-1$
        c0.setBoxType((DialogBox.BOXTYPE_1COL));
        tab.addSub(c0);

        DialogButtonSet c1 = DialogFactory.getDialogButtonSetInstance(request, response, null, null);
        c1.setName("mgnlParagraph"); //$NON-NLS-1$
        c1.setButtonType(ControlSuper.BUTTONTYPE_RADIO);
        c1.setBoxType(DialogBox.BOXTYPE_1COL);
        c1.setConfig("saveInfo", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        c1.setConfig("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$

        String[] pars = paragraph.split(","); //$NON-NLS-1$
        for (int i = 0; i < pars.length; i++) {
            try {
                Paragraph paragraphInfo = Paragraph.getInfo(pars[i]);
                Button button = new Button(c1.getName(), paragraphInfo.getName());
                StringBuffer label = new StringBuffer();
                // TODO enable an invidual message bundle for each paragraph
                label.append("<strong>" //$NON-NLS-1$
                    + TemplateMessagesUtil.get(request, paragraphInfo.getTitle())
                    + "</strong><br />"); //$NON-NLS-1$
                label.append(TemplateMessagesUtil.get(request, paragraphInfo.getDescription()));
                label.append("<br /><br />"); //$NON-NLS-1$
                button.setLabel(label.toString());
                button.setOnclick("document.mgnlFormMain.submit();"); //$NON-NLS-1$
                c1.addOption(button);
            }
            catch (Exception e) {
                // paragraph definition does not exist
                log.warn("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }

        tab.addSub(c1);

        return dialog;
    }

    /**
     * @see info.magnolia.module.admininterface.DialogMVCHandler#getWesiteNode()
     */
    public Content getStorageNode() {
        return null;
    }

    /**
     * @see info.magnolia.module.admininterface.DialogMVCHandler#getConfigNode()
     */
    public Content getConfigNode() {
        return null;
    }

    /**
     * @see info.magnolia.module.admininterface.DialogMVCHandler#save()
     */
    public String save() {
        try {
            // copy all parameters exept mgnlDialog
            StringBuffer query = new StringBuffer();
            for (Iterator iter = form.getParameters().keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                if (!key.equals("mgnlDialog")) { //$NON-NLS-1$
                    if (query.length() != 0) {
                        query.append("&"); //$NON-NLS-1$
                    }
                    query.append(key);
                    query.append("="); //$NON-NLS-1$
                    query.append(form.getParameter(key));
                }

            }
            response.sendRedirect(request.getContextPath() + "/.magnolia/dialogs/" + this.paragraph + ".html?" + query); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (IOException e) {
            log.error("can't redirect to the paragraph-dialog", e); //$NON-NLS-1$
        }
        return VIEW_NOTHING;
    }

}