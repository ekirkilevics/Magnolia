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
package info.magnolia.templatinguicomponents.jsp;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.templatinguicomponents.AuthoringUiComponent;
import info.magnolia.templatinguicomponents.components.NewParagraphBar;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.util.List;

/**
 * @jsp.tag name="new" body-content="empty"
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class NewParagraphBarTag extends AbstractTag {

    private String newButtonLabel;
    private Content target;
    private String containerNodeName;
    private Object allowedParagraphs;

    /**
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setNewLabel(String newButtonLabel) {
        this.newButtonLabel = newButtonLabel;
    }

    /**
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setTarget(Content target) {
        this.target = target;
    }

    /**
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setContainer(String containerNodeName) {
        this.containerNodeName = containerNodeName;
    }

    /**
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setParagraphs(Object allowedParagraphs) {
        this.allowedParagraphs = allowedParagraphs;
    }

    @Override
    protected AuthoringUiComponent prepareUIComponent(ServerConfiguration serverCfg, AggregationState aggState) throws JspException, IOException {
        if (target == null && containerNodeName == null) {
            // TODO check
            throw new JspException("At least target or container must be specified.");
        }

        final List<String> paraList = mandatoryStringList(allowedParagraphs, "paragraphs");

        return NewParagraphBar.make(serverCfg, aggState, target, containerNodeName, paraList, newButtonLabel);
    }

}
