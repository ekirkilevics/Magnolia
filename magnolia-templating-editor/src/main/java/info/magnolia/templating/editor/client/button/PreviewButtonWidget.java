/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.templating.editor.client.button;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.MenuItem;

/**
 * Preview button composed by two buttons: left-hand side button is for the default action, whereas right-hand side button will display a dropdown menu with further options.
 * @version $Id$
 *
 */
public final class PreviewButtonWidget extends Composite {

    private static FlowPanel panel = new FlowPanel();
    private Button defaultActionButton = new Button();

    public PreviewButtonWidget(final String caption, final Command defaultAction, final DropdownButtonWidget dropdown) {
        initWidget(panel);
        defaultActionButton.setHTML(caption);
        defaultActionButton.setStylePrimaryName("mgnlEditorButton");
        defaultActionButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                defaultAction.execute();
            }
        });

        panel.add(defaultActionButton);

        dropdown.setStylePrimaryName("mgnlEditorButton");
        dropdown.addStyleDependentName("previewRight");
        panel.add(dropdown);
    }

    /**
     * Extends {@link DropdownButtonWidget} in order to left align itself with the default (lhs) button.
     * @version $Id$
     */
    public static final class PreviewDropdownButtonWidget extends DropdownButtonWidget {

        public PreviewDropdownButtonWidget(final List<MenuItem> menuItems) {
            super("---", menuItems); //FIXME in order to have this top aligned correctly, we need to set a dummy text and then hide it with a negative text-indent. See also editor.css
        }
        @Override
        protected void setDropdownPosition(int left, int top) {
            getDropdownPanel().setPopupPosition(panel.getAbsoluteLeft(), panel.getAbsoluteTop() + panel.getOffsetHeight());
            //TODO does this have the desired effect?
            getDropdownPanel().setWidth(panel.getOffsetWidth()+"px");
        }
    }

}
