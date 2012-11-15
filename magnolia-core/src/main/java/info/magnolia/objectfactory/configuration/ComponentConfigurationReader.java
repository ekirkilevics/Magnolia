/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.objectfactory.configuration;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.io.IOUtils;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import info.magnolia.module.model.ComponentsDefinition;

/**
 * Reads components defined in xml.
 *
 * @version $Id$
 * @see ComponentsDefinition
 */
public class ComponentConfigurationReader {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ComponentConfigurationReader.class);

    private static final String DTD = "/info/magnolia/module/model/components.dtd";

    private final String dtdUrl;
    private final BeanReader beanReader;

    public ComponentConfigurationReader() {
        final URL dtd = getClass().getResource(DTD);
        if (dtd == null) {
            throw new ComponentConfigurationException("DTD not found at " + DTD);
        }
        dtdUrl = dtd.toString();

        beanReader = new BeanReader();
        try {
            beanReader.setValidating(true);
            beanReader.setErrorHandler(new ErrorHandler());
            beanReader.registerBeanClass(ComponentsDefinition.class);
        } catch (IntrospectionException e) {
            throw new ComponentConfigurationException(e);
        }
    }

    public List<ComponentsDefinition> readAll(List<String> resourcePaths) {
        List<ComponentsDefinition> componentsDefinitions = new ArrayList<ComponentsDefinition>();
        for (String resourcePath : resourcePaths) {
            log.debug("Parsing components file {}", resourcePath);
            ComponentsDefinition componentsDefinition = readFromResource(resourcePath);
            componentsDefinitions.add(componentsDefinition);
        }
        return componentsDefinitions;
    }

    public ComponentsDefinition read(Reader in) {
        try {
            final Reader replacedDtd = replaceDtd(in);
            return (ComponentsDefinition) beanReader.parse(replacedDtd);
        } catch (IOException e) {
            throw new ComponentConfigurationException("Can't read components definition file: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new ComponentConfigurationException(e.getMessage(), e);
        }
    }

    public ComponentsDefinition readFromResource(String resourcePath) {
        final InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(resourcePath));
        try {
            return read(reader);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Can't close input for " + resourcePath);
            }
        }
    }

    /**
     * @deprecated TODO very ugly hack to force documents to be validated against OUR DTD.
     *             We could use an EntityResolver, but it seems that with SAX, the documents will only be
     *             validated if they original have a doctype declaration.
     *             We could also use http://doctypechanger.sourceforge.net/
     *             ... or switch to XmlSchema.
     */
    private Reader replaceDtd(Reader reader) throws IOException {
        String content = IOUtils.toString(reader);

        // remove doctype
        Pattern pattern = Pattern.compile("<!DOCTYPE .*>");
        Matcher matcher = pattern.matcher(content);
        content = matcher.replaceFirst("");

        // set doctype to the dtd
        try {
            Document doc = new SAXBuilder().build(new StringReader(content));
            doc.setDocType(new DocType("components", dtdUrl));
            // write the xml to the string
            XMLOutputter outputter = new XMLOutputter();
            StringWriter writer = new StringWriter();
            outputter.output(doc, writer);
            final String replacedDtd = writer.toString();
            return new StringReader(replacedDtd);
        } catch (JDOMException e) {
            throw new ComponentConfigurationException(e);
        }
    }

    private static class ErrorHandler implements org.xml.sax.ErrorHandler {

        @Override
        public void warning(SAXParseException e) throws SAXException {
            log.warn("Warning on components definition " + getSaxParseExceptionMessage(e));
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw new SAXException("Invalid components definition file, error " + getSaxParseExceptionMessage(e), e);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw new SAXException("Invalid components definition file, fatal error " + getSaxParseExceptionMessage(e), e);
        }
    }

    private static String getSaxParseExceptionMessage(SAXParseException e) {
        return "at line " + e.getLineNumber() + " column " + e.getColumnNumber() + ": " + e.getMessage();
    }
}
