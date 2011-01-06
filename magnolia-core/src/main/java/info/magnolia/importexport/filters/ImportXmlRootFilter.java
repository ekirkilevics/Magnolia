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

import info.magnolia.cms.util.UnicodeNormalizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;


/**
 * XML Filter for cleaning up version store from imported root node files.
 */
public class ImportXmlRootFilter extends XMLFilterImpl {

    // this is true if it is an import of a file containing jcr:root node
    public boolean rootNodeFound = false;

    private boolean isRootNode = false;

    private boolean isPrimaryTypeProperty = false;

    private boolean isPrimaryTypeValue = false;

    /**
     * if != 0 we are in the middle of a filtered element.
     */
    private int inFilterElement;

    public ImportXmlRootFilter(XMLReader parent) {
        super(parent);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (inFilterElement > 0) {
            inFilterElement--;
            return;
        }

        super.endElement(uri, localName, qName);
    }

    /**
     * Root node was found should be called after parsing to check if a root node was indeed found.
     */
    public boolean rootNodeWasFound() {
        return rootNodeFound;
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        // filter content
        if (inFilterElement == 0) {
            // change primary type of root node
            if (this.isPrimaryTypeValue) {
                this.isRootNode = false;
                this.isPrimaryTypeProperty = false;
                this.isPrimaryTypeValue = false;

                super.characters("mgnl:content".toCharArray(), 0, "mgnl:content".length());
            }
            else {
                super.characters(ch, start, length);
            }
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(String, String, String, Attributes)
     */
    /*
     * (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     * org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        // filter if already in a version element
        if (inFilterElement > 0) {
            inFilterElement++;
            return;
        }

        // filter if it is the version store
        if ("sv:node".equals(qName)) { //$NON-NLS-1$
            String attName = atts.getValue("sv:name"); //$NON-NLS-1$
            attName = UnicodeNormalizer.normalizeNFC(attName);
            // remember if there was a root node presend
            if ("jcr:root".equals(attName)) {
                this.rootNodeFound = true;
                this.isRootNode = true;
            }

            if ("jcr:system".equals(attName)) { //$NON-NLS-1$
                inFilterElement++;
                return;
            }
        }

        // change the nodetype of the jcr:root node
        if (this.isRootNode && "sv:property".equals(qName) && "jcr:primaryType".equals(atts.getValue("sv:name"))) {
            this.isPrimaryTypeProperty = true;
        }

        if (this.isPrimaryTypeProperty && "sv:value".equals(qName)) {
            this.isPrimaryTypeValue = true;
        }

        super.startElement(uri, localName, qName, atts);
    }
}
