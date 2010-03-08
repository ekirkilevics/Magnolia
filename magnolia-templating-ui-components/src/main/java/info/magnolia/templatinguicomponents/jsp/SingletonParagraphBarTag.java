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
import info.magnolia.templatinguicomponents.AuthoringUiComponent;
import info.magnolia.templatinguicomponents.components.SingletonParagraphBar;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.util.List;

/**
 * @jsp.tag name="singleton" body-content="scriptless"
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SingletonParagraphBarTag extends AbstractTag {

    private String contentName;
    private Object allowedParagraphs;
    private String enableButtonLabel;

    /**
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setContent(String contentName) {
        this.contentName = contentName;
    }

    /**
     * @jsp.attribute required="false" rtexprvalue="true" type="java.lang.Object"
     */
    public void setParagraphs(Object allowedParagraphs) {
        this.allowedParagraphs = allowedParagraphs;
    }

    /**
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setEnableLabel(String enableButtonLabel) {
        this.enableButtonLabel = enableButtonLabel;
    }

    @Override
    protected AuthoringUiComponent prepareUIComponent(ServerConfiguration serverCfg, AggregationState aggState) throws JspException, IOException {
        final List<String> paraList = mandatoryStringList(allowedParagraphs, "paragraphs");

        return SingletonParagraphBar.make(serverCfg, aggState, contentName, paraList, enableButtonLabel);
    }

}
