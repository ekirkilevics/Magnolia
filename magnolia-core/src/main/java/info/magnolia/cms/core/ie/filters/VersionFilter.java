package info.magnolia.cms.core.ie.filters;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;


/**
 * Sax filter, strips version information from a jcr xml (system view).
 */
public class VersionFilter extends XMLFilterImpl {

    /**
     * if != 0 we are in the middle of a filtered element.
     */
    private int inVersionElement;

    /**
     * Instantiates a new version filter.
     * @param parent wrapped XMLReader
     */
    public VersionFilter(XMLReader parent) {
        super(parent);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (inVersionElement > 0) {
            inVersionElement--;
            return;
        }

        super.endElement(uri, localName, qName);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        // filter content
        if (inVersionElement == 0) {
            super.characters(ch, start, length);
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(String, String, String, Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

        if (inVersionElement > 0) {
            inVersionElement++;
            return;
        }
        if ("sv:property".equals(qName)) { //$NON-NLS-1$
            String attName = atts.getValue("sv:name"); //$NON-NLS-1$
            if (attName != null
                && ("jcr:predecessors".equals(attName) || "jcr:baseVersion".equals(attName) || "jcr:versionHistory" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                .equals(attName))) {
                inVersionElement++;
                return;
            }
        }

        super.startElement(uri, localName, qName, atts);
    }

}