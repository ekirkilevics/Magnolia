/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.cms.gui.controlx.list;

import info.magnolia.cms.gui.controlx.impl.TemplatedRenderer;


/**
 * Renders a list view.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ListControlRenderer extends TemplatedRenderer {

    /**
     * Default template used.
     */
    public ListControlRenderer() {
        super();
    }

    /**
     * Pass the template to use.
     * @param templateName
     */
    public ListControlRenderer(String templateName) {
        super(templateName);
    }

    /**
     * Return asc or desc.
     * @param list
     * @param field
     * @return
     */
    public String nextSortByOrder(ListControl list, String field) {
        if (list.getSortBy().equals(field)) {
            if (list.getSortByOrder().equals("asc")) {
                return "desc";
            }
        }
        return "asc";
    }

    /**
     * Return asc or desc.
     * @param list
     * @param field
     * @return
     */
    public String nextGroupByOrder(ListControl list, String field) {
        if (list.getGroupBy().equals(field)) {
            if (list.getGroupByOrder().equals("asc")) {
                return "desc";
            }
        }
        return "asc";
    }

    /**
     * Called onclick, dblclick, contextmenu
     * @param list
     * @return
     */
    public String onSelect(ListControl list, Integer index) {
        return "";
    }

    /**
     * Render the click event
     * @param iter
     * @return
     */
    public String onClick(ListControl list, Integer index) {
        return "";
    }

    /**
     * Render the double click event
     * @param iter
     * @return
     */
    public String onDblClick(ListControl list, Integer index) {
        return "";
    }

    /**
     * Render the double click event
     * @param iter
     * @return
     */
    public String onRightClick(ListControl list, Integer index) {
        return "";
    }

    /**
     * Used to get the css class for the grouplinks
     * @param list
     * @param field
     * @return the css class as a string
     */
    public String getGroupLinkCSSClass(ListControl list, String field) {
        if (list.getGroupBy().equals(field)) {
            return "mgnlListSortGroupLink" + list.getGroupByOrder().toUpperCase();
        }
        return "mgnlListSortGroupLink";
    }

    /**
     * Used to get the css class for the sort links
     * @param list
     * @param field
     * @return the css class as a string
     */
    public String getSortLinkCSSClass(ListControl list, String field) {
        if (list.getSortBy().equals(field)) {
            return "mgnlListSortGroupLink" + list.getSortByOrder().toUpperCase();
        }
        return "mgnlListSortGroupLink";
    }

}
