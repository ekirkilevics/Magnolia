package info.magnolia.cms.core.ie.filters;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;


/**
 * A simple filter that strips jcr:uuid properties in MetaData nodes. Needed due to MAGNOLIA-1650 uuids in MetaData
 * nodes are changed during import.
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class MetadataUuidFilter extends XMLFilterImpl {

    /**
     * if != 0 we are in the middle of a filtered element.
     */
    private int inMetadataElement;

    private boolean skipProperty;

    /**
     * Instantiates a new MetadataUuidFilter filter.
     * @param parent wrapped XMLReader
     */
    public MetadataUuidFilter(XMLReader parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (inMetadataElement > 0) {
            inMetadataElement--;
        }

        if (skipProperty) {
            if ("sv:property".equals(qName)) {
                skipProperty = false;
            }
            return;
        }

        super.endElement(uri, localName, qName);
    }

    /**
     * {@inheritDoc}
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (!skipProperty) {
            super.characters(ch, start, length);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

        if (inMetadataElement > 0) {
            inMetadataElement++;
        }

        String svname = atts.getValue("sv:name");

        if ("sv:node".equals(qName) && "MetaData".equals(svname)) {
            inMetadataElement++;
        }

        if (inMetadataElement > 0) {

            if ("sv:property".equals(qName) && ("jcr:uuid".equals(svname))) {
                skipProperty = true;
            }

            if (skipProperty) {
                return;
            }
        }

        super.startElement(uri, localName, qName, atts);

    }
}