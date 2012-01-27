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
package info.magnolia.templating.editor.client;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * A button which, when clicking on it, will display a dropdown menu just beneath. The menu can have different mutually exclusive choices.
 * Each choice corresponds to a {@link MenuItem}. The order in which these options are added in the menu bar is the same in which
 * they are in the list or array passed in to the object's constructor. A menu item can also accept a further {@link MenuBar} as a parameter. This will enable sub menus.
 * <p>Usage sample:
 * <pre>
 *  ...
 *  // commands are already defined elsewhere
 *  MenuItem one = new MenuItem("Option one", true, optionCommandOne);
 *  MenuItem two = new MenuItem("Option two", true, optionCommandTwo);
 *  MenuItem three = new MenuItem("Option three", true, optionCommandThree);
 *
 *  List&lt;MenuItem&gt; options = new ArrayList&lt;MenuItem&gt;();
 *  options.add(one);
 *  options.add(two);
 *  options.add(three);
 *
 *  DropdownButtonWidget dropdown = new DropdownButtonWidget(options);
 *  ...
 * </pre>
 *
 * @version $Id$
 */
public class DropdownButtonWidget extends Button {

    private final PopupPanel dropdownPanel = new PopupPanel(true);
    private final MenuBar dropdownMenuBar = new MenuBar(true);

    public DropdownButtonWidget(List<MenuItem> menuItems) {
        if(menuItems == null) {
            throw new IllegalArgumentException("menuItems cannot be null");
        }

        for(MenuItem item: menuItems) {
            dropdownMenuBar.addItem(item);
        }

        dropdownPanel.add(dropdownMenuBar);
        dropdownPanel.setPopupPosition(this.getAbsoluteLeft(), this.getAbsoluteTop() + this.getOffsetHeight());

        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dropdownMenuBar.setVisible(true);
                dropdownPanel.show();
            }
        });
    }

    public DropdownButtonWidget(MenuItem... menuItems) {
        this(Arrays.asList(menuItems));
    }
}
