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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.DefaultFieldFactory;


/**
 * @author dlipp
 * @version $Id$
 */
public class WebsiteTreeTableFactoryTest {

    @Test
    public void testCreateWebsiteTreeTable() {
        WebsiteTreeTable treeTable = WebsiteTreeTableFactory.getInstance().createWebsiteTreeTable();
        assertTrue("TreeTabel expected to be editable", treeTable.isEditable());
        assertTrue("TreeTable expected to be selectable", treeTable.isSelectable());
        assertNotNull(treeTable.getTableFieldFactory());
    }

    @Test
    public void testDropHandler() {
        WebsiteTreeTable treeTable = WebsiteTreeTableFactory.getInstance().createWebsiteTreeTable();
        DropHandler dropHandler = treeTable.getDropHandler();
        assertNotNull(dropHandler);

        AcceptCriterion acceptCriterion = dropHandler.getAcceptCriterion();
        assertEquals(AcceptAll.get(), acceptCriterion);

        /**
         * TODO: create sample TableModel & DragAndDropEvent to test D&D in detail
         */
    }

    @Test
    public void testDoulbeClickEditing() {
        WebsiteTreeTable treeTable = new WebsiteTreeTable();
        assertEquals(DefaultFieldFactory.class,treeTable.getTableFieldFactory().getClass());

        WebsiteTreeTableFactory.getInstance().addEditingByDoubleClick(treeTable);
        assertNotNull(treeTable.getTableFieldFactory());

        /**
         * TODO: Test the ItemClickListener
         */
    }
}
