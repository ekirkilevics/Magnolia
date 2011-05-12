/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.wcm.editor.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.HasDirection;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Base class for horizontal bars with buttons.
 */
public abstract class AbstractBarWidget extends SimplePanel {

    private AbstractBarWidget parentBar;
    private HorizontalPanel horizontalPanel;
    private Label label;
    private String color;

    public AbstractBarWidget(AbstractBarWidget parentBar, String color) {
        this.parentBar = parentBar;
        this.color = color;
        label = new Label("");

        horizontalPanel = new HorizontalPanel();
        horizontalPanel.setWidth("100%");
        horizontalPanel.add(label);
        horizontalPanel.setCellWidth(label, "100%");

        horizontalPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onSelect();
            }
        }, ClickEvent.getType());

        add(horizontalPanel);
        setStyle(color);
    }

    public void deselect() {
        setStyle(this.color);
    }

    protected void onSelect() {

        // TODO: it needs to be deselected when something else is selected

        setStyle("rgb(255, 255, 255)");
    }

    private void setStyle(String color) {
        getElement().setAttribute("style",
                "background-color:" +
                        color +
                        ";" +
                        "background-position:repeat;" +
                        "border-top: 1px solid #ADC97B !important;" +
                        "border-left: 1px solid #ADC97B !important;" +
                        "border-bottom: 1px solid #396101 !important;" +
                        "border-right: 1px solid #396101 !important;");
    }

    public AbstractBarWidget getParentBar() {
        return parentBar;
    }

    protected void setLabel(String label) {
        this.label.setText(label);
    }

    protected void addButton(Button button) {
        horizontalPanel.add(button);
        horizontalPanel.setCellHorizontalAlignment(button, HasHorizontalAlignment.HorizontalAlignmentConstant.endOf(HasDirection.Direction.DEFAULT));
    }

    public void attach(Element element) {
        element.appendChild(getElement());
        onAttach();
    }
}
