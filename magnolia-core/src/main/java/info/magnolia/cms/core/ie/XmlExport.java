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
package info.magnolia.cms.core.ie;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.Format.TextMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This utility class provides static methods for turning a content [node] into an XML document.
 * @author Mettraux John (john.mettraux &gt;at&lt; openwfe.org)
 * @version 0.1 $Id :$
 *
 * @deprecated deprecated since 3.6 but wasn't used before - MAGNOLIA-405
 */
public class XmlExport implements ExportHandler {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(XmlExport.class);

    /**
     * XML structure constants
     */
    private static final String E_CONTENT = "content"; //$NON-NLS-1$

    private static final String E_PROPERTY = "property"; //$NON-NLS-1$

    private static final String A_NAME = "name"; //$NON-NLS-1$

    private static final String A_TYPE = "type"; //$NON-NLS-1$

    /**
     * default properties
     */
    public static final String DEFAULT_ENCODING = "UTF-8"; //$NON-NLS-1$

    /**
     * basic parameters
     */
    public static final String ENCODING = "encoding"; //$NON-NLS-1$

    /**
     * fields
     */
    private boolean binaryAsLink = true;

    private Map params = new Hashtable();

    //
    // CONSTRUCTORS

    /**
     * Builds an XmlExporter, if the parameter 'embedBinaryContent' is set to true, binary content will be integrated in
     * the generated XML documents as base64.
     */
    public XmlExport() {
    }

    //
    // PUBLIC METHODS

    public void setBinaryAsLink(boolean binaryAsLink) {
        this.binaryAsLink = binaryAsLink;
    }

    public boolean getBinaryAsLink() {
        return this.binaryAsLink;
    }

    /**
     * Turns the given Content instance into a JDOM XML document.
     */
    public Object exportContent(final Content content) {
        return new Document(domExport(content));
    }

    public void exportContent(final Content content, OutputStream outStream) throws RepositoryException, IOException {
        // check if the encoding is set by config parameters
        String encoding = ((String) this.getParameter(ENCODING));
        if (StringUtils.isEmpty(encoding)) {
            encoding = DEFAULT_ENCODING;
        }
        this.getXMLOutputter(encoding).output(((Document) this.exportContent(content)), outStream);
    }

    public void setParameter(String key, Object value) {
        this.params.put(key, value);
    }

    public Object getParameter(String key) {
        return this.params.get(key);
    }

    private Element domExport(final Content content) {

        Element elt = new Element(E_CONTENT);
        elt.setAttribute(A_NAME, content.getName());
        try {
            elt.setAttribute(A_TYPE, content.getNodeTypeName());
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }

        // collect all nodes
        export(elt, content.getChildren(ItemType.NT_BASE));

        // node data
        exportNodeData(elt, content.getNodeDataCollection());

        return elt;
    }

    private void exportNodeData(Element elt, Collection nodeData) {

        Iterator it = nodeData.iterator();
        while (it.hasNext()) {
            NodeData nd = (NodeData) it.next();

            export(elt, nd.getJCRProperty());
        }
    }

    private void export(Element elt, Collection contentChildren) {

        Iterator it = contentChildren.iterator();
        while (it.hasNext()) {
            Content c = (Content) it.next();
            elt.addContent(domExport(c));
        }
    }

    private void export(Element elt, Property property) {

        Element pElt = new Element(E_PROPERTY);

        try {

            pElt.setAttribute(A_NAME, property.getName());
            pElt.setAttribute(A_TYPE, PropertyType.nameFromValue(property.getType()));
            exportValue(pElt, property);

        }
        catch (final Throwable t) {
            log.warn("export() skipped a property because of " + t); //$NON-NLS-1$
        }

        elt.addContent(pElt);
    }

    private void exportValue(final org.jdom.Element pElt, final Property property) {

        String sContent;

        try {
            if (property.getType() == PropertyType.BINARY) {

                if (this.binaryAsLink) {
                    sContent = property.getPath();
                }
                else {
                    StringBuffer stringBuffer = new StringBuffer();
                    try {
                        InputStream is = property.getStream();
                        byte[] buffer = new byte[8192];

                        while ((is.read(buffer)) > 0) {
                            stringBuffer.append(new String(buffer));
                        }
                        IOUtils.closeQuietly(is);
                    }
                    catch (Exception e) {
                        log.error("Failed to read input stream", e);
                    }

                    sContent = new String(Base64.encodeBase64(stringBuffer.toString().getBytes()));
                }
            }
            else if (property.getType() == PropertyType.DATE) {
                sContent = property.getDate().getTime().toString();
            }
            else {
                sContent = property.getString();
            }
        }
        catch (final Throwable t) {

            log.warn("exportValue() failure", t); //$NON-NLS-1$

            sContent = "exportValue() failure " + t.toString(); //$NON-NLS-1$
        }
        pElt.addContent(new org.jdom.Text(sContent));
    }

    /**
     * A convenience method for setting up a pretty print XMLOutputter with a given encoding.
     */
    private org.jdom.output.XMLOutputter getXMLOutputter(String encoding) {
        Format format = Format.getPrettyFormat();
        format.setEncoding(encoding);
        format.setTextMode(TextMode.PRESERVE);
        return new org.jdom.output.XMLOutputter(format);
    }

}
