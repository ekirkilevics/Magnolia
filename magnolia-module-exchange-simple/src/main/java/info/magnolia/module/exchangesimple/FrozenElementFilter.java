/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.module.exchangesimple;

import info.magnolia.importexport.filters.VersionFilter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;


/**
 * this filter converts frozen nodes to mimic actual state of a node, this is only meant to be used while activation
 * Taken from:
 * @see info.magnolia.importexport.filters.VersionFilter $Id$
 */
class FrozenElementFilter extends VersionFilter {

    /**
     * if != 0 we are in the middle of a filtered element.
     */
    private int inVersionElement;

    /**
     * original node name
     */
    private String nodeName;

    /**
     * Instantiates a new version filter.
     * @param parent wrapped XMLReader
     */
    protected FrozenElementFilter(XMLReader parent) {
        super(parent);
    }

    protected void setNodeName(String name) {
        this.nodeName = name;
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
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(String, String, String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

        if (inVersionElement > 0) {
            inVersionElement++;
            return;
        }
        if ("sv:property".equals(qName)) { //$NON-NLS-1$
            String attName = atts.getValue("sv:name"); //$NON-NLS-1$
            if (attName != null) {
                if ("jcr:predecessors".equals(attName)
                    || "jcr:baseVersion".equals(attName)
                    || "jcr:primaryType".equals(attName)
                    || "jcr:uuid".equals(attName)
                    || "jcr:mixinTypes".equals(attName)
                    || "jcr:isCheckedOut".equals(attName)
                    || "jcr:created".equals(attName)
                    || "mgnl:sequenceposition".equals(attName)
                    || "jcr:versionHistory" //$NON-NLS-1$ 
                    .equals(attName)) {
                    inVersionElement++;
                    return;
                }
                else if ("jcr:frozenPrimaryType".equals(attName)) {
                    AttributesImpl attributesImpl = new AttributesImpl(atts);
                    int index = attributesImpl.getIndex("sv:name");
                    attributesImpl.setValue(index, "jcr:primaryType");
                    atts = attributesImpl;
                }
                else if ("jcr:frozenMixinTypes".equals(attName)) {
                    AttributesImpl attributesImpl = new AttributesImpl(atts);
                    int index = attributesImpl.getIndex("sv:name");
                    attributesImpl.setValue(index, "jcr:mixinTypes");
                    atts = attributesImpl;
                }
                else if ("jcr:frozenUuid".equals(attName)) {
                    AttributesImpl attributesImpl = new AttributesImpl(atts);
                    int index = attributesImpl.getIndex("sv:name");
                    attributesImpl.setValue(index, "jcr:uuid");
                    atts = attributesImpl;
                }
            }
        }
        else if ("sv:node".equals(qName)) {
            String attName = atts.getValue("sv:name");
            if (attName != null) {
                if ("jcr:frozenNode".equals(attName)) {
                    AttributesImpl attributesImpl = new AttributesImpl(atts);
                    int index = attributesImpl.getIndex("sv:name");
                    attributesImpl.setValue(index, this.nodeName);
                    atts = attributesImpl;
                }
            }
        }

        super.startElement(uri, localName, qName, atts);
    }

}
