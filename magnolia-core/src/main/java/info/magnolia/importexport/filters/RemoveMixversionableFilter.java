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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A filter that removed "mix:versionable" from jcr:mixinTypes while importing
 * xml files. Can be used to automatically adapt version 3.5 xml files to
 * magnolia 3.6 during bootstrap or activation. You need
 * to modify DataTransporter in order to disable it (see the comments in
 * DataTransporter.importXmlStream())
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class RemoveMixversionableFilter extends XMLFilterImpl {

    private boolean inMixinTypes = false;

    private boolean inValue = false;

    private List values = new ArrayList();

    private String value;

    private Attributes atts;

    /**
     * Instantiates a new MetadataUuidFilter filter.
     * @param parent wrapped XMLReader
     */
    public RemoveMixversionableFilter(XMLReader parent) {
        super(parent);
    }

    public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
        if (name.equals("sv:property") && atts.getValue("sv:name").equals("jcr:mixinTypes")) {
            this.atts = new AttributesImpl(atts);
            inMixinTypes = true;
            values.clear();
        }
        else if (inMixinTypes && name.equals("sv:value")) {
            inValue = true;
        }
        else {
            super.startElement(uri, localName, name, atts);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inValue) {
            if(value == null){
                value = new String(ch, start, length);
            }
            else{
                value += new String(ch, start, length);
            }
        }
        else {
            super.characters(ch, start, length);
        }
    }

    public void endElement(String uri, String localName, String name) throws SAXException {
        if (inValue && name.equals("sv:value")) {
            inValue = false;
            if (!value.equals("mix:versionable")) {
                values.add(value);
            }
            value = null;
        }
        else if (inMixinTypes && name.equals("sv:property")) {
            inMixinTypes = false;
            // process the buffer
            if (values.size() > 0) {
                super.startElement(uri, "property", "sv:property", this.atts);
                for (Iterator iterator = values.iterator(); iterator.hasNext();) {
                    String value = (String) iterator.next();
                    super.startElement(uri, "value", "sv:value", new AttributesImpl());
                    super.characters(value.toCharArray(), 0, value.length());
                    super.endElement(uri, "value", "sv:value");
                }
                super.endElement(uri, "property", "sv:property");
            }
        }
        else {
            super.endElement(uri, localName, name);
        }
    }
}
