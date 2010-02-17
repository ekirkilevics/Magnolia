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
package info.magnolia.templatinguicomponents.components;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.gui.inline.BarNew;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $) 
 */
public class NewParagraphBar extends AbstractAuthoringUiComponent {
    private String newButtonLabel = "buttons.new";
    private List<String> allowedParagraphs;

    public NewParagraphBar(ServerConfiguration server, AggregationState aggregationState) {
        super(server, aggregationState);
    }

    public void setNewButtonLabel(String newButtonLabel) {
        this.newButtonLabel = newButtonLabel;
    }

    public void setAllowedParagraphs(List<String> allowedParagraphs) {
        this.allowedParagraphs = allowedParagraphs;
    }

    @Override
    protected void doRender(Appendable out) throws IOException {
        final BarNew bar = new BarNew();

        bar.setParagraph(allowedParagraphsAsString());
//        if (StringUtils.isBlank(bar.getParagraph())) {
//            log.warn("No paragraph selected for new bar in {}", pageContext.getPage());
        // don't set new button's label if there's no selectable paragraph
//        }

        final String targetPath = getTarget().getHandle();
        final String lastPortionPath = targetPath.substring(targetPath.lastIndexOf('/') + 1);
        bar.setPath(targetPath);
//        bar.setNodeCollectionName(lastPortionPath);
        bar.setNodeName("mgnlNew"); // one of the quirks we'll have to get rid of.

        bar.setDefaultButtons();

        bar.getButtonNew().setLabel(newButtonLabel);

        bar.placeDefaultButtons();
        bar.drawHtml((Writer) out);
    }

    private String allowedParagraphsAsString() {
        return StringUtils.join(allowedParagraphs, ',');
    }
}
