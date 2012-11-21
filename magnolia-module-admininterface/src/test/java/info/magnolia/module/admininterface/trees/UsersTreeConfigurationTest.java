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
package info.magnolia.module.admininterface.trees;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import info.magnolia.cms.gui.control.Tree;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.mockito.InOrder;


/**
 * Tests for UsersTreeConfiguration.prepareFunctionBar method
 */
public class UsersTreeConfigurationTest extends TestCase {

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private Tree tree = mock(Tree.class);
    private UsersTreeConfiguration utc = new UsersTreeConfiguration();
    private InOrder inOrder = inOrder(tree);

    public void testRootPathNull() {
        //GIVEN
        when(tree.getRootPath()).thenReturn(null);
        //WHEN
        utc.prepareFunctionBar(tree, true, request);
        //THEN
        inOrder.verify(tree).addFunctionBarItemFromContextMenu("edit");
        inOrder.verify(tree,times(1)).getRootPath();
        inOrder.verify(tree).addFunctionBarItem(null);
        inOrder.verify(tree).addFunctionBarItemFromContextMenu("activate");
        inOrder.verify(tree).addFunctionBarItemFromContextMenu("deactivate");
        inOrder.verify(tree).addFunctionBarItem(null);
    }

    public void testRootPathNotNull() {
        //GIVEN
        when(tree.getRootPath()).thenReturn("/");
        //WHEN
        utc.prepareFunctionBar(tree, true, request);
        //THEN
        inOrder.verify(tree).addFunctionBarItemFromContextMenu("edit");
        inOrder.verify(tree,times(2)).getRootPath();
        inOrder.verify(tree).addFunctionBarItemFromContextMenu("new");
        inOrder.verify(tree).addFunctionBarItemFromContextMenu("newFolder");
        inOrder.verify(tree).addFunctionBarItem(null);
        inOrder.verify(tree).addFunctionBarItemFromContextMenu("activate");
        inOrder.verify(tree).addFunctionBarItemFromContextMenu("deactivate");
        inOrder.verify(tree).addFunctionBarItem(null);
    }

    public void testRootSystemPath() {
        //GIVEN
        when(tree.getRootPath()).thenReturn("/system");
        //WHEN
        utc.prepareFunctionBar(tree, true, request);
        //THEN
        inOrder.verify(tree).addFunctionBarItemFromContextMenu("edit");
        inOrder.verify(tree,times(2)).getRootPath();
        inOrder.verify(tree).addFunctionBarItem(null);
        inOrder.verify(tree).addFunctionBarItemFromContextMenu("activate");
        inOrder.verify(tree).addFunctionBarItemFromContextMenu("deactivate");
        inOrder.verify(tree).addFunctionBarItem(null);
    }
}
