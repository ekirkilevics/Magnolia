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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;


/**
 * Date: May 24, 2005 Time: 4:59:37 PM
 * @author Sameer Charles $Id :$
 */
public class XmlImport implements ImportHandler {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(XmlExport.class);

    /**
     * XML structure constants
     */
    private static final String E_CONTENT = "content"; //$NON-NLS-1$

    private static final String E_PROPERTY = "property"; //$NON-NLS-1$

    private static final String A_NAME = "name"; //$NON-NLS-1$

    private static final String A_TYPE = "type"; //$NON-NLS-1$

    /**
     * params
     */
    public static final String DATE_FORMAT = "dateFormat"; //$NON-NLS-1$

    /**
     * default
     */
    public static final String DEFAULT_DATE_FORMAT = "EEE MMM dd hh:mm:ss zzzz yyyy"; //$NON-NLS-1$

    /**
     * fields
     */
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
            target.save();
        }
        catch (Exception e) {
            log.error("failed to import"); //$NON-NLS-1$
            log.error(e.getMessage(), e);
        }
        finally {
            IOUtils.closeQuietly(inStream);
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
        }
        else if (element.getName().equalsIgnoreCase(E_PROPERTY)) {
            this.addProperty(content, element);
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("Undefined type - " + element.getName()); //$NON-NLS-1$
            }
        }
        // if not allowed or some repository exception occured
        if (content == null) {
            return;
        }
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
        }
        catch (PathNotFoundException e) {
            try {
                Content newContent = content.createContent(element.getAttributeValue(A_NAME), element
                    .getAttributeValue(A_TYPE));
                if (log.isDebugEnabled()) {
                    log.debug("Adding content - " + newContent.getHandle()); //$NON-NLS-1$
                }
                return newContent;
            }
            catch (AccessDeniedException ade) {
                log.error(ade.getMessage());
            }
            catch (RepositoryException re) {
                log.error(re.getMessage(), re);
            }
        }
        catch (AccessDeniedException ade) {
            log.error(ade.getMessage());
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * set property value or create a new property if not exist. PropertyType.REFERENCE are handled differently, since
     * there is no difference in storage of String and Reference type it must be set while creating a property.
     */
    private void addProperty(Content content, Element element) {
        NodeData nodeData = content.getNodeData(element.getAttributeValue(A_NAME));
        int type = PropertyType.valueFromName(element.getAttributeValue(A_TYPE));
        String value = element.getText();
        if (!nodeData.isExist()) {
            try {
                if (type == PropertyType.REFERENCE) {
                    Value refValue = content.getJCRNode().getSession().getValueFactory().createValue(value);
                    nodeData = content.createNodeData(element.getAttributeValue(A_NAME), refValue, type);
                }
                nodeData = content.createNodeData(element.getAttributeValue(A_NAME));
                if (log.isDebugEnabled()) {
                    log.debug("Adding property - " + nodeData.getHandle()); //$NON-NLS-1$
                }
            }
            catch (AccessDeniedException ade) {
                log.error(ade.getMessage());
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        // set value and type
        try {
            this.setPropertyValue(nodeData, type, value);
        }
        catch (AccessDeniedException ade) {
            log.error(ade.getMessage());
        }
        catch (RepositoryException re) {
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
                String dateFormat = (String) this.getParameter(DATE_FORMAT);
                if (StringUtils.isEmpty(dateFormat)) {
                    dateFormat = DEFAULT_DATE_FORMAT;
                }
                SimpleDateFormat simpleFormat = new SimpleDateFormat(dateFormat);
                try {
                    Date date = simpleFormat.parse(value);
                    Calendar cal = new GregorianCalendar();
                    cal.setTime(date);
                    nodeData.setValue(cal);
                }
                catch (ParseException e) {
                    log.error("Failed to parse date with the given format " + dateFormat, e); //$NON-NLS-1$
                }
                break;
            case PropertyType.BOOLEAN:
                nodeData.setValue(BooleanUtils.toBoolean(value));
                break;
            case PropertyType.BINARY:
                nodeData.setValue(new ByteArrayInputStream(Base64.decodeBase64(value.getBytes())));
                break;
            case PropertyType.REFERENCE:
                /**
                 * this property must exist before of the same type as REFERENCE
                 */
                nodeData.setValue(value);
        }
    }

}
