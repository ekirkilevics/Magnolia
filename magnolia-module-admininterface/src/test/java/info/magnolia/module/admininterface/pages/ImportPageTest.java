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
package info.magnolia.module.admininterface.pages;

import java.io.File;
import javax.jcr.ImportUUIDBehavior;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.context.AbstractContext;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockWebContext;

/**
 * Basic test to verify deleting tmp file after importing.
 */
public class ImportPageTest extends RepositoryTestCase {

    private HttpServletRequest request;
    private HttpServletResponse response;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        request = createStrictMock(HttpServletRequest.class);
        response = createStrictMock(HttpServletResponse.class);

        // Context without an AccessManager avoids permission checks
        MockWebContext context = new MockWebContext() {

            @Override
            public AccessManager getAccessManager(String workspace) {
                return null;
            }
        };
        context.setRepositoryStrategy(((AbstractContext) MgnlContext.getInstance()).getRepositoryStrategy());
        MgnlContext.setInstance(context);
    }

    @Test
    public void testXmlImport() throws Exception {

        // GIVEN
        File originalXml = new File(getClass().getResource("/test-import.xml").getFile());
        File tmpXml = new File(originalXml.getParent() + "/tmp-test-import.xml");
        FileUtils.copyFile(originalXml, tmpXml);
        Document importFile = new Document(tmpXml, "text/xml");

        ImportPage importXml = new ImportPage("import", request, response);
        importXml.setMgnlFileImport(importFile);
        importXml.setMgnlKeepVersions(true);
        importXml.setMgnlUuidBehavior(ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);

        // WHEN
        importXml.importxml();

        // THEN
        assertTrue(MgnlContext.getJCRSession("website").nodeExists("/news"));
        assertFalse(importFile.getFile().exists());
    }
}
