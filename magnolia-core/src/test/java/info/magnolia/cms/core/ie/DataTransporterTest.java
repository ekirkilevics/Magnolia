package info.magnolia.cms.core.ie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


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
     * {@link info.magnolia.cms.core.ie.DataTransporter#parseAndFormat(java.io.OutputStream, org.xml.sax.XMLReader, java.lang.String, java.lang.String, boolean, boolean, javax.jcr.Session)}.
     */
    public void testParseAndFormat() throws Exception {

        File inputFile = new File(getClass().getResource("/test-formatted-input.xml").getFile());
        File outputFile = File.createTempFile("export-test-", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
        OutputStream outputStream = new FileOutputStream(outputFile);

        XMLReader reader = XMLReaderFactory.createXMLReader(org.apache.xerces.parsers.SAXParser.class.getName());

        DataTransporter.readFormatted(reader, inputFile, outputStream, true);

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
