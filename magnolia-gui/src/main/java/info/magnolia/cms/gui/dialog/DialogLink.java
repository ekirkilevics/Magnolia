/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.gui.dialog;

import info.magnolia.repository.RepositoryConstants;


/**
 * @author Vinzenz Wyser
 * @version $Revision$ ($Author$)
 */
public class DialogLink extends DialogEditWithButton {

    /**
     * Customize the dialog.
     * @see info.magnolia.cms.gui.dialog.DialogEditWithButton#doBeforeDrawHtml()
     */
    @Override
    protected void doBeforeDrawHtml() {
        super.doBeforeDrawHtml();

        String extension = this.getConfigValue("extension"); //$NON-NLS-1$
        String label = this.getMessage("dialog.link.internal"); //$NON-NLS-1$
        this.getButton().setLabel(label);
        this.getButton().setSaveInfo(false);
        String repository = this.getConfigValue("repository", RepositoryConstants.WEBSITE); //$NON-NLS-1$
        String tree = this.getConfigValue("tree", repository);
        String buttonOnClick = this.getConfigValue(
            "buttonOnClick",
            "mgnlDialogLinkOpenBrowser('" + this.getName() + "','" + tree + "','" + extension + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        this.getButton().setOnclick(buttonOnClick);

    }
}
