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
package info.magnolia.cms.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.test.MgnlTestCase;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.Path.Element;
import org.junit.Test;

public class RootOnlyPermissionTest extends MgnlTestCase {

    private final SessionImpl session = mock(SessionImpl.class);

    private final Element rootElement = mock(Element.class);
    private final Element pageElement = mock(Element.class);
    private final Element contentNodeElement = mock(Element.class);
    private final Path itemPath = mock(Path.class);
    private final Name rootName = mock(Name.class);
    private final Name pageName = mock(Name.class);
    private final ItemId itemId = mock(ItemId.class);

    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(rootElement.getName()).thenReturn(rootName);
        when(rootElement.denotesRoot()).thenReturn(true);
        when(pageElement.getName()).thenReturn(pageName);
        when(pageElement.denotesIdentifier()).thenReturn(true);
        when(pageElement.getString()).thenReturn("page");
    }

    @Test
    public void testCanRead() throws RepositoryException {
        //GIVEN
        RootOnlyPermissions permissions = new RootOnlyPermissions(session);
        when(itemPath.getElements()).thenReturn(new Element[]{rootElement});
        //WHEN
        boolean access = permissions.canRead(itemPath, itemId);
        //THEN
        assertTrue(access);
    }

    @Test
    public void testGrants() throws RepositoryException {
        //GIVEN
        RootOnlyPermissions permissions = new RootOnlyPermissions(session);
        when(itemPath.getElements()).thenReturn(new Element[]{rootElement});
        //WHEN
        boolean access = permissions.grants(itemPath, 12);
        //THEN
        assertTrue(access);
    }

    @Test
    public void testNoRootNode() throws RepositoryException {
        //GIVEN
        RootOnlyPermissions permissions = new RootOnlyPermissions(session);
        when(itemPath.getElements()).thenReturn(new Element[]{pageElement});
        //WHEN
        boolean access = permissions.grants(itemPath, 12);
        //THEN
        assertFalse(access);
    }
}
