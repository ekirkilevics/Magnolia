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
package info.magnolia.importexport.postprocess;

import java.util.Calendar;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.MgnlNodeTypeNames;
import info.magnolia.test.RepositoryTestCase;

/**
 * Test case for {@link MetaDataImportPostProcessor}.
 */
public class MetaDataImportPostProcessorTest extends RepositoryTestCase {

    @Test
    public void testMetaDataPropertiesAreConverted() throws RepositoryException {

        // GIVEN
        Session session = MgnlContext.getJCRSession("config");
        Node node = session.getRootNode().addNode("test");
        Node md = node.addNode("MetaData", MgnlNodeTypeNames.METADATA);
        Calendar calendar = Calendar.getInstance();
        md.setProperty("mgnl:creationdate", calendar);
        md.setProperty("mgnl:lastaction", calendar);
        md.setProperty("mgnl:activatorid", "superuser");
        md.setProperty("mgnl:activated", "2");
        md.setProperty("mgnl:template", "samples:pages/some/page");
        md.setProperty("mgnl:authorid", "hyperuser");
        md.setProperty("mgnl:lastmodified", calendar);
        node.setProperty("mgnl:deletedOn", calendar);

        // WHEN
        new MetaDataImportPostProcessor().postProcessNode(node);

        // THEN
        assertPropertyEquals(node, "mgnl:created", calendar);
        assertPropertyEquals(node, "mgnl:lastActivated", calendar);
        assertPropertyEquals(node, "mgnl:lastActivatedBy", "superuser");
        assertPropertyEquals(node, "mgnl:activationStatus", "2");
        assertPropertyEquals(node, "mgnl:template", "samples:pages/some/page");
        assertPropertyEquals(node, "jcr:lastModifiedBy", "hyperuser");
        assertPropertyEquals(node, "jcr:lastModified", calendar);
        assertPropertyEquals(node, "mgnl:deleted", calendar);
    }

    private void assertPropertyEquals(Node node, String relPath, String expectedValue) throws RepositoryException {
        assertTrue(node.hasProperty(relPath));
        assertEquals(node.getProperty(relPath).getValue().getString(), expectedValue);
    }

    private void assertPropertyEquals(Node node, String relPath, Calendar expectedValue) throws RepositoryException {
        assertTrue(node.hasProperty(relPath));
        assertEquals(node.getProperty(relPath).getValue().getDate().getTimeInMillis(), expectedValue.getTimeInMillis());
    }
}
