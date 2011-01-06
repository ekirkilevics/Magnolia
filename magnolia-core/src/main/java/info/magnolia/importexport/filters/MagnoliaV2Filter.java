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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * SAX filter, strips version information from a JCR XML (system view).
 */
public class MagnoliaV2Filter extends XMLFilterImpl {

    /**
     * if != 0 we are in the middle of a filtered element.
     */
    private int inMetadataElement;

    private boolean skipNode;

    private boolean skipProperty;

    private boolean skipWs;
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
            skipWs = true; // skip additional whitespace after skipped tag
            return;
        }

        if (skipProperty) {
            if ("sv:property".equals(qName)) {
                skipProperty = false;
                skipWs = true; // skip additional whitespace after skipped tag
            }
            return;
        }

        super.endElement(uri, localName, qName);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        // do not emit text inside of skipped elements
        if (!(skipNode || skipProperty)) {
            while ( skipWs && length>0
                    && Character.isWhitespace(ch[start]) ){
                start ++;
                length--;
            }
            if ( length>0){
                super.characters(ch, start, length);
            }
        }
        skipWs = false;
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
            // MAGNOILA-2653 skip nested elements (sv:value)
            if ( skipProperty ){
                return;
            }
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
