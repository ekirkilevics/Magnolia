/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.exchangesimple;

import info.magnolia.cms.core.ie.filters.VersionFilter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;


/**
 * this filter converts frozen nodes to mimic actual state of a node, this is only meant to be used while activation
 * Taken from:
 * @see info.magnolia.cms.core.ie.filters.VersionFilter $Id$
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
