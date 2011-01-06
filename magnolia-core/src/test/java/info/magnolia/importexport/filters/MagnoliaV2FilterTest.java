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
package info.magnolia.importexport.filters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Test MagnoliaV2Filter behaviour, fixed bugs, etc... Loosely based on DataTransporterTest.java
 * @author hpaluch
 */
public class MagnoliaV2FilterTest extends XMLTestCase {

    private static Logger log = LoggerFactory.getLogger(MagnoliaV2FilterTest.class);

    /**
     * Test bogus sv:value creation while replacing MetaData jcr:primary block See
     * http://jira.magnolia-cms.com/browse/MAGNOLIA-2653 for details.
     * @throws Exception
     */
    public void testBogusMetaElement() throws Exception {
        final String inputResourcePath = "/test-v2-filter.xml";

        final InputStream input = getClass().getResourceAsStream(inputResourcePath);
        assertNotNull("Can't open stream to resource " + inputResourcePath, input);
        File outputFile = File.createTempFile("v2filter-out-", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
        OutputStream os = new FileOutputStream(outputFile);

        // simulate DataTransporter Formatting
        OutputFormat outputFormat = new OutputFormat();
        outputFormat.setPreserveSpace(false); // this is ok, doesn't affect text nodes??
        outputFormat.setIndenting(true);
        outputFormat.setIndent(2 /* INDENT_VALUE */);
        outputFormat.setLineWidth(120); // need to be set after setIndenting()!

        XMLSerializer xmlSerializer = new XMLSerializer(os, outputFormat);

        // XMLReader inputXmlReader =
        // XMLReaderFactory.createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName());
        XMLReader v2Reader = XMLReaderFactory.createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName());

        MagnoliaV2Filter v2Filter = new MagnoliaV2Filter(v2Reader);
        v2Filter.setContentHandler(xmlSerializer);
        v2Filter.parse(new InputSource(input));

        IOUtils.closeQuietly(input);
        IOUtils.closeQuietly(os);

        // XXX: Fixme - UTF-8!
        final InputStream expected = getClass().getResourceAsStream(inputResourcePath);
        assertNotNull("Can't open stream to resource " + inputResourcePath, expected);
        Reader expectedReader = new InputStreamReader(expected);
        Reader actualReader = new FileReader(outputFile);

        DetailedDiff xmlDiff = new DetailedDiff(new Diff(expectedReader, actualReader));

        IOUtils.closeQuietly(expectedReader);
        IOUtils.closeQuietly(actualReader);

        for (Iterator iter = xmlDiff.getAllDifferences().iterator(); iter.hasNext();) {
            Difference d = (Difference) iter.next();
            log.warn(d.getControlNodeDetail().getXpathLocation()
                + " expected: '"
                + d.getControlNodeDetail().getValue()
                + "'");
            log
                .warn(d.getTestNodeDetail().getXpathLocation()
                    + " actual:   '"
                    + d.getTestNodeDetail().getValue()
                    + "'");

        }

        assertTrue("Document " + outputFile.getAbsolutePath() + " is not formatted as expected", xmlDiff.identical());
        outputFile.delete(); // Delete working file on success (keep for diagnostits on error )

    }

}
