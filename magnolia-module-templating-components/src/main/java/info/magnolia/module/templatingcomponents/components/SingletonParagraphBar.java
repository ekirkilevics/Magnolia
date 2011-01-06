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
import info.magnolia.cms.gui.inline.BarNew;
import info.magnolia.context.MgnlContext;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * An hybrid between a new bar and an edit bar for non-moveable paragraphs: it's either added by the editor, edited, or removed.
 * This is currently meant to be used in conjunction with the regular edit bar component: if the request content is present,
 * wrappers should render their content (which should render said paragraph with an edit bar), and if not, this
 * renders a "new bar".
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SingletonParagraphBar extends AbstractAuthoringUiComponent {
    private static final String DEFAULT_ENABLE_LABEL = "buttons.enable";

    /**
     * Utility method for other components to determine if they are being rendered inside a SingletonParagraphBar.
     */
    public static boolean isInSingleton() {
        return MgnlContext.hasAttribute(SingletonParagraphBar.class.getName());
    }

    /**
     * @param serverCfg
     * @param aggState
     * @param contentName the name of the node which contains (or will contain) the singleton paragraph; this is a child node of {@link #currentContent()}.
     * @param allowedParagraphs the list of paragraph definitions (their names) that are allow to be added by this component
     * @param enableButtonLabel if null, default will be used
     */
    public static SingletonParagraphBar make(ServerConfiguration serverCfg, AggregationState aggState, String contentName, List<String> allowedParagraphs, String enableButtonLabel) {
        final SingletonParagraphBar bar = new SingletonParagraphBar(serverCfg, aggState);
        bar.setAllowedParagraphs(allowedParagraphs);
        bar.setContentName(contentName);
        if (enableButtonLabel != null) {
            bar.setEnableButtonLabel(enableButtonLabel);
        }
        return bar;
    }

    private String contentName;
    private List<String> allowedParagraphs;
    private String enableButtonLabel = DEFAULT_ENABLE_LABEL;

    public SingletonParagraphBar(ServerConfiguration server, AggregationState aggregationState) {
        super(server, aggregationState);
    }

    public void setContentName(String contentName) {
        this.contentName = contentName;
    }

    public void setAllowedParagraphs(List<String> allowedParagraphs) {
        this.allowedParagraphs = allowedParagraphs;
    }

    public void setEnableButtonLabel(String enableButtonLabel) {
        this.enableButtonLabel = enableButtonLabel;
    }

    @Override
    protected void doRender(Appendable out) throws IOException, RepositoryException {
        setupSingleton();

        final Content container = currentContent();

        if (container.hasContent(contentName)) {
            // we assume there's an edit bar in the paragraph that will be rendered where this singleton was enabled
            return;
        }

        final BarNew bar = new BarNew();
        bar.setParagraph(asString(allowedParagraphs));
//   TODO     if (StringUtils.isBlank(bar.getParagraph())) {
//            log.warn("No paragraph selected for new bar in {}", pageContext.getPage());
        // don't set new button's label if there's no selectable paragraph
//        }

        bar.setPath(container.getHandle());
        //bar.setNodeCollectionName(contentName);
        bar.setNodeName(contentName); // see difference with NewBar

        bar.setDefaultButtons();
        bar.getButtonNew().setLabel(enableButtonLabel);

        bar.placeDefaultButtons();
        bar.drawHtml((Writer) out);
    }

    private static void setupSingleton() {
        MgnlContext.setAttribute(SingletonParagraphBar.class.getName(), Boolean.TRUE);
    }

    public void postRender() {
        MgnlContext.removeAttribute(SingletonParagraphBar.class.getName());
    }
}
