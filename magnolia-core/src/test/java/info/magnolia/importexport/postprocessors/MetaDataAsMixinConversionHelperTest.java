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
package info.magnolia.importexport.postprocessors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.jcr.MockSession;

/**
 * Test case for {@link MetaDataAsMixinConversionHelper}.
 */
public class MetaDataAsMixinConversionHelperTest {

    @Test
    public void testRenamesDeletedOnProperty() throws RepositoryException, IOException {
        // GIVEN
        MockSession session = loadPropertiesFile("renamesDeletedOnProperty");

        assertTrue(session.nodeExists("/parent"));
        assertTrue(session.propertyExists("/parent/mgnl:deletedOn"));
        assertFalse(session.propertyExists("/parent/mgnl:deleted"));

        // WHEN
        MetaDataAsMixinConversionHelper conversionHelper = new MetaDataAsMixinConversionHelper();
        conversionHelper.setDeleteMetaDataIfEmptied(true);
        conversionHelper.convertNodeAndChildren(session.getRootNode());

        // THEN
        assertTrue(session.nodeExists("/parent"));
        assertFalse(session.propertyExists("/parent/mgnl:deletedOn"));
        assertTrue(session.propertyExists("/parent/mgnl:deleted"));

        assertWorkspaceEquals(session, "renamesDeletedOnProperty");
    }

    @Test
    public void testRemovesMetaDataWhenEmptied() throws RepositoryException, IOException {

        // GIVEN
        MockSession session = loadPropertiesFile("removesMetaDataWhenEmptied");

        assertTrue(session.nodeExists("/parent"));
        assertTrue(session.nodeExists("/parent/MetaData"));

        // WHEN
        MetaDataAsMixinConversionHelper conversionHelper = new MetaDataAsMixinConversionHelper();
        conversionHelper.setDeleteMetaDataIfEmptied(true);
        conversionHelper.convertNodeAndChildren(session.getRootNode());

        // THEN
        assertTrue(session.nodeExists("/parent"));
        assertFalse(session.nodeExists("/parent/MetaData"));
        assertTrue(session.nodeExists("/parent/child/MetaData"));
        assertTrue(session.propertyExists("/parent/child/MetaData/mgnl:customProperty"));

        assertWorkspaceEquals(session, "removesMetaDataWhenEmptied");
    }

    @Test
    public void testConversionWhenPropertiesAreAlreadyInPlace() throws RepositoryException, IOException {

        // GIVEN
        MockSession session = loadPropertiesFile("propertiesAlreadyInPlace");

        // WHEN
        MetaDataAsMixinConversionHelper conversionHelper = new MetaDataAsMixinConversionHelper();
        conversionHelper.setDeleteMetaDataIfEmptied(true);
        conversionHelper.convertNodeAndChildren(session.getRootNode());

        // THEN
        assertWorkspaceEquals(session, "propertiesAlreadyInPlace");
    }

    private MockSession loadPropertiesFile(String name) throws IOException, RepositoryException {
        MockSession session = new MockSession(RepositoryConstants.WEBSITE);
        new PropertiesImportExport().createNodes(session.getRootNode(), getClass().getResourceAsStream("test-MetaDataAsMixinConversionHelper-" + name + ".properties"));
        return session;
    }

    private void assertWorkspaceEquals(MockSession session, String name) throws RepositoryException, IOException {
        Properties expectedProperties = new Properties();
        expectedProperties.load(getClass().getResourceAsStream("test-MetaDataAsMixinConversionHelper-" + name + "-expected.properties"));
        Properties actualProperties = new PropertiesImportExport().toProperties(session.getRootNode(), new AbstractPredicate<Node>() {
            @Override
            public boolean evaluateTyped(Node node) {
                return true;
            }
        });
        assertEquals(propertiesToString(expectedProperties), propertiesToString(actualProperties));
    }

    private String propertiesToString(Properties properties) {
        ArrayList<String> keys = new ArrayList<String>();
        for (Object key : properties.keySet()) {
            if (!((String) key).endsWith("@uuid")) {
                keys.add((String) key);
            }
        }
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(key).append("=").append(properties.get(key)).append("\n");
        }
        return sb.toString();
    }
}
