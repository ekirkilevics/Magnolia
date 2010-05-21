/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import java.io.IOException;

import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.module.admininterface.DialogHandlerManager;
import info.magnolia.module.admininterface.DialogMVCHandler;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This dialog hander delegates to a dialog handler representing the dialog assigned to the paragraph we edit (or create).
 *
 * @author philipp
 */
public class ParagraphEditDialog extends ConfiguredDialog {

    private DialogMVCHandler dialogHandler;

    public ParagraphEditDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);

        // build the dialog handler we will delegate to
        String paragraphName = params.getParameter("mgnlParagraph");

        final String dialogName = getDialogUsedByParagraph(paragraphName);

        dialogHandler = DialogHandlerManager.getInstance().getDialogHandler(dialogName, request, response);

        // needed for the creation of new paragraphs
        dialogHandler.getDialog().setConfig("paragraph", paragraphName);
    }

    private String getDialogUsedByParagraph(String paragraphName) {
        if (StringUtils.isEmpty(paragraphName)) {
            throw new IllegalStateException("No paragraph selected.");
        }
        final Paragraph para = ParagraphManager.getInstance().getParagraphDefinition(paragraphName);
        if (para == null) {
            throw new IllegalStateException("No paragraph registered with name " + paragraphName);
        }
        final String dialogName;
        if (para.getDialog() != null) {
            dialogName = para.getDialog();
        } else {
            dialogName = para.getName();
        }
        return dialogName;
    }

    // methods delegating to the paragraph's dialog handler
    // this are the only methods called by the dialog mvc servlet

    public String getCommand() {
        return dialogHandler.getCommand();
    }

    public String execute(String command) {
        return dialogHandler.execute(command);
    }

    public void renderHtml(String view) throws IOException {
        dialogHandler.renderHtml(view);
    }

}
