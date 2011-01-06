/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.module.templatingcomponents.components;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.inline.BarMain;

import java.io.IOException;
import java.io.Writer;

/**
 * This describes the "main bar" for pages. This typically renders a bar to be placed on top of pages, with a
 * "preview" button and a "page info" button. It might also display a language selector (if i18n authoring is
 * enabled), and a "back to AdminCentral" button if relevant.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PageEditBar extends AbstractAuthoringUiComponent {
    private static final String DEFAULT_EDIT_LABEL = "buttons.properties";

    /**
     * @param serverCfg
     * @param aggState
     * @param editButtonLabel pass null for the default
     * @param dialogName if null or empty, no dialog button will be rendered
     */
    public static PageEditBar make(ServerConfiguration serverCfg, AggregationState aggState, String editButtonLabel, String dialogName) {
        final PageEditBar bar = new PageEditBar(serverCfg, aggState);
        if (editButtonLabel != null) {
            bar.setEditButtonLabel(editButtonLabel);
        }

        //
        if (dialogName != null && dialogName.length() > 0) {
            bar.setDialogName(dialogName);
        }

        return bar;
    }

    private String dialogName;
    private String editButtonLabel = DEFAULT_EDIT_LABEL;

    public PageEditBar(ServerConfiguration server, AggregationState aggregationState) {
        super(server, aggregationState);
    }

    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }

    public void setEditButtonLabel(String editButtonLabel) {
        this.editButtonLabel = editButtonLabel;
    }

    protected void doRender(Appendable out) throws IOException {
        final Content content = currentContent();

        final BarMain bar = new BarMain();
        bar.setPath(content.getHandle());

        // TODO - we could deduce the dialog if AggregationState/RenderingContext would hold the "currentRenderableDefinition"
        bar.setDialog(dialogName);

        bar.setAdminButtonVisible(true);
        bar.setDefaultButtons();
        bar.getButtonPreview().setLabel(getMessage(content, "buttons.preview"));
        bar.getButtonEditView().setLabel(getMessage(content, "buttons.preview.hidden"));
        bar.getButtonSiteAdmin().setLabel(getMessage(content, "buttons.admincentral"));
        bar.getButtonProperties().setLabel(getMessage(content, editButtonLabel));

        bar.placeDefaultButtons();
        bar.drawHtml((Writer) out);
    }

}
