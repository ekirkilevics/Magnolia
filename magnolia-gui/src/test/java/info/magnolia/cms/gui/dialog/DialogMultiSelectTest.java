/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.cms.gui.dialog;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @version $Id$
 */
public class DialogMultiSelectTest  {

    @Test
    public void testGetJSONFromJSONModel() {
        // GIVEN
        DialogMultiSelect select = new DialogMultiSelect();
        select.setConfig(DialogMultiSelect.CONFIG_SAVE_MODE, DialogMultiSelect.SAVE_MODE_JSON);
        final String jsonValue = "[{value:''}]";
        select.setValue(jsonValue);

        // WHEN
        String result = select.getJSON();

        // THEN
        assertEquals(jsonValue, result);
    }

    @Test
    public void testGetJSONFromListModel() {
        // GIVEN
        DialogMultiSelect select = new DialogMultiSelect();
        select.setConfig(DialogMultiSelect.CONFIG_SAVE_MODE, DialogMultiSelect.SAVE_MODE_LIST);
        select.setValue("I'm from Alabama,with my Banjo,on my knee.");

        // WHEN
        String result = select.getJSON();

        // THEN - verify single quote got escaped
        assertEquals("[{value: 'I\\'m from Alabama'},{value: 'with my Banjo'},{value: 'on my knee.'}]", result);
    }
}
