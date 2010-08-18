/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.ArrayList;
import java.util.List;


/**
 * A base abstract filter that can be sub-classed in order to easily implement removal of properties based on their
 * name/content.
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public abstract class SkipNodePropertyFilter extends XMLFilterImpl {

    /**
     * Logger.
     */
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected String lastNodeName;

    protected boolean skipProperty;

    protected boolean invalue;

    private List elementBuffer = new ArrayList();

    /**
     * Instantiates a new filter.
     * @param parent wrapped XMLReader
     */
    public SkipNodePropertyFilter(XMLReader parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (skipProperty) {
            if ("sv:property".equals(qName)) {
                skipProperty = false;
                invalue = false;
                elementBuffer.clear();
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
        else {
            if (invalue) {
                invalue = false;
                // Arrays.copyOfRange(ch, start, start + length)
                char[] range = new char[length];
                System.arraycopy(ch, start, range, 0, length);
                String textContent = new String(range);

                // skip only if filter() say so
                boolean skip = filter(textContent, lastNodeName);

                if (!skip) {
                    while (!elementBuffer.isEmpty()) {
                        BufferedElement be = (BufferedElement) elementBuffer.remove(0);
                        super.startElement(be.getUri(), be.getLocalName(), be.getQName(), be.getAtts());
                    }
                    super.characters(ch, start, length);
                    skipProperty = false;
                }
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

        String svname = atts.getValue("sv:name");
        if ("sv:node".equals(qName)) {
            lastNodeName = svname;
        }
        else if ("sv:property".equals(qName) && (getFilteredPropertyName().equals(svname))) {
            elementBuffer.add(new BufferedElement(uri, localName, qName, new AttributesImpl(atts)));

            skipProperty = true;
            invalue = false;
        }
        else if (skipProperty && "sv:value".equals(qName)) {
            elementBuffer.add(new BufferedElement(uri, localName, qName, new AttributesImpl(atts)));
            invalue = true;
        }

        if (skipProperty) {
            return;
        }
        super.startElement(uri, localName, qName, atts);

    }

    /**
     * Implement this method to specify the name of the property you want to filter.
     * @return filtered property name
     */
    protected abstract String getFilteredPropertyName();

    /**
     * Implement this method to specificy if a given property (given its value and the parent node name) should be
     * removed.
     * @param propertyValue property value
     * @param parentNodeName parent node name
     * @return <code>true</code> if this property should be removed
     */
    protected abstract boolean filter(String propertyValue, String parentNodeName);

    /**
     * Temporary element storage node.
     */
    public static class BufferedElement {

        private String uri;

        private String localName;

        private String qName;

        private Attributes atts;

        /**
         * @param atts
         * @param localName
         * @param name
         * @param uri
         */
        public BufferedElement(String uri, String localName, String qName, Attributes atts) {
            this.atts = atts;
            this.localName = localName;
            this.qName = qName;
            this.uri = uri;
        }

        /**
         * Returns the uri.
         * @return the uri
         */
        public String getUri() {
            return uri;
        }

        /**
         * Sets the uri.
         * @param uri the uri to set
         */
        public void setUri(String uri) {
            this.uri = uri;
        }

        /**
         * Returns the localName.
         * @return the localName
         */
        public String getLocalName() {
            return localName;
        }

        /**
         * Sets the localName.
         * @param localName the localName to set
         */
        public void setLocalName(String localName) {
            this.localName = localName;
        }

        /**
         * Returns the qName.
         * @return the qName
         */
        public String getQName() {
            return qName;
        }

        /**
         * Sets the qName.
         * @param name the qName to set
         */
        public void setQName(String name) {
            qName = name;
        }

        /**
         * Returns the atts.
         * @return the atts
         */
        public Attributes getAtts() {
            return atts;
        }

        /**
         * Sets the atts.
         * @param atts the atts to set
         */
        public void setAtts(Attributes atts) {
            this.atts = atts;
        }
    }
}
