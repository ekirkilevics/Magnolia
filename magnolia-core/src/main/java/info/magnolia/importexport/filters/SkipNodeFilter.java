/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.importexport.filters;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * A simple filter that removes whole nodes
 *
 * @author tmiyar
 * @version $Revision: $ ($Author: $)
 */
public abstract class SkipNodeFilter extends XMLFilterImpl {

    private final static String NODE_TAG = "sv:node";
    private final static String NAME_TAG = "sv:name";

    private boolean skipping;

    private int skipDepth;

    public SkipNodeFilter(XMLReader parent) {
        super(parent);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (!skipping) {
            ignorableWhitespace(ch, start, length);
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (!skipping) {
            super.characters(ch, start, length);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

        String svname = atts.getValue(NAME_TAG);

        if (skipping) {
            skipDepth++;
            return;
        }
        if (NODE_TAG.equals(qName) && getFilteredNodeName().equals(svname)) {
            skipping = true;
            return;
        }

        super.startElement(uri, localName, qName, atts);
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        if (skipping) {
            if (skipDepth == 0) {
                skipping = false;
            } else {
                skipDepth--;
            }
            return;
        }

        super.endElement(uri, localName, qName);
    }

    /**
     * Implement this method to specify the name of the node you want to filter.
     *
     * @return filtered node name
     */
    protected abstract String getFilteredNodeName();

}
