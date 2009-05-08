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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.inline.BarMain;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.tagext.TagSupport;
import java.util.ArrayList;
import java.util.List;


/**
 * Displays the mainBar, i.e. the bar that allows you to change the page properties and switch to preview mode. This
 * tag also add the CSS and JS links if not previously defined, but it's recommended to add the cms:links tag to the
 * header of the page. CSS links are not valid inside the HTML body tag.
 * 
 * @jsp.tag name="mainBar" body-content="JSP"
 * 
 * @author Marcel Salathe
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class MainBar extends TagSupport implements BarTag {
    private static final Logger log = LoggerFactory.getLogger(MainBar.class);

    private String dialog;

    private boolean adminButtonVisible = true;

    /**
     * Label for the properties button.
     */
    private String label;

    /**
     * Addition buttons (left).
     */
    private List buttonLeft;

    /**
     * Addition buttons (right).
     */
    private List buttonRight;

    /**
     * Label for the page properties button.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @param s dialogName type
     * @deprecated use the dialog attribute instead
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setParagraph(String s) {
        this.dialog = s;
    }

    /**
     * Name of the dialog for the page properties. (as defined in config)
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    /**
     * Set this to false if you don't want to show the AdminCentral button.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setAdminButtonVisible(boolean adminButtonVisible) {
        this.adminButtonVisible = adminButtonVisible;
    }

    public void addButtonLeft(Button button) {
        if (buttonLeft == null) {
            buttonLeft = new ArrayList();
        }
        buttonLeft.add(button);
    }

    public void addButtonRight(Button button) {
        if (buttonRight == null) {
            buttonRight = new ArrayList();
        }
        buttonRight.add(button);
    }

    public int doStartTag() {

        if (ServerConfiguration.getInstance().isAdmin()) {
            return EVAL_BODY_INCLUDE;
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        Content activePage = MgnlContext.getAggregationState().getMainContent();
        if (ServerConfiguration.getInstance().isAdmin() && activePage != null && activePage.isGranted(Permission.SET)) {
            try {
                BarMain bar = new BarMain();
                bar.setPath(this.getPath());
                bar.setDialog(this.dialog);
                bar.setAdminButtonVisible(this.adminButtonVisible);
                bar.setDefaultButtons();

                if (label != null) {
                    bar.getButtonProperties().setLabel(label);
                }

                if (buttonRight != null) {
                    bar.getButtonsRight().addAll(buttonRight);
                }
                if (buttonLeft != null) {
                    bar.getButtonsLeft().addAll(buttonLeft);
                }

                bar.placeDefaultButtons();
                bar.drawHtml(pageContext.getOut());
            }
            catch (Exception e) {
                log.warn("Exception caught during display: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }

        this.buttonLeft = null;
        this.buttonRight = null;

        return EVAL_PAGE;
    }

    /**
     * Get the content path. (Page or Node)
     * @return String path
     */
    private String getPath() {
        try {
            return Resource.getCurrentActivePage().getHandle();
        }
        catch (Exception re) {
            return StringUtils.EMPTY;
        }
    }

    public void release() {
        super.release();
        this.dialog = null;
        this.adminButtonVisible = true;
        this.label = null;
    }

}
