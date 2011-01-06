/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.ControlImpl;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.cms.gui.dialog.DialogBox;
import info.magnolia.cms.gui.dialog.DialogButtonSet;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.dialog.DialogHidden;
import info.magnolia.cms.gui.dialog.DialogStatic;
import info.magnolia.cms.gui.dialog.DialogTab;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.module.admininterface.DialogMVCHandler;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;


/**
 * If there are more than one paragraph available you have first to choose one.
 * @author philipp
 */
public class ParagraphSelectDialog extends DialogMVCHandler {
    private static final Logger log = LoggerFactory.getLogger(ParagraphSelectDialog.class);

    private static final String EDIT_PARAGRAPH_DIALOGNAME = "editParagraph";
    public static final String EDITPARAGRAPH_DIALOG_URL = ".magnolia/dialogs/" + EDIT_PARAGRAPH_DIALOGNAME + ".html";

    private final String paragraph;

    public ParagraphSelectDialog(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        paragraph = params.getParameter("mgnlParagraph"); //$NON-NLS-1$
    }

    protected Dialog createDialog(Content configNode, Content websiteNode) throws RepositoryException {
        Dialog dialog = DialogFactory.getDialogInstance(request, response, null, null);

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
        c1.setButtonType(ControlImpl.BUTTONTYPE_RADIO);
        c1.setBoxType(DialogBox.BOXTYPE_1COL);
        c1.setConfig("saveInfo", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        c1.setConfig("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$

        String[] pars = paragraph.split(","); //$NON-NLS-1$
        for (int i = 0; i < pars.length; i++) {
            try {
                addParagraph(c1, pars[i]);

            } catch (Exception e) {
                // paragraph definition does not exist
                log.warn("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }

        tab.addSub(c1);

        return dialog;
    }

    protected void addParagraph(DialogButtonSet radioButtonSet, String paragraph) {
        final Paragraph paragraphInfo = ParagraphManager.getInstance().getParagraphDefinition(paragraph);

        // prevent NPEs
        if (paragraphInfo == null) {
            log.error("Unable to load paragraph {}", paragraph);
            return;
        }

        final Messages msgs = MessagesManager.getMessages(paragraphInfo.getI18nBasename());

        final StringBuffer label = new StringBuffer();
        label.append("<strong>" //$NON-NLS-1$
            + msgs.getWithDefault(paragraphInfo.getTitle(), paragraphInfo.getTitle())
            + "</strong><br />"); //$NON-NLS-1$

        final String description = paragraphInfo.getDescription();
        if (StringUtils.isNotEmpty(description)) {
            label.append(msgs.getWithDefault(description, description));
        }
        label.append("<br /><br />"); //$NON-NLS-1$

        final Button button = new Button(radioButtonSet.getName(), paragraphInfo.getName());
        button.setLabel(label.toString());
        button.setOnclick("document.getElementById('mgnlFormMain').submit();"); //$NON-NLS-1$
        radioButtonSet.addOption(button);
    }

    public Content getStorageNode() {
        return null;
    }

    public Content getConfigNode() {
        return null;
    }

    public String save() {
        try {
            // copy all parameters except mgnlDialog (which we switch to "editParagraph")
            StringBuffer query = new StringBuffer();
            for (Iterator iter = form.getParameters().keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                if (!key.equals("mgnlDialog")) { //$NON-NLS-1$
                    if (query.length() != 0) {
                        query.append("&"); //$NON-NLS-1$
                    }
                    query.append(key);
                    query.append("="); //$NON-NLS-1$
                    query.append(URLEncoder.encode(form.getParameter(key),"UTF-8"));
                }

            }
            response.sendRedirect(request.getContextPath() + "/" + EDITPARAGRAPH_DIALOG_URL + "?" + query); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (IOException e) {
            log.error("can't redirect to the paragraph-dialog", e); //$NON-NLS-1$
        }
        return VIEW_NOTHING;
    }

}
