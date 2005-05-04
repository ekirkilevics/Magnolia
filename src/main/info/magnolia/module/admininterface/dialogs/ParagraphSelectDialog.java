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


/**
 * If there are more than one paragraph available you have first to choose one.
 * @author philipp
 */
public class ParagraphSelectDialog extends DialogMVCHandler {

    private static Logger log = Logger.getLogger(ParagraphSelectDialog.class);

    private String paragraph = "";

    /**
     * @param request
     * @param response
     */
    public ParagraphSelectDialog(HttpServletRequest request, HttpServletResponse response) {
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
        // multiple paragraphs: show selection dialog
        dialog = DialogFactory.getDialogDialogInstance(request, response, null, null);

        dialog.setConfig("paragraph", paragraph);

        dialog.setLabel(msgs.get("dialog.paragraph.createNew"));
        dialog.setConfig("saveLabel", msgs.get("buttons.ok"));

        DialogHidden h1 = DialogFactory.getDialogHiddenInstance(request, response, null, null);
        h1.setName("mgnlParagraphSelected");
        h1.setValue("true");
        h1.setConfig("saveInfo", "false");
        dialog.addSub(h1);

        DialogTab tab = dialog.addTab();

        DialogStatic c0 = DialogFactory.getDialogStaticInstance(request, response, null, null);

        c0.setConfig("line", "false");
        c0.setValue(msgs.get("dialog.paragraph.select"));
        c0.setBoxType((DialogBox.BOXTYPE_1COL));
        tab.addSub(c0);

        DialogButtonSet c1 = DialogFactory.getDialogButtonSetInstance(request, response, null, null);
        c1.setName("mgnlParagraph");
        c1.setButtonType(ControlSuper.BUTTONTYPE_RADIO);
        c1.setBoxType(DialogBox.BOXTYPE_1COL);
        c1.setConfig("saveInfo", "false");
        c1.setConfig("width", "100%");

        String[] pars = paragraph.split(",");
        for (int i = 0; i < pars.length; i++) {
            try {
                Paragraph paragraphInfo = Paragraph.getInfo(pars[i]);
                Button button = new Button(c1.getName(), paragraphInfo.getName());
                StringBuffer label = new StringBuffer();
                label.append("<strong>"
                    + TemplateMessagesUtil.get(request, paragraphInfo.getTitle())
                    + "</strong><br />");
                label.append(TemplateMessagesUtil.get(request, paragraphInfo.getDescription()));
                label.append("<br /><br />");
                button.setLabel(label.toString());
                button.setOnclick("document.mgnlFormMain.submit();");
                c1.addOption(button);
            }
            catch (Exception e) {
                // paragraph definition does not exist
                log.warn("Exception caught: " + e.getMessage(), e);
            }
        }

        tab.addSub(c1);

        return dialog;
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#getWesiteNode()
     */
    protected Content getWesiteNode() {
        return null;
    }

    protected Content getConfigNode() {
        return null;
    }

}