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
