/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.module.admincentral.navigation;

import java.util.LinkedList;
import java.util.List;
/**
 * Represents a menu item.
 * @author fgrilli
 * @deprecated decide whether we need this or not
 */
public class MenuDefinition {
    private String name;
    private String icon;
    private String label;
    //TODO This should represent the view to navigate to. It can either be a html page (which will be embedded as an iframe in the main container)
    //or a Vaadin component, e.g. a TreeTable (possibly implementing Navigator.View for handling history/bookmarkability?)
    private String view;

    private List<MenuDefinition> subs = new LinkedList<MenuDefinition>();

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public String getView() {
        return view;
    }
    public void setView(String view) {
        this.view = view;
    }

    public List<MenuDefinition> getSubs(){
        return subs;
    }
    public void setSubs(List<MenuDefinition> subs){
        this.subs = subs;
    }

    public boolean addSub(MenuDefinition md){
        return subs.add(md);
    }
}
