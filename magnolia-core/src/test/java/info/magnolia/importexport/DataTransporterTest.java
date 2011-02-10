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
package info.magnolia.importexport;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.test.TestMagnoliaConfigurationProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLTestCase;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.Properties;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class DataTransporterTest extends XMLTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Properties properties = new Properties();
        properties.put("magnolia.export.keep_extra_namespaces", "false");
        SystemProperty.setMagnoliaConfigurationProperties(new TestMagnoliaConfigurationProperties(properties));
    }

    @Override
    protected void tearDown() throws Exception {
        SystemProperty.clear();
        super.tearDown();
    }

    public void testParseAndFormat() throws Exception {
        File inputFile = new File(getClass().getResource("/test-formatted-input.xml").getFile());
        File outputFile = File.createTempFile("export-test-", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
        OutputStream outputStream = new FileOutputStream(outputFile);

        XMLReader reader = XMLReaderFactory.createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName());

        DataTransporter.readFormatted(reader, inputFile, outputStream);

        IOUtils.closeQuietly(outputStream);

        Reader expectedReader = new InputStreamReader(getClass().getResourceAsStream("/test-formatted-expected.xml"));
        Reader actualReader = new FileReader(outputFile);

        DetailedDiff xmlDiff = new DetailedDiff(new Diff(expectedReader, actualReader));

        IOUtils.closeQuietly(expectedReader);
        IOUtils.closeQuietly(actualReader);
        outputFile.delete();

        final StringBuilder diffLog = new StringBuilder();
        for (Iterator iter = xmlDiff.getAllDifferences().iterator(); iter.hasNext();) {
            Difference difference = (Difference) iter.next();
            diffLog.append("expected> ").append(difference.getControlNodeDetail().getValue()).append("\n");
            diffLog.append("actual  > ").append(difference.getTestNodeDetail().getValue()).append("\n");
        }

        assertTrue("Document is not formatted as expected:\n" + diffLog.toString(), xmlDiff.identical());
    }

    public void testRemoveNs() throws Exception {
        InputStream input = getClass().getResourceAsStream("/test-unwantedns.xml");
        File outputFile = File.createTempFile("export-test-", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
        OutputStream outputStream = new FileOutputStream(outputFile);

        XMLReader reader = XMLReaderFactory.createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName());

        DataTransporter.readFormatted(reader, input, outputStream);

        IOUtils.closeQuietly(outputStream);

        String result = FileUtils.readFileToString(outputFile);
        outputFile.delete();

        assertFalse("'removeme' namespace still found in output file", StringUtils.contains(result, "xmlns:removeme"));
        assertTrue("'sv' namespace not found in output file", StringUtils.contains(result, "xmlns:sv"));
        assertTrue("'xsi' namespace not found in output file", StringUtils.contains(result, "xmlns:xsi"));
    }
}
