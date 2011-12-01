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
package info.magnolia.cms.core;


import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.jcr.MockNode;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class MetaDataTest {

    private static final String PROPERTY_NAME = "testProperty";
    private Node root;

    @Before
    public void setUp() throws Exception {
        root = new MockNode();
        root.addNode(MetaData.DEFAULT_META_NODE);
    }

    @Test
    public void testSetPropertyWithString() {
        // GIVEN
        final MetaData md = MetaDataUtil.getMetaData(root);
        final String value = "value";

        // WHEN
        md.setProperty(PROPERTY_NAME, value);

        // THEN
        assertEquals(value, md.getStringProperty(PROPERTY_NAME));
    }

    @Test
    public void testSetPropertyWithDouble() throws RepositoryException{
        // GIVEN
        final MetaData md = MetaDataUtil.getMetaData(root);
        final double value = 12d;

        // WHEN
        md.setProperty(PROPERTY_NAME, value);

        // THEN
        assertEquals(value, md.getDoubleProperty(PROPERTY_NAME), 0d);
    }

    @Test
    public void testSetPropertyWithLong() throws RepositoryException{
        // GIVEN
        final MetaData md = MetaDataUtil.getMetaData(root);
        final long value = 12l;

        // WHEN
        md.setProperty(PROPERTY_NAME, value);

        // THEN
        assertEquals(value, md.getLongProperty(PROPERTY_NAME));
    }

    @Test
    public void testSetPropertyWithBoolean() throws RepositoryException{
        // GIVEN
        final MetaData md = MetaDataUtil.getMetaData(root);
        final boolean value = false;

        // WHEN
        md.setProperty(PROPERTY_NAME, value);

        // THEN
        assertEquals(value, md.getBooleanProperty(PROPERTY_NAME));
    }

    @Test
    public void testSetPropertyWithDate() throws RepositoryException{
        // GIVEN
        final MetaData md = MetaDataUtil.getMetaData(root);
        final Calendar value = Calendar.getInstance();

        // WHEN
        md.setProperty(PROPERTY_NAME, value);

        // THEN
        assertEquals(value, md.getDateProperty(PROPERTY_NAME));
    }

    @Test
    public void testGetStringProperty() throws RepositoryException{
        // GIVEN
        final String value = "value";
        final MetaData md = MetaDataUtil.getMetaData(root);
        final Node mdNode = root.getNode(MetaData.DEFAULT_META_NODE);
        mdNode.setProperty(RepositoryConstants.NAMESPACE_PREFIX + ":" + PROPERTY_NAME, value);

        // WHEN
        final String result = md.getStringProperty(PROPERTY_NAME);

        // THEN
        assertEquals(value, result);
    }

    @Test
    public void testGetStringPropertyThrowingPathNotFoundException() throws RepositoryException{
        // GIVEN
        Node metaDataNode = mock(Node.class);
        final MetaData md = MetaDataUtil.getMetaData(metaDataNode);

        when(metaDataNode.getProperty(RepositoryConstants.NAMESPACE_PREFIX + ":" + PROPERTY_NAME)).thenThrow(new PathNotFoundException("TEST"));

        // WHEN
        final String result = md.getStringProperty(PROPERTY_NAME);

        // THEN
        assertEquals("", result);
    }

    @Test
    public void testSetPropertyWithStringWhenAlreadyExisting() throws RepositoryException{
        // GIVEN
        root.addNode(MetaData.DEFAULT_META_NODE);
        final MetaData md = MetaDataUtil.getMetaData(root);
        final String name = "name";
        final String value = "value";
        md.setProperty(name, value);

        final String newValue = "newValue";

        // WHEN
        md.setProperty(name, newValue);

        // THEN
        assertEquals(newValue, md.getStringProperty(name));
    }
}
