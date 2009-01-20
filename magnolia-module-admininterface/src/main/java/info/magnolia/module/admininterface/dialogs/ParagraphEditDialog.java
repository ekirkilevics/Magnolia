/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.module.admininterface.DialogHandlerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Editing paragraph data.
 *
 * @author philipp
 */
public class ParagraphEditDialog extends ConfiguredDialog {
    private static final Logger log = LoggerFactory.getLogger(ParagraphEditDialog.class);

    private final String paragraph;

    public ParagraphEditDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);
        paragraph = params.getParameter("mgnlParagraph"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#createDialog(info.magnolia.cms.core.Content,
     * info.magnolia.cms.core.Content)
     */
    protected Dialog createDialog(Content configNode, Content websiteNode) throws RepositoryException {
        if (StringUtils.isEmpty(this.paragraph)) {
            throw new IllegalStateException("No paragraph selected.");
        }
        final Paragraph para = ParagraphManager.getInstance().getParagraphDefinition(paragraph);
        if (para == null) {
            throw new IllegalStateException("No paragraph registered with name " + paragraph);
        }
        final String dialogName;
        if (para.getDialog() != null) {
            dialogName = para.getDialog();
        } else {
            dialogName = para.getName();
        }

        final Content dialogConfigNode = DialogHandlerManager.getInstance().getDialogConfigNode(dialogName);
        Dialog dialog = super.createDialog(dialogConfigNode, websiteNode);
        dialog.setConfig("paragraph", paragraph); //$NON-NLS-1$
        return dialog;
    }

    /**
     * Get the configuration of the dialog from the paragraph
     * @deprecated since 4.0 - this is not used
     */
    public static Content getConfigNode(HttpServletRequest request, String paragraph) {
        Paragraph para = ParagraphManager.getInstance().getParagraphDefinition(paragraph);

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
     * @deprecated since 4.0 - this is not used
     */
    public String getParagraph() {
        return paragraph;
    }
}
