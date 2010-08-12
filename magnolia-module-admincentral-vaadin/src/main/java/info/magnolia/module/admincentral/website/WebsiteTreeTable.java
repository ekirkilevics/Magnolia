/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.website;

import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.event.ItemClickEvent;


/**
 * Widget to display the Website-structure. Extending TreeTable is required in order to have a
 * single place where the whole widget and events can be kept - separated from the Application
 * class.
 *
 * @author dlipp
 * @version $Id$
 */
public class WebsiteTreeTable extends TreeTable {

    /**
     * Used for deciding, whether a table column gets editable or not (only
     * the selected one will be...).
     * <p/>
     * Hint: not yet properly working
     */
    private ItemClickEvent selectedContactId = null;


    public ItemClickEvent getSelectedContactId() {
        return selectedContactId;
    }

    public void setSelectedContactId(ItemClickEvent selectedContactId) {
        this.selectedContactId = selectedContactId;
    }
}
