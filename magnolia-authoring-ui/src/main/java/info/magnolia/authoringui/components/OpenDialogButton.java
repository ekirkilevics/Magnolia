/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.authoringui.components;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;

import java.io.IOException;

/**
 * This describes a simple "edit" button which will open a given dialog for a given content node.
 * The dialog name is not deduced.
 * TODO - except maybe for page info? although in that case we're looking at the main bar.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class OpenDialogButton extends AbstractAuthoringUiComponent {
    private String dialogName;

    public OpenDialogButton(ServerConfiguration server, AggregationState aggregationState) {
        super(server, aggregationState);
    }

    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }

    public void doRender(Appendable out) throws IOException {
        out.append("This is a").append(getClass().getSimpleName()).append(" for dialog ").append(dialogName).append(" for node ").append(String.valueOf(getTarget()));
    }
}