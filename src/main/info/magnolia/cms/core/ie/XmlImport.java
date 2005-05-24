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
package info.magnolia.cms.core.ie;


import javax.jcr.RepositoryException;
import javax.jcr.PropertyType;
import javax.jcr.PathNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.*;

import org.apache.log4j.Logger;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;

/**
 * Date: May 24, 2005
 * Time: 4:59:37 PM
 *
 * @author Sameer Charles
 * $Id :$
 */
public class XmlImport implements ImportHandler {

    /* *
     * Logger.
     */
    private static Logger log = Logger.getLogger(XmlExport.class);

    /**
     * XML structure constants
     * */
    private final static String E_CONTENT = "content";

    private final static String E_PROPERTY = "property";

    private final static String A_NAME = "name";

    private final static String A_TYPE = "type";

    /**
     * fields
     * */
    private boolean binaryAsLink = true;

    private Map params = new Hashtable();

    public void setBinaryAsLink(boolean binaryAsLink) {
        this.binaryAsLink = binaryAsLink;
    }

    public boolean getBinaryAsLink() {
        return this.binaryAsLink;
    }

    public void importContent(Content target, InputStream inStream) throws RepositoryException, IOException {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(inStream);
            this.importContent(target, document.getRootElement());
        }
        catch (Exception e) {
            log.error("failed to import");
            log.error(e.getMessage(), e);
        }
        finally {
            if (inStream != null) {
                try {
                    inStream.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public void setParameter(String key, Object value) {
        this.params.put(key, value);
    }

    public Object getParameter(String key) {
        return this.params.get(key);
    }

    private void importContent(Content content, Element element) {
        if (element.getName().equalsIgnoreCase(E_CONTENT)) {
            content = this.addContent(content, element);
        } else if (element.getName().equalsIgnoreCase(E_PROPERTY)) {
            this.addProperty(content, element);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Undefined type - "+element.getName());
            }
        }
        // if not allowed or some repository exception occured
        if (content == null)
            return;
        Iterator children = element.getChildren().iterator();
        while (children.hasNext()) {
            Element subElement = (Element) children.next();
            importContent(content, subElement);
        }
    }

    private Content addContent(Content content, Element element) {
        try {
            Content newContent = content.getContent(element.getAttributeValue(A_NAME));
            return newContent;
        } catch (PathNotFoundException e) {
            try {
                Content newContent
                        = content.createContent(element.getAttributeValue(A_NAME), element.getAttributeValue(A_TYPE));
                if (log.isDebugEnabled()) {
                    log.debug("Adding content - "+newContent.getHandle());
                }
                content.save();
                return newContent;
            } catch (AccessDeniedException ade) {
                log.error(ade.getMessage());
            } catch (RepositoryException re) {
                log.error(re.getMessage(), re);
            }
        } catch (AccessDeniedException ade) {
            log.error(ade.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private void addProperty(Content content, Element element) {
        NodeData nodeData = content.getNodeData(element.getAttributeValue(A_NAME));
        if (!nodeData.isExist()) {
            try {
                nodeData = content.createNodeData(element.getAttributeValue(A_NAME));
                if (log.isDebugEnabled()) {
                    log.debug("Adding property - "+nodeData.getHandle());
                }
            } catch (AccessDeniedException ade) {
                log.error(ade.getMessage());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        // set value and type
        String value = element.getText();
        int type = (new Integer(element.getAttributeValue(A_TYPE))).intValue();
        try {
            this.setPropertyValue(nodeData, type, value);
        } catch (AccessDeniedException ade) {
            log.error(ade.getMessage());
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
    }

    private void setPropertyValue(NodeData nodeData, int type, String value) throws AccessDeniedException,
            RepositoryException {
        switch (type) {
            case PropertyType.STRING:
                nodeData.setValue(value);
                break;
            case PropertyType.LONG:
                nodeData.setValue((new Long(value)).longValue());
                break;
            case PropertyType.DOUBLE:
                nodeData.setValue((new Double(value)).doubleValue());
                break;
            case PropertyType.DATE:
                // todo
                //Calendar cal = new GregorianCalendar();
                break;
            case PropertyType.BOOLEAN:
                nodeData.setValue((new Boolean(value)).booleanValue());
                break;
            case PropertyType.BINARY:
                nodeData.setValue(new ByteArrayInputStream(value.getBytes()));
        }
    }

}
