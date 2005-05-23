/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core.export;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.PropertyIterator;

import org.apache.log4j.Logger;
import org.apache.commons.codec.binary.Base64;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentHandler;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.NodeData;


/**
 * This utility class provides static methods for turning a content [node]
 * into an XML document.
 *
 * @author Mettraux John (john.mettraux &gt;at&lt; openwfe.org)
 * @version 0.1
 * $Revision$ $Author$
 */
public class ContentExporter {

    /* *
     * Logger.
     */
    private static Logger log = Logger.getLogger(ContentExporter.class);

    //
    // CONSTANTS

    private final static String E_CONTENT = "content";
    private final static String E_METADATA = "metadata";
    private final static String E_PROPERTY = "property";

    private final static String A_NAME = "name";
    private final static String E_VALUE = "value";

    private final static String E_NODEDATA = "nodedata";

    private final static String A_TYPE = "type";
    private final static String A_TYPENAME = "tname";

    //
    // FIELDS

    private boolean embedBinaryContent = true;

    //
    // CONSTRUCTORS

    /**
     * Builds an XmlExporter, if the parameter 'embedBinaryContent' is set to
     * true, binary content will be integrated in the generated XML documents
     * as base64.
     */
    public ContentExporter (final boolean embedBinaryContent) {

        this.embedBinaryContent = embedBinaryContent;
    }

    /**
     * Builds an XmlExporter, if the parameter 'embedBinaryContent' is set to
     * true, binary content will be integrated in the generated XML documents
     * as base64.
     */
    public ContentExporter (final Boolean embedBinaryContent) {

        this.embedBinaryContent = embedBinaryContent.booleanValue();
    }

    //
    // PUBLIC METHODS

    /**
     * Turns the given Content instance into a JDOM XML document.
     */
    public org.jdom.Document export (final Content content) {
	    return new org.jdom.Document(exportContent(content));
    }

    /**
     * The same as export, but returns the XML document as a String.
     */
    public String exportToString (final Content content, final String encoding) {
        return toString(export(content), encoding);
    }

    //
    // METHODS

    private org.jdom.Element exportContent (final Content content) {

        final org.jdom.Element elt = new org.jdom.Element(E_CONTENT);

	//
	// Metadata

        export(elt, content.getMetaData());

	//
	// children of type content

	export
            (elt, 
             content.getChildren
                (ItemType.CONTENT, ContentHandler.SORT_BY_SEQUENCE));

	//
	// children of type content node

	export
            (elt, 
             content.getChildren
                (ItemType.CONTENTNODE, ContentHandler.SORT_BY_SEQUENCE));

        //
        // node data

        exportNodeData(elt, content.getNodeDataCollection());

        //
        // done.

        return elt;
    }

    private void exportNodeData (final org.jdom.Element elt, final java.util.Collection nodeData) {

        final java.util.Iterator it = nodeData.iterator();
        while (it.hasNext()) {
            final NodeData nd = (NodeData)it.next();

            export(elt, nd.getJCRProperty());
        }
    }

    private void export (final org.jdom.Element elt, final java.util.Collection contentChildren) {

        final java.util.Iterator it = contentChildren.iterator();

        while (it.hasNext()) {

            final Content c = (Content)it.next();

            elt.addContent(exportContent(c));
        }
    }

    private void export (final org.jdom.Element elt, final MetaData md) {

        final org.jdom.Element mdElt = new org.jdom.Element(E_METADATA);

        final PropertyIterator pit = md.getProperties();
        while (pit.hasNext()) {

            final Property p = pit.nextProperty();

            export(mdElt, p);
        }

        elt.addContent(mdElt);
    }

    private void export (final org.jdom.Element elt, final Property property) {

        final org.jdom.Element pElt = new org.jdom.Element(E_PROPERTY);

        try {

            pElt.setAttribute(A_NAME, property.getName());
            pElt.setAttribute(A_TYPE, ""+property.getType());
            pElt.setAttribute(A_TYPENAME, PropertyType.nameFromValue(property.getType()));
            exportValue(pElt, property);

        } catch (final Throwable t) {

            log.warn("export() skipped a property because of "+t);
            log.debug("export() skipped a property", t);
        }

        elt.addContent(pElt);
    }

    private void exportValue (final org.jdom.Element pElt, final Property property) {

        String sContent = null;

        try {

            if (property.getType() == PropertyType.BINARY) {

                if (this.embedBinaryContent) {

                    sContent = new String(Base64.encodeBase64(property.getString().getBytes()));
                } else {

                    sContent = property.getPath();
                }
            } else {

                sContent = property.getString();
            }
        } catch (final Throwable t) {

            log.warn("exportValue() failure", t);

            sContent = "exportValue() failure "+t.toString();
        }

        pElt.addContent(new org.jdom.Text(sContent));
    }

    //
    // STATIC METHODS

    /**
     * A convenience method for setting up a pretty print XMLOutputter
     * with a given encoding.
     */
    public static org.jdom.output.XMLOutputter getXMLOutputter (String encoding) {
        final org.jdom.output.Format format = 
            org.jdom.output.Format.getPrettyFormat();

        format.setEncoding(encoding);

        return new org.jdom.output.XMLOutputter(format);
    }

    /**
     * Turns a JDOM document into a String.
     */
    public static String toString (final org.jdom.Document doc, final String encoding) {

        final org.jdom.output.XMLOutputter out = getXMLOutputter(encoding);

        java.io.ByteArrayOutputStream baos = 
            new java.io.ByteArrayOutputStream();

        try {
            out.output(doc, baos);
            baos.write('\n');
            baos.flush();
        } catch (final java.io.IOException ie) {
            log.warn
                ("Failed to encode workitem as xml", ie);
        }

        return baos.toString();
    }

}
