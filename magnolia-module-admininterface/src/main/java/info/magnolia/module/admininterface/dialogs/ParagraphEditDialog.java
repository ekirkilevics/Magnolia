/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.config.ParagraphManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.Dialog;

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
    protected Dialog createDialog(Content configNode, Content websiteNode) throws RepositoryException {
        Dialog dialog = super.createDialog(configNode, websiteNode);
        dialog.setConfig("paragraph", paragraph); //$NON-NLS-1$
        return dialog;
    }

    /**
     * Get the configuration of the dialog from the paragraph
     */
    public static Content getConfigNode(HttpServletRequest request, String paragraph) {
        Paragraph para = ParagraphManager.getInstance().getInfo(paragraph);

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
