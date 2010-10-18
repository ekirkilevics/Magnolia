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
package info.magnolia.module.admincentral.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import javax.jcr.RepositoryException;

import org.junit.Test;

import com.vaadin.ui.Field;


/**
 * Tests LabelColumn.
 *
 * @author daniellipp
 * @version $Id$
 */
public class LabelColumnTest {

    @Test
    public void testGetValue() throws RepositoryException {
        MockNode mock = new MockNode();
        String original = "Beckenbauer";
        mock.setName(original);
        mock.setProperty("name", original);
        LabelColumn column = new LabelColumn();
        Object result = column.getValue(mock);
        assertEquals(original, result);
    }

    @Test
    public void testSetValue() throws RepositoryException {
        MockNode mock = new MockNode();
        LabelColumn column = new LabelColumn();
        String newValue = "Netzer";
        // TODO: uncomment as soon as setValue is properly implemented on that Column.
        // column.setValue(mock, "newValue");
        // assertEquals(column.getValue(mock), newValue);
    }

    @Test
    public void testGetEditField() {
        LabelColumn column = new LabelColumn();
        boolean editableFlag = true;
        column.setEditable(editableFlag);
        assertEquals(editableFlag, column.isEditable());
        assertTrue(column.getEditField(null) instanceof Field);

        editableFlag = false;
        column.setEditable(editableFlag);
        assertNull(column.getEditField(null));
    }
}