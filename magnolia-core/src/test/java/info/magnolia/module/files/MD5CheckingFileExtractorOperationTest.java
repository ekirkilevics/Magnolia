/**
 * This file Copyright (c) 2009-2012 Magnolia International
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
package info.magnolia.module.files;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * @version $Id$
 */
public class MD5CheckingFileExtractorOperationTest {
    @Test
    public void testAbsentFilesAreRecreated() throws Exception {
        // GIVEN
        final String resourcePath = "/info/magnolia/test/mock/testcontent.properties";
        final String fileInfoNodePath = "/server/install" + resourcePath;

        assertNotNull(getClass().getResourceAsStream(resourcePath));

        // We need a randomly named (to enable test to be run in parallel), not yet physically existing file
        final File testOut = File.createTempFile("MD5-temp", null);
        // remove the file but keep the reference - file will be (re-)created by the class under test
        testOut.delete();

        // no need to keep file after test so delete when VM exits
        testOut.deleteOnExit();

        final HierarchyManager hm = createStrictMock(HierarchyManager.class);
        final Content fileInfoNode = createStrictMock(Content.class);
        final NodeData md5 = createStrictMock(NodeData.class);
        // the file had already been extracted:
        expect(hm.isExist(fileInfoNodePath)).andReturn(true);
        expect(hm.getContent(fileInfoNodePath)).andReturn(fileInfoNode);
        expect(fileInfoNode.hasNodeData("md5")).andReturn(true);
        expect(fileInfoNode.getNodeData("md5")).andReturn(md5);
        md5.setValue(isA(String.class));

        final MD5CheckingFileExtractorOperation op = new MD5CheckingFileExtractorOperation(new FileExtractionLogger() {
            @Override
            public void error(String message) {
                fail(message);
            }
        }, hm, resourcePath, testOut.getAbsolutePath());

        replay(hm, fileInfoNode, md5);

        // WHEN
        op.extract();

        verify(hm, fileInfoNode, md5);

        // THEN
        assertEquals("File should have been re-extracted", true, testOut.exists());
        assertEquals(IOUtils.toString(new FileInputStream(testOut)), IOUtils.toString(getClass().getResourceAsStream(resourcePath)));
    }
}
