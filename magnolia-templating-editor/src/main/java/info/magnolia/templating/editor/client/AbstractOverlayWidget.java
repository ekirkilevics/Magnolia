/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.templating.editor.client;

import info.magnolia.templating.editor.client.dom.CMSBoundary;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;


/**
 * Base class for overlay widgets.
 */
public class AbstractOverlayWidget extends FlowPanel {

    protected String label;
    protected int level = 1;
    protected CMSBoundary boundary;
    private double top;
    public AbstractOverlayWidget(CMSBoundary boundary) {

        this.boundary = boundary;
        this.label = boundary.getComment().getAttribute("label");

        for (CMSBoundary parent = boundary.getParentBoundary(); parent != null; parent = parent.getParentBoundary()) {
            this.level++;
        }

/*        Label overlayName = new InlineLabel(label);
        overlayName.getElement().getStyle().setFontSize(15, Style.Unit.PX);
        overlayName.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        this.add(overlayName);*/

        this.setStyleName("mgnlOverlay");

        this.getElement().getStyle().setPosition(Style.Position.ABSOLUTE);
        this.getElement().getStyle().setZIndex(level);

    }

    public void attach() {
        Element body = Document.get().getBody();
        body.appendChild(this.getElement());
        onAttach();
    }

    public void setTop(double top) {
        this.top = top;
        this.getElement().getStyle().setTop(top, Style.Unit.PX);
    }
    public double getTop() {
        return top;
    }
    public void setLeft(double left) {
        this.getElement().getStyle().setLeft(left, Style.Unit.PX);
    }

    public void setWidth(double width) {
        this.getElement().getStyle().setWidth(width, Style.Unit.PX);
    }

    public void setHeight(double height) {
        this.getElement().getStyle().setHeight(height, Style.Unit.PX);
    }

    public CMSBoundary getBoundary() {
        return boundary;
    }


}