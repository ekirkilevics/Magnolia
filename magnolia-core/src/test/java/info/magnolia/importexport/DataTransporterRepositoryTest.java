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
package info.magnolia.importexport;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.jcr.ImportUUIDBehavior;

import org.junit.Test;

import info.magnolia.test.RepositoryTestCase;

import static org.junit.Assert.*;

/**
 * @author ochytil
 */
public class DataTransporterRepositoryTest extends RepositoryTestCase{

    private static final String EXCEPTION = "java.lang.RuntimeException: Error importing test-stream: DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.";

    @Test
    public void testDoctypeImport() throws Exception{

        File inputFile = new File(getClass().getResource("/test-doctype.xml").getFile());

        InputStream inputStream = new FileInputStream(inputFile);

        try{
            DataTransporter.importXmlStream(inputStream, "website", "/", "test-stream", false, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, false, false);
        }catch(Exception e){
            assertEquals(EXCEPTION, e.toString());
        }
    }
}