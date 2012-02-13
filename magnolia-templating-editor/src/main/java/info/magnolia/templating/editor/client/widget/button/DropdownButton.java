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
package info.magnolia.templating.editor.client.widget.button;

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
 * they are in the list or array passed in to the object's constructor. A menu item can also accept a further {@link MenuBar} as a parameter, thus enabling sub menus.
 * <p>Usage sample:
 * <pre>
 *  ...
 *  // commands are defined elsewhere
 *  MenuItem one = new MenuItem("Option one", true, optionCommandOne);
 *  MenuItem two = new MenuItem("Option two", true, optionCommandTwo);
 *  MenuItem three = new MenuItem("Option three", true, optionCommandThree);
 *
 *  List&lt;MenuItem&gt; options = new ArrayList&lt;MenuItem&gt;();
 *  options.add(one);
 *  options.add(two);
 *  options.add(three);
 *
 *  DropdownButton dropdown = new DropdownButton("My cool caption", options);
 *  ...
 * </pre>
 *
 * @version $Id$
 */
public class DropdownButton extends Button {

    private final PopupPanel dropdownPanel = new PopupPanel(true);
    private final MenuBar dropdownMenuBar = new MenuBar(true);

    public DropdownButton(String caption, List<MenuItem> menuItems) {
        super(caption);
        if(menuItems == null) {
            throw new IllegalArgumentException("menuItems cannot be null");
        }
        dropdownMenuBar.setStylePrimaryName("mgnlPreviewMenuDropdown");
        for(MenuItem item: menuItems) {
            item.setStylePrimaryName("mgnlPreviewMenuItem");
            dropdownMenuBar.addItem(item);
        }
        dropdownPanel.setStylePrimaryName("mgnlPreviewMenuPanel");
        //TODO add an onMouseOut event to the menu? This however will make it disappear also when hovering back on the button itself.
        dropdownPanel.add(dropdownMenuBar);

        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
               onClickCallback(event);
            }
        });
    }

    public DropdownButton(String caption, MenuItem... menuItems) {
        this(caption, Arrays.asList(menuItems));
    }

    /**
     * Sets the dropdown's position relative to the browser's client area.
     * @param left in px
     * @param top in px
     */
    protected void setDropdownPosition(int left, int top) {
        dropdownPanel.setPopupPosition(left, top);
    }

    protected void showDropdown() {
        dropdownMenuBar.setVisible(true);
        dropdownPanel.show();
    }

    protected void hideDropdown() {
        dropdownMenuBar.setVisible(false);
        dropdownPanel.hide();
    }

    /**
     * Callback method invoked when clicking on the drop-down button. By default the menu will appear just beneath the button, left aligned with it.
     */
    protected void onClickCallback(ClickEvent event) {
        setDropdownPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight());
        showDropdown();
    }
}
