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
package info.magnolia.cms.gui.dialog;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class DialogControlImplTest {
    
    private HttpServletRequest request;
    
    private HttpServletResponse response;
    
    private Content configNode;
    
    @Before
    public void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        configNode = mock(Content.class);
    }
    
    @Test
    public void testSortMultiselectItemsInRightOrder() throws RepositoryException, IOException {
        DialogMultiSelect select = new DialogMultiSelect();
        select.setConfig(DialogMultiSelect.CONFIG_SAVE_MODE, DialogMultiSelect.SAVE_MODE_LIST);
        
        // insert unsorted properties
        String unsortedProp =
            "/root/another/node.1=/categorization/Family\n" +           
            "/root/another/node.2=/categorization/Sports\n" +
            "/root/another/node.0=/categorization/Culture\n";
        
        // sorted node data
        List<String> sorted = new ArrayList<String>();
        sorted.add("/categorization/Culture");
        sorted.add("/categorization/Family");
        sorted.add("/categorization/Sports");
        
        // unsorted node data
        List<String> unsorted = new ArrayList<String>();
        unsorted.add("/categorization/Family");
        unsorted.add("/categorization/Sports");
        unsorted.add("/categorization/Culture");
        
        HierarchyManager hm = MockUtil.createHierarchyManager(unsortedProp);

        when(configNode.getHandle()).thenReturn("/root");
        when(configNode.getName()).thenReturn("/another/node");
        
        select.init(request, response, hm.getContent("/root"), configNode);
        
        // read sorted values
        List<String> nodeValues = select.readValues();

        assertFalse(nodeValues.equals(unsorted));
        assertTrue(nodeValues.equals(sorted));
    }
}