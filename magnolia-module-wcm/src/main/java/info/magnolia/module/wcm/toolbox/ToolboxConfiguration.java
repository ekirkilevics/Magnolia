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
package info.magnolia.module.wcm.toolbox;

import java.util.ArrayList;
import java.util.List;

import info.magnolia.ui.model.menu.definition.MenuItemDefinition;

/**
 * Toolbox configuration.
 */
public class ToolboxConfiguration {

    private List<MenuItemDefinition> page = new ArrayList<MenuItemDefinition>();
    private List<MenuItemDefinition> area = new ArrayList<MenuItemDefinition>();
    private List<MenuItemDefinition> slot = new ArrayList<MenuItemDefinition>();
    private List<MenuItemDefinition> paragraph = new ArrayList<MenuItemDefinition>();
    private List<MenuItemDefinition> paragraphInSlot = new ArrayList<MenuItemDefinition>();

    public List<MenuItemDefinition> getPage() {
        return page;
    }

    public void setPage(List<MenuItemDefinition> page) {
        this.page = page;
    }

    public List<MenuItemDefinition> getParagraph() {
        return paragraph;
    }

    public void setParagraph(List<MenuItemDefinition> paragraph) {
        this.paragraph = paragraph;
    }

    public List<MenuItemDefinition> getArea() {
        return area;
    }

    public void setArea(List<MenuItemDefinition> area) {
        this.area = area;
    }

    public List<MenuItemDefinition> getParagraphInSlot() {
        return paragraphInSlot;
    }

    public List<MenuItemDefinition> getSlot() {
        return slot;
    }

    public void setSlot(List<MenuItemDefinition> slot) {
        this.slot = slot;
    }

    public void setParagraphInSlot(List<MenuItemDefinition> paragraphInSlot) {
        this.paragraphInSlot = paragraphInSlot;
    }

    public boolean addPage(MenuItemDefinition menuItemDefinition) {
        return page.add(menuItemDefinition);
    }

    public boolean addParagraph(MenuItemDefinition menuItemDefinition) {
        return paragraph.add(menuItemDefinition);
    }

    public boolean addArea(MenuItemDefinition menuItemDefinition) {
        return area.add(menuItemDefinition);
    }

    public boolean addParagraphInSlot(MenuItemDefinition menuItemDefinition) {
        return paragraphInSlot.add(menuItemDefinition);
    }

    public boolean addSlot(MenuItemDefinition menuItemDefinition) {
        return slot.add(menuItemDefinition);
    }
}
