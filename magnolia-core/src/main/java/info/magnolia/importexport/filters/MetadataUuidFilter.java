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

import org.apache.commons.lang.ArrayUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * A simple filter that strips jcr:uuid properties in MetaData nodes. Needed due to MAGNOLIA-1650 uuids in MetaData
 * nodes are changed during import. It also supports removal of non-standards namespaces, by setting the
 * <code>removeUnwantedNamespaces</code> flag in the constructor.
 *
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class MetadataUuidFilter extends XMLFilterImpl {

    /**
     * if != 0 we are in the middle of a filtered element.
     */
    private int inMetadataElement;

    private boolean skipProperty;

    private boolean inSkippedNs;

    private String[] validNs = new String[]{"sv", "xsi"};

    private boolean removeUnwantedNamespaces;

    /**
     * Instantiates a new MetadataUuidFilter filter.
     * @param parent wrapped XMLReader
     * @deprecated since 4.2.2 (MAGNOLIA-2960)
     */
    public MetadataUuidFilter(XMLReader parent) {
        super(parent);
    }

    /**
     * Instantiates a new MetadataUuidFilter filter.
     * @param parent wrapped XMLReader
     * @param removeUnwantedNamespaces if set, non standard JCR name spaces will be removed
     */
    public MetadataUuidFilter(XMLReader parent, boolean removeUnwantedNamespaces) {
        super(parent);
        this.removeUnwantedNamespaces = removeUnwantedNamespaces;
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        if (!inSkippedNs) {
            super.endPrefixMapping(prefix);
        }
        inSkippedNs = false;

    }

    /**
     * Skip unwanted name spaces, see MAGNOLIA-2756.
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (!removeUnwantedNamespaces || ArrayUtils.contains(validNs, prefix)) {
            super.startPrefixMapping(prefix, uri);
        }
        else {
            inSkippedNs = true;
        }
    }

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

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (!skipProperty) {
            super.characters(ch, start, length);
        }
    }

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
