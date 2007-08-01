package info.magnolia.cms.core.ie.filters;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;


/**
 * A filter that can be used to remove optional "name" attributes in template, dialogs or paragraph nodes. The name
 * attribute was required in magnolia 2, magnolia 3 makes use of the name of the containing node. This filter can be
 * used to cleanup old configurations that survived to copy and paste.
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class UselessNameFilter extends XMLFilterImpl {

    private boolean skipProperty;

    /**
     * Instantiates a new MetadataUuidFilter filter.
     * @param parent wrapped XMLReader
     */
    public UselessNameFilter(XMLReader parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {

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

        String svname = atts.getValue("sv:name");

        if ("sv:property".equals(qName) && ("name".equals(svname))) {
            skipProperty = true;
        }

        if (skipProperty) {
            return;
        }
        super.startElement(uri, localName, qName, atts);

    }
}