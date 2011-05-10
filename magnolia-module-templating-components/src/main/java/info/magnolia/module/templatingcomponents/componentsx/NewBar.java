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
package info.magnolia.module.templatingcomponents.componentsx;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.inline.BarNew;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Builds a new bar UI component.
 *
 * @version $Id$
 */
public class NewBar extends AbstractAuthoringUiComponent {
    private static final String DEFAULT_NEW_LABEL = "buttons.new";

    /**
     * @param serverCfg
     * @param aggState
     * @param containerName the name of the node into which new paragraphs will be added; this is a child node of {@link #currentContent()}.
     * @param allowedParagraphs the list of paragraph definitions (their names) that are allow to be added by this component
     * @param newButtonLabel if null, default will be used
     */
    public static NewBar make(ServerConfiguration serverCfg, AggregationState aggState, String containerName, List<String> allowedParagraphs, String newButtonLabel) {
        final NewBar bar = new NewBar(serverCfg, aggState);
        /* TODO @param do we want this ? specificTarget override for {@link #currentContent()}.
        if (specificTarget != null) {
            bar.setContent(specificTarget);
        }
        */
        bar.setContainerName(containerName);

        if (newButtonLabel != null) {
            bar.setNewButtonLabel(newButtonLabel);
        }

        bar.setAllowedParagraphs(allowedParagraphs);
        return bar;
    }

    private String containerName;
    private String newButtonLabel = DEFAULT_NEW_LABEL;
    private List<String> allowedParagraphs;

    public NewBar(ServerConfiguration server, AggregationState aggregationState) {
        super(server, aggregationState);
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
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

        bar.setParagraph(asString(allowedParagraphs));

        final Content content = currentContent();
        final String targetPath = content.getHandle();
        bar.setPath(targetPath);

        // TODO - test combinations of containerName and target

        bar.setNodeCollectionName(containerName);
        bar.setNodeName("mgnlNew"); // one of the quirks we'll have to get rid of.

        // don't set new button if there's no selectable paragraph
        if (!allowedParagraphs.isEmpty()) {
            bar.setDefaultButtons();
            // final String label = allowedParagraphs.isEmpty() ? "buttons.new.noparagraph" : newButtonLabel;
            bar.getButtonNew().setLabel(getMessage(content, newButtonLabel));

            bar.placeDefaultButtons();
        }
        bar.drawHtml((Writer) out);
    }

}
