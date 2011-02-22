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
package info.magnolia.module.admincentral.place;

import static junit.framework.Assert.*;
import info.magnolia.module.vaadin.place.Place;

import org.junit.Before;
import org.junit.Test;

/**
 * @author fgrilli
 *
 */
public class AdminCentralPlaceHistoryMapperTest {

    private AdminCentralPlaceHistoryMapper mapper = new AdminCentralPlaceHistoryMapper(EditWorkspacePlace.class);
    private EditWorkspacePlace editWorkspacePlace;
    private ItemSelectedPlace itemSelectedPlace;
    private String prefix;

    @Before
    public void setUp(){
        editWorkspacePlace = new EditWorkspacePlace("website");
        itemSelectedPlace = new ItemSelectedPlace("website", "/foo/bar");
        editWorkspacePlace.setSubPlace("edit-workspace", itemSelectedPlace);
        prefix = editWorkspacePlace.getPrefixValue();
    }

    @Test
    public void testGetToken(){
        String token = mapper.getToken(editWorkspacePlace);
        assertNotNull(token);
        assertEquals(prefix + ":website;/foo/bar", token);
    }

    @Test
    public void testGetPlace(){
        Place place = mapper.getPlace(prefix + ":website;/foo/bar");
        assertNotNull(place);
        assertTrue(place instanceof EditWorkspacePlace);
        assertTrue(((EditWorkspacePlace)place).getSubPlace("edit-workspace") instanceof ItemSelectedPlace);
    }
}
