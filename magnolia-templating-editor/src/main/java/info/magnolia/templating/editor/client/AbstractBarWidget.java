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


import info.magnolia.templating.editor.client.dom.CMSComment;
import info.magnolia.templating.editor.client.dom.MgnlElement;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Float;


import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

/**
 * Base class for horizontal bars with buttons.
 */
public abstract class AbstractBarWidget extends FlowPanel {

    private MgnlElement boundary;
    protected boolean hasControls = false;
    private String label = "";
    private boolean toggleSelection = false;

    public AbstractBarWidget(MgnlElement boundary, CMSComment comment) {

        this.setBoundary(boundary);
        if (comment != null) {
            this.label = comment.getAttribute("label");
        }

        if (this.label != null && !this.label.isEmpty()) {
            Label areaName = new InlineLabel(this.label);
            //tooltip. Nice to have when area label is truncated because too long.
            areaName.setTitle(this.label);

            //setStylePrimaryName(..) replaces gwt default css class, in this case gwt-Label
            areaName.setStylePrimaryName("mgnlAreaLabel");
            add(areaName);
        }

    }

    /**
     * Called when this bar widget is selected/clicked. Default implementation does nothing.
     */
    protected void select() {

        this.addStyleName("selected");
/*        Document.get().getElementById("mgnlBoundary").getStyle().setTop(boundary.getMinCoordinate().getTop(), Style.Unit.PX);
        Document.get().getElementById("mgnlBoundary").getStyle().setLeft(boundary.getMinCoordinate().getLeft(), Style.Unit.PX);
        Document.get().getElementById("mgnlBoundary").getStyle().setHeight(boundary.getMaxCoordinate().getTop()-boundary.getMinCoordinate().getTop(), Style.Unit.PX);
        Document.get().getElementById("mgnlBoundary").getStyle().setHeight(boundary.getMaxCoordinate().getLeft()-boundary.getMinCoordinate().getLeft(), Style.Unit.PX);*/
    }

    protected void deSelect() {

        this.removeStyleName("selected");

    }

    protected void setId(String id){
        getElement().setId(id);
    }

    protected void addButton(final Button button, final Float cssFloat) {
        button.setStylePrimaryName("mgnlControlButton");
        button.getElement().getStyle().setFloat(cssFloat);

/*        button.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                //add push button style
                button.setStyleName("mgnlControlButton_PUSHED", true);
            }
        });
        button.addMouseUpHandler(new MouseUpHandler() {

            @Override
            public void onMouseUp(MouseUpEvent event) {
                //remove push button style
                button.setStyleName("mgnlControlButton_PUSHED", false);
            }
        });*/
        add(button);
        hasControls = true;
    }

    protected void setClassName(String className) {
        getElement().setClassName(className);
    }

    /**
     * @return the element's underlying {@link Style}. You can use this object to manipulate the css style attribute of this bar widget.
     */
    protected Style getStyle() {
        return getElement().getStyle();
    }

    /**
     *  TODO: we should not have to call onAttach ourself?
     */
    public void attach(Element element) {
        final Node parentNode = element.getParentNode();
        parentNode.insertAfter(getElement(), element);
        onAttach();
    }

    /**
     *  TODO: we should not have to call onAttach ourself?
     */
    public void attach(Node node) {
        final Node parentNode = node.getParentNode();
        parentNode.insertAfter(getElement(), node);
        onAttach();
    }
    public void setBoundary(MgnlElement boundary) {
        this.boundary = boundary;
    }

    public MgnlElement getBoundary() {
        return boundary;
    }
}
