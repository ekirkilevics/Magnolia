/**
 * This file Copyright (c) 2003-2012 Magnolia International
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


import java.util.Calendar;
import java.util.Date;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.jcr.util.NodeTypes;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import info.magnolia.cms.exchange.ActivationUtil;
import info.magnolia.test.mock.jcr.MockNode;

/**
 * Tests for {@link MetaData}.
 */
public class MetaDataTest {

    private Node root;

    @Before
    public void setUp() throws Exception {
        root = new MockNode();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTitleThrowsException() {
        new MetaData(root).getTitle();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTitleThrowsException() {
        new MetaData(root).setTitle("Title");
    }

    @Test
    public void testSetCreationDate() throws RepositoryException {
        new MetaData(root).setCreationDate();
        assertTrue(root.hasProperty(NodeTypes.CreatedMixin.CREATED));
    }

    @Test
    public void testGetCreationDate() throws RepositoryException {
        Calendar expected = Calendar.getInstance();
        root.setProperty(NodeTypes.CreatedMixin.CREATED, expected);
        Calendar actual = new MetaData(root).getCreationDate();
        assertEquals(expected.getTimeInMillis(), actual.getTimeInMillis());
    }

    @Test
    public void testSetActivated() throws RepositoryException {
        new MetaData(root).setActivated();
        assertTrue(root.getProperty(NodeTypes.ActivatableMixin.ACTIVATION_STATUS).getBoolean());
    }

    @Test
    public void testSetUnActivated() throws RepositoryException {
        root.setProperty(NodeTypes.ActivatableMixin.ACTIVATION_STATUS, true);
        new MetaData(root).setUnActivated();
        assertFalse(root.getProperty(NodeTypes.ActivatableMixin.ACTIVATION_STATUS).getBoolean());
    }

    @Test
    public void testGetIsActivated() throws RepositoryException {
        assertFalse(new MetaData(root).getIsActivated());
        root.setProperty(NodeTypes.ActivatableMixin.ACTIVATION_STATUS, true);
        assertTrue(new MetaData(root).getIsActivated());
    }

    @Test
    public void testGetActivationStatusReturnsNotActivatedWhenNotActivated() {
        assertEquals(ActivationUtil.ACTIVATION_STATUS_NOT_ACTIVATED, new MetaData(root).getActivationStatus());
    }

    @Test
    public void testGetActivationStatusReturnsActivatedWhenActivatedAndNeverModified() throws RepositoryException {
        root.setProperty(NodeTypes.ActivatableMixin.ACTIVATION_STATUS, true);
        assertEquals(ActivationUtil.ACTIVATION_STATUS_ACTIVATED, new MetaData(root).getActivationStatus());
    }

    @Test
    public void testGetActivationStatusReturnsActivatedWhenActivatedAndNotSubsequentlyModified() throws RepositoryException {
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(DateUtils.addDays(new Date(), -1));

        root.setProperty(NodeTypes.ActivatableMixin.ACTIVATION_STATUS, true);
        root.setProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED, today);
        root.setProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED, yesterday);
        assertEquals(ActivationUtil.ACTIVATION_STATUS_ACTIVATED, new MetaData(root).getActivationStatus());
    }

    @Test
    public void testGetActivationStatusReturnsActivatedWhenActivatedAndThenModified() throws RepositoryException {
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(DateUtils.addDays(new Date(), -1));

        root.setProperty(NodeTypes.ActivatableMixin.ACTIVATION_STATUS, true);
        root.setProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED, yesterday);
        root.setProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED, today);
        assertEquals(ActivationUtil.ACTIVATION_STATUS_MODIFIED, new MetaData(root).getActivationStatus());
    }

    @Test
    public void testSetLastActivationActionDate() throws RepositoryException {
        assertFalse(root.hasProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED));
        new MetaData(root).setLastActivationActionDate();
        assertTrue(root.hasProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED));
    }

    @Test
    public void testGetLastActionDate() throws RepositoryException {

        assertFalse(root.hasProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED));
        assertNull(new MetaData(root).getLastActionDate());

        root.setProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED, Calendar.getInstance());

        assertTrue(root.hasProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED));
        assertNotNull(new MetaData(root).getLastActionDate());
    }

    @Test
    public void testSetModificationDate() throws RepositoryException {
        assertFalse(root.hasProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED));
        new MetaData(root).setModificationDate();
        assertTrue(root.hasProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED));
    }

    @Test
    public void testGetModificationDate() throws RepositoryException {

        assertFalse(root.hasProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED));
        assertNull(new MetaData(root).getModificationDate());

        root.setProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED, Calendar.getInstance());

        assertTrue(root.hasProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED));
        assertNotNull(new MetaData(root).getModificationDate());
    }

    @Test
    public void testGetAuthorId() throws RepositoryException {

        assertFalse(root.hasProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED_BY));
        assertEquals("", new MetaData(root).getAuthorId());

        root.setProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED_BY, "superuser");

        assertTrue(root.hasProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED_BY));
        assertNotNull(new MetaData(root).getAuthorId());
    }

    @Test
    public void testSetAuthorId() throws RepositoryException {
        assertFalse(root.hasProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED_BY));
        new MetaData(root).setAuthorId("superuser");
        assertTrue(root.hasProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED_BY));
    }

    @Test
    public void testGetActivatorId() throws RepositoryException {

        assertFalse(root.hasProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED_BY));
        assertEquals("", new MetaData(root).getActivatorId());

        root.setProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED_BY, "superuser");

        assertTrue(root.hasProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED_BY));
        assertNotNull(new MetaData(root).getActivatorId());
    }

    @Test
    public void testSetActivatorId() throws RepositoryException {
        assertFalse(root.hasProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED_BY));
        new MetaData(root).setActivatorId("superuser");
        assertTrue(root.hasProperty(NodeTypes.ActivatableMixin.LAST_ACTIVATED_BY));
    }

    @Test
    public void testGetTemplate() throws RepositoryException {
        assertFalse(root.hasProperty(NodeTypes.RenderableMixin.TEMPLATE));
        assertEquals("", new MetaData(root).getTemplate());

        root.setProperty(NodeTypes.RenderableMixin.TEMPLATE, "samples:pages/main");

        assertTrue(root.hasProperty(NodeTypes.RenderableMixin.TEMPLATE));
        assertNotNull(new MetaData(root).getTemplate());
    }

    @Test
    public void testSetTemplate() throws RepositoryException {
        assertFalse(root.hasProperty(NodeTypes.RenderableMixin.TEMPLATE));
        new MetaData(root).setTemplate("samples:pages/main");
        assertTrue(root.hasProperty(NodeTypes.RenderableMixin.TEMPLATE));
    }

    @Test
    public void testSetPropertyWithString() throws RepositoryException {
        // GIVEN
        final MetaData md = new MetaData(root);
        final String value = "value";

        // WHEN
        md.setProperty(MetaData.TEMPLATE, value);

        // THEN
        assertEquals(value, md.getStringProperty(MetaData.TEMPLATE));
        assertTrue(root.hasProperty(NodeTypes.RenderableMixin.TEMPLATE));
    }

    /**
     * None of the supported properties are of type long.
     */
    @Test
    public void testSetPropertyWithDouble() {
        // GIVEN
        final MetaData md = new MetaData(root);
        final double value = 12d;

        // WHEN
        md.setProperty(MetaData.TEMPLATE, value);

        // THEN
        assertEquals(value, md.getDoubleProperty(MetaData.TEMPLATE), 0d);
    }

    /**
     * None of the supported properties are of type long.
     */
    @Test
    public void testSetPropertyWithLong() {
        // GIVEN
        final MetaData md = new MetaData(root);
        final long value = 12l;

        // WHEN
        md.setProperty(MetaData.TEMPLATE, value);

        // THEN
        assertEquals(value, md.getLongProperty(MetaData.TEMPLATE));
    }

    @Test
    public void testSetPropertyWithBoolean() throws RepositoryException {
        // GIVEN
        final MetaData md = new MetaData(root);
        final boolean value = false;

        // WHEN
        md.setProperty(MetaData.ACTIVATED, value);

        // THEN
        assertEquals(value, md.getBooleanProperty(MetaData.ACTIVATED));
        assertTrue(root.hasProperty(NodeTypes.ActivatableMixin.ACTIVATION_STATUS));
    }

    @Test
    public void testSetPropertyWithDate() throws RepositoryException {
        // GIVEN
        final MetaData md = new MetaData(root);
        final Calendar value = Calendar.getInstance();

        // WHEN
        md.setProperty(MetaData.CREATION_DATE, value);

        // THEN
        assertEquals(value, md.getDateProperty(MetaData.CREATION_DATE));
        assertTrue(root.hasProperty(NodeTypes.CreatedMixin.CREATED));
    }

    @Test
    public void testGetStringProperty() throws RepositoryException {
        // GIVEN
        final String value = "value";
        final MetaData md = new MetaData(root);
        md.setProperty(MetaData.TEMPLATE, value);

        // WHEN
        final String result = md.getStringProperty(MetaData.TEMPLATE);

        // THEN
        assertEquals(value, result);
        assertTrue(root.hasProperty(NodeTypes.RenderableMixin.TEMPLATE));
    }

    @Test
    public void testSetPropertyWithStringWhenAlreadyExisting() throws RepositoryException {
        // GIVEN
        final MetaData md = new MetaData(root);
        final String value = "value";
        root.setProperty(NodeTypes.RenderableMixin.TEMPLATE, value);

        final String newValue = "newValue";

        // WHEN
        md.setProperty(MetaData.TEMPLATE, newValue);

        // THEN
        assertEquals(newValue, md.getStringProperty(MetaData.TEMPLATE));
        assertEquals(newValue, root.getProperty(NodeTypes.RenderableMixin.TEMPLATE).getString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStringPropertyWithUnsupportedName() {
        new MetaData(root).setProperty("foo", "bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStringPropertyWithUnsupportedName() {
        new MetaData(root).getStringProperty("foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStringPropertyWithUnsupportedTitleProperty() {
        new MetaData(root).setProperty("mgnl:title", "bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStringPropertyWithUnsupportedTitleProperty() {
        new MetaData(root).getStringProperty("mgnl:title");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStringPropertyWithUnsupportedTemplateTypeProperty() {
        new MetaData(root).setProperty("mgnl:templatetype", "bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStringPropertyWithUnsupportedTemplateTypeProperty() {
        new MetaData(root).getStringProperty("mgnl:templatetype");
    }

    @Test
    public void testRemoveProperty() throws RepositoryException {
        root.setProperty(NodeTypes.RenderableMixin.TEMPLATE, "samples:pages/main");
        new MetaData(root).removeProperty(MetaData.TEMPLATE);
        assertFalse(root.hasProperty(NodeTypes.RenderableMixin.TEMPLATE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemovePropertyWithUnsupportedProperty() throws RepositoryException {
        new MetaData(root).removeProperty(MetaData.TEMPLATE_TYPE);
    }
}
