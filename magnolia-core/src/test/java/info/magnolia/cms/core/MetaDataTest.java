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

import static org.junit.Assert.assertEquals;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.test.mock.jcr.MockNode;

import java.util.Calendar;

import javax.jcr.RepositoryException;

import org.junit.Test;

/**
 * @version $Id$
 */
public class MetaDataTest {

    @Test
    public void testSetPropertyWithString() throws RepositoryException{
        // GIVEN
        final MockNode root = new MockNode();
        root.addNode(MetaData.DEFAULT_META_NODE);
        final MetaData md = MetaDataUtil.getMetaData(root);
        final String name = "name";
        final String value = "value";

        // WHEN
        md.setProperty(name, value);

        // THEN
        assertEquals(value, md.getStringProperty(name));
    }

    @Test
    public void testSetPropertyWithStringWhenAlreadyExisting() throws RepositoryException{
        // GIVEN
        final MockNode root = new MockNode();
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

    @Test
    public void testSetPropertyWithDouble() throws RepositoryException{
        // GIVEN
        final MockNode root = new MockNode();
        root.addNode(MetaData.DEFAULT_META_NODE);
        final MetaData md = MetaDataUtil.getMetaData(root);
        final String name = "name";
        final double value = 12d;

        // WHEN
        md.setProperty(name, value);

        // THEN
        assertEquals(value, md.getDoubleProperty(name), 0d);
    }

    @Test
    public void testSetPropertyWithLong() throws RepositoryException{
        // GIVEN
        final MockNode root = new MockNode();
        root.addNode(MetaData.DEFAULT_META_NODE);
        final MetaData md = MetaDataUtil.getMetaData(root);
        final String name = "name";
        final long value = 12l;

        // WHEN
        md.setProperty(name, value);

        // THEN
        assertEquals(value, md.getLongProperty(name));
    }

    @Test
    public void testSetPropertyWithBoolean() throws RepositoryException{
        // GIVEN
        final MockNode root = new MockNode();
        root.addNode(MetaData.DEFAULT_META_NODE);
        final MetaData md = MetaDataUtil.getMetaData(root);
        final String name = "name";
        final boolean value = false;

        // WHEN
        md.setProperty(name, value);

        // THEN
        assertEquals(value, md.getBooleanProperty(name));
    }

    @Test
    public void testSetPropertyWithDate() throws RepositoryException{
        // GIVEN
        final MockNode root = new MockNode();
        root.addNode(MetaData.DEFAULT_META_NODE);
        final MetaData md = MetaDataUtil.getMetaData(root);
        final String name = "name";
        final Calendar value = Calendar.getInstance();

        // WHEN
        md.setProperty(name, value);

        // THEN
        assertEquals(value, md.getDateProperty(name));
    }

}
