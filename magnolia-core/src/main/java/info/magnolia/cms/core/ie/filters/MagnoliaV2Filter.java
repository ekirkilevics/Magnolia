package info.magnolia.cms.core.ie.filters;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;


/**
 * Sax filter, strips version information from a jcr xml (system view).
 */
public class MagnoliaV2Filter extends XMLFilterImpl {

    /**
     * if != 0 we are in the middle of a filtered element.
     */
    private int inMetadataElement;

    private boolean skipNode;

    private boolean skipProperty;

    /**
     * Instantiates a new version filter.
     * @param parent wrapped XMLReader
     */
    public MagnoliaV2Filter(XMLReader parent) {
        super(parent);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (inMetadataElement > 0) {
            inMetadataElement--;
        }

        if (skipNode && "sv:node".equals(qName)) {
            skipNode = false;
            return;
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
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        // filter content
        // if (inMetadataElement == 0) {
        super.characters(ch, start, length);
        // }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(String, String, String, Attributes)
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
            // remove
            // <sv:node sv:name="jcr:content">
            if ("sv:node".equals(qName) && "jcr:content".equals(svname)) {
                skipNode = true;
                return;
            }
            if ("sv:property".equals(qName)
                && ("sequenceposition".equals(svname) || "jcr:primaryType".equals(svname) || "jcr:isCheckedOut"
                    .equals(svname))) {
                skipProperty = true;
                return;
            }
            if ("sv:property".equals(qName)
                && ("Data".equals(svname) || "template".equals(svname) || "authorid".equals(svname) || "title"
                    .equals(svname))) {
                atts = new AttributesImpl();
                ((AttributesImpl) atts).addAttribute(uri, "name", "sv:name", uri, "mgnl:" + svname);
                ((AttributesImpl) atts).addAttribute(uri, "type", "sv:type", uri, "String");
            }

            else if ("sv:property".equals(qName) && ("creationdate".equals(svname) || "lastmodified".equals(svname))) {
                atts = new AttributesImpl();
                ((AttributesImpl) atts).addAttribute(uri, "name", "sv:name", uri, "mgnl:" + svname);
                ((AttributesImpl) atts).addAttribute(uri, "type", "sv:type", uri, "Date");
            }

        }

        super.startElement(uri, localName, qName, atts);

        if ("sv:node".equals(qName) && "MetaData".equals(svname)) {

            // add:
            // <sv:property sv:name="jcr:primaryType" sv:type="Name">
            // <sv:value>nt:unstructured</sv:value>
            // </sv:property>

            String atturi = atts.getURI(0);
            AttributesImpl atts2 = new AttributesImpl();
            atts2.addAttribute(uri, "name", "sv:name", atturi, "jcr:primaryType");
            atts2.addAttribute(uri, "type", "sv:type", atturi, "Name");

            super.startElement(uri, "property", "sv:property", atts2);
            super.startElement(uri, "value", "sv:value", new AttributesImpl());
            super.characters(new char[]{'m', 'g', 'n', 'l', ':', 'm', 'e', 't', 'a', 'D', 'a', 't', 'a'}, 0, 13);
            super.endElement(uri, "value", "sv:value");
            super.endElement(uri, "property", "sv:property");
        }
    }
}