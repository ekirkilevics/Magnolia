/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.importexport;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Iterator;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class DataTransporterTest extends XMLTestCase {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DataTransporterTest.class);

    /**
     * Test method for
     * {@link info.magnolia.importexport.DataTransporter#readFormatted(org.xml.sax.XMLReader, java.io.File, java.io.OutputStream)}.
     */
    public void testParseAndFormat() throws Exception {

        File inputFile = new File(getClass().getResource("/test-formatted-input.xml").getFile());
        File outputFile = File.createTempFile("export-test-", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
        OutputStream outputStream = new FileOutputStream(outputFile);

        XMLReader reader = XMLReaderFactory.createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName());

        DataTransporter.readFormatted(reader, inputFile, outputStream);

        IOUtils.closeQuietly(outputStream);

        Reader expectedReader = new FileReader(new File(getClass()
            .getResource("/test-formatted-expected.xml")
            .getFile()));
        Reader actualReader = new FileReader(outputFile);

        DetailedDiff xmlDiff = new DetailedDiff(new Diff(expectedReader, actualReader));

        IOUtils.closeQuietly(expectedReader);
        IOUtils.closeQuietly(actualReader);
        outputFile.delete();

        for (Iterator iter = xmlDiff.getAllDifferences().iterator(); iter.hasNext();) {
            Difference difference = (Difference) iter.next();

            log.warn("expected> " + difference.getControlNodeDetail().getValue());
            log.warn("actual  > " + difference.getTestNodeDetail().getValue());

        }

        assertTrue("Document is not formatted as expected", xmlDiff.identical());
    }

}
