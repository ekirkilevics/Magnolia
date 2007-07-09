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
package info.magnolia.module;

import info.magnolia.module.model.ModuleDefinition;
import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.io.IOUtils;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class BetwixtModuleDefinitionReader implements ModuleDefinitionReader {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BetwixtModuleDefinitionReader.class);

    private static final String DTD = "/info/magnolia/module/model/module.dtd";

    private final BeanReader beanReader;

    public BetwixtModuleDefinitionReader() {
        beanReader = new BeanReader();
        try {
            beanReader.setValidating(true);
            beanReader.setErrorHandler(new ErrorHandler());
            beanReader.registerBeanClass(ModuleDefinition.class);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public ModuleDefinition read(Reader in) throws ModuleManagementException {
        try {
            final Reader replacedDtd = replaceDtd(in);
            return (ModuleDefinition) beanReader.parse(replacedDtd);
        } catch (IOException e) {
            throw new ModuleManagementException("Can't read module definition file: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new ModuleManagementException(e.getMessage(), e);
        }

    }

    public ModuleDefinition readFromResource(String resourcePath) throws ModuleManagementException {
        final InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(resourcePath));
        return read(reader);
    }

    /**
     * @deprecated TODO very ugly hack to force documents to be validated against OUR DTD.
     */
    private Reader replaceDtd(Reader reader) throws IOException {
        URL dtdUrl = getClass().getResource(DTD);

        String content = IOUtils.toString(reader);

        // remove doctype
        Pattern pattern = Pattern.compile("<!DOCTYPE .*>");
        Matcher matcher = pattern.matcher(content);
        content = matcher.replaceFirst("");

        // set doctype to the dtd
        try {
            Document doc = new SAXBuilder().build(new StringReader(content));
            doc.setDocType(new DocType("module", dtdUrl.toString()));
            // write the xml to the string
            XMLOutputter outputter = new XMLOutputter();
            StringWriter writer = new StringWriter();
            outputter.output(doc, writer);
            final String replacedDtd = writer.toString();
            return new StringReader(replacedDtd);
        } catch (JDOMException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    private static class ErrorHandler implements org.xml.sax.ErrorHandler {
        public void warning(SAXParseException e) throws SAXException {
            log.warn("Warning on module definition " + getSaxParseExceptionMessage(e));
        }

        public void error(SAXParseException e) throws SAXException {
            throw new SAXException("Invalid module definition file, error " + getSaxParseExceptionMessage(e), e);
        }

        public void fatalError(SAXParseException e) throws SAXException {
            throw new SAXException("Invalid module definition file, fatal error " + getSaxParseExceptionMessage(e), e);
        }
    }

    private static String getSaxParseExceptionMessage(SAXParseException e) {
        return "at line " + e.getLineNumber() + " column " + e.getColumnNumber() + ": " + e.getMessage();
    }
}
