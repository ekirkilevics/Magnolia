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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Path;
import info.magnolia.cms.module.ServletDefinition;
import info.magnolia.cms.module.ServletParameterDefinition;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Contains utility methods to register or check for the existence of elements in web.xml
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WebXmlUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebXmlUtil.class);

    /**
     * Register a servlet in the web.xml including init parameters. The code checks if the servlet already exists
     * @deprecated since 3.1, servlets are wrapped and executed through ServletDispatchingFilter
     * @see info.magnolia.cms.filters.ServletDispatchingFilter
     */
    public static boolean registerServlet(String name, String className, String[] urlPatterns, String comment, Hashtable initParams) throws JDOMException, IOException {
        boolean changed = false;

        // get the web.xml
        File source = getWebappDescriptorFile();

        Document doc = loadWebappDescriptor(source);

        // check if there already registered
        XPath xpath = XPath.newInstance("/webxml:web-app/webxml:servlet[webxml:servlet-name='" + name + "']");
        // must add the namespace and use it: there is no default namespace elsewise
        xpath.addNamespace("webxml", doc.getRootElement().getNamespace().getURI());
        final boolean isServletRegistered = (xpath.selectSingleNode(doc) != null);
        if (!isServletRegistered) {
            log.info("register servlet " + name);

            // make a nice comment
            doc.getRootElement().addContent(new Comment(comment));

            // the same name space must be used
            Namespace ns = doc.getRootElement().getNamespace();

            Element node = createServletElement(ns, name, className, initParams);

            doc.getRootElement().addContent(node);
            changed = true;
        } else {
            log.info("servlet {} already registered", name);
        }
        for (int i = 0; i < urlPatterns.length; i++) {
            String urlPattern = urlPatterns[i];
            changed = changed | registerServletMapping(doc, name, urlPattern, comment);
        }

        if (changed) {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(doc, new FileWriter(source));
        }
        return changed;
    }

    private static Element createServletElement(Namespace ns, String name, String className, Hashtable initParams) {
        Element node = new Element("servlet", ns);
        node.addContent(new Element("servlet-name", ns).addContent(name));
        node.addContent(new Element("servlet-class", ns).addContent(className));

        if (initParams != null && !(initParams.isEmpty())) {
            Enumeration params = initParams.keys();
            while (params.hasMoreElements()) {
                String paramName = params.nextElement().toString();
                String paramValue = (String) initParams.get(paramName);
                Element initParam = new Element("init-param", ns);
                initParam.addContent(new Element("param-name", ns).addContent(paramName));
                initParam.addContent(new Element("param-value", ns).addContent(paramValue));
                node.addContent(initParam);
            }
        }
        return node;
    }

    public static boolean registerServletMapping(Document doc, String name, String urlPattern, String comment) throws JDOMException {
        XPath xpath = XPath.newInstance("/webxml:web-app/webxml:servlet-mapping[webxml:servlet-name='"
                + name
                + "' and webxml:url-pattern='"
                + urlPattern
                + "']");

        // must add the namespace and use it: there is no default namespace elsewise
        xpath.addNamespace("webxml", doc.getRootElement().getNamespace().getURI());
        Element node = (Element) xpath.selectSingleNode(doc);

        if (node == null) {
            log.info("register servlet mapping [{}] for servlet [{}]", urlPattern, name);

            // make a nice comment
            doc.getRootElement().addContent(new Comment(comment));

            // the same name space must be used
            Namespace ns = doc.getRootElement().getNamespace();

            // create the mapping
            node = new Element("servlet-mapping", ns);
            node.addContent(new Element("servlet-name", ns).addContent(name));
            node.addContent(new Element("url-pattern", ns).addContent(urlPattern));
            doc.getRootElement().addContent(node);
            return true;

        }

        log.info("servlet mapping [{}] for servlet [{}] already registered", urlPattern, name);
        return false;
    }

    private static Document loadWebappDescriptor(File source) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(source);
        return doc;
    }

    protected static File getWebappDescriptorFile() throws FileNotFoundException {
        File source = new File(Path.getAppRootDir() + "/WEB-INF/web.xml");
        if (!source.exists()) {
            throw new FileNotFoundException("Failed to locate web.xml : " + source.getAbsolutePath());
        }
        return source;
    }
}
