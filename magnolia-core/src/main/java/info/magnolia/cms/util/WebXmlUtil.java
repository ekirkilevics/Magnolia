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
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;


/**
 * Contains utility methods to register or check for the existence of elements in web.xml.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WebXmlUtil {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebXmlUtil.class);

    /**
     * @deprecated
     */
    private File source;

    private final Document doc;

    public WebXmlUtil() {
        final File source = new File(Path.getAppRootDir() + "/WEB-INF/web.xml");
        if (!source.exists()) {
            throw new IllegalStateException("Failed to locate web.xml : " + source.getAbsolutePath());
        }
        final SAXBuilder builder = new SAXBuilder();
        try {
            doc = builder.build(source);
            this.source = source;
        } catch (JDOMException e) {
            throw new RuntimeException(e); // TODO
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    /**
     * Test-friendly constructor.
     */
    WebXmlUtil(InputStream inputStream) {
        final SAXBuilder builder = new SAXBuilder();
        try {
            doc = builder.build(inputStream);
        } catch (JDOMException e) {
            throw new RuntimeException(e); // TODO
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    /**
     * Register a servlet in the web.xml including init parameters. The code checks if the servlet already exists
     * @deprecated since 3.1, servlets are wrapped and executed through ServletDispatchingFilter
     * @see info.magnolia.cms.filters.ServletDispatchingFilter
     */
    public boolean registerServlet(String name, String className, String[] urlPatterns, String comment, Map initParams) throws JDOMException, IOException {
        boolean changed = false;
        if (!isServletRegistered(name)) {
            log.info("register servlet " + name);

            // make a nice comment
            doc.getRootElement().addContent(new Comment(comment));

            Element node = createServletElement(name, className, initParams);

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
            save();
        }
        return changed;
    }

    /**
     * @deprecated since 3.1, servlets are wrapped and executed through ServletDispatchingFilter
     * @see info.magnolia.cms.filters.ServletDispatchingFilter
     */
    public boolean registerServletMapping(Document doc, String name, String urlPattern, String comment) throws JDOMException {
        if (isServletMappingRegistered(name, urlPattern)) {
            log.info("register servlet mapping [{}] for servlet [{}]", urlPattern, name);

            // make a nice comment
            doc.getRootElement().addContent(new Comment(comment));

            // the same name space must be used
            Element node = createMappingElement(doc, name, urlPattern);
            doc.getRootElement().addContent(node);
            return true;

        }

        log.info("servlet mapping [{}] for servlet [{}] already registered", urlPattern, name);
        return false;
    }

    public boolean isServletOrMappingRegistered(String servletName) {
        return isServletRegistered(servletName) || isServletMappingRegistered(servletName);
    }

    public boolean isServletRegistered(String servletName) {
        return xpathMatches("/webxml:web-app/webxml:servlet[webxml:servlet-name='" + servletName + "']");
    }

    public boolean isServletMappingRegistered(String servletName) {
        return xpathMatches("/webxml:web-app/webxml:servlet-mapping[webxml:servlet-name='" + servletName + "']");
    }

    public boolean isServletMappingRegistered(String servletName, String urlPattern) {
        final String xpathExpr = "/webxml:web-app/webxml:servlet-mapping[webxml:servlet-name='"
                + servletName + "' and webxml:url-pattern='" + urlPattern + "']";
        return xpathMatches(xpathExpr);
    }

    public boolean isFilterRegistered(String filterClass) {
        return getFilterElement(filterClass) != null;
    }

    public boolean areFilterDispatchersConfiguredProperly(String filterClass, List mandatoryDispatchers, List optionalDispatchers) {
        final Element filterEl = getFilterElement(filterClass);
        if (filterEl != null) {
            final String filterName = filterEl.getTextNormalize();
            final String filterMappingXPathExpr = "/webxml:web-app/webxml:filter-mapping[webxml:filter-name='" + filterName + "']/webxml:dispatcher";
            final List dispatchersEl = getElementsFromXPath(filterMappingXPathExpr);
            final List dispatchers = new ArrayList();
            final Iterator it = dispatchersEl.iterator();
            while (it.hasNext()) {
                final Element dispatcherEl = (Element) it.next();
                dispatchers.add(dispatcherEl.getTextNormalize());
            }
            dispatchers.removeAll(optionalDispatchers);
            return CollectionUtils.isEqualCollection(dispatchers, mandatoryDispatchers);

        }
        return true;
    }

    public boolean isListenerRegistered(String deprecatedListenerClass) {
	    final String xpathExpr = "/webxml:web-app/webxml:listener[webxml:listener-class='"
	        + deprecatedListenerClass + "']";
	    return xpathMatches(xpathExpr);
	}

	public static void main(String[] args) {
        final ArrayList list = new ArrayList();
        list.add("foo");
        list.add("bar");
        list.add("baz");

        final LinkedList l2 = new LinkedList();
        l2.add("bar");
        l2.add("foo");
        l2.add("baz");
        System.out.println("CollectionUtils.isEqualCollection(list,l2) = " + CollectionUtils.isEqualCollection(list, l2));


        System.out.println("l2.equals(list) = " + l2.equals(list));
    }

    private Element getFilterElement(String filterClass) {
        final String filterXPathExpr = "/webxml:web-app/webxml:filter[webxml:filter-class='" + filterClass + "']/webxml:filter-name";

        return getElementFromXPath(filterXPathExpr);
    }

    private List getElementsFromXPath(String xpathExpr) {
        try {
            final XPath xpath = XPath.newInstance(xpathExpr);
            // must add the namespace and use it: there is no default namespace elsewise
            xpath.addNamespace("webxml", doc.getRootElement().getNamespace().getURI());
            return xpath.selectNodes(doc);
        } catch (JDOMException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    private Element getElementFromXPath(String xpathExpr) {
        final List list = getElementsFromXPath(xpathExpr);
        return (Element) (list.size() > 0 ? list.get(0) : null);
    }

    private boolean xpathMatches(String xpathExpr) {
        return getElementsFromXPath(xpathExpr).size() > 0;
    }


    /**
     * @deprecated
     */
    private Element createServletElement(String name, String className, Map initParams) {
        Namespace ns = doc.getRootElement().getNamespace();
        Element node = new Element("servlet", ns);
        node.addContent(new Element("servlet-name", ns).addContent(name));
        node.addContent(new Element("servlet-class", ns).addContent(className));

        if (initParams != null && !(initParams.isEmpty())) {
            Iterator params = initParams.keySet().iterator();
            while (params.hasNext()) {
                String paramName = (String) params.next();
                String paramValue = (String) initParams.get(paramName);
                Element initParam = new Element("init-param", ns);
                initParam.addContent(new Element("param-name", ns).addContent(paramName));
                initParam.addContent(new Element("param-value", ns).addContent(paramValue));
                node.addContent(initParam);
            }
        }
        return node;
    }

    /**
     * @deprecated
     */
    private Element createMappingElement(Document doc, String name, String urlPattern) {
        Namespace ns = doc.getRootElement().getNamespace();

        Element node = new Element("servlet-mapping", ns);
        node.addContent(new Element("servlet-name", ns).addContent(name));
        node.addContent(new Element("url-pattern", ns).addContent(urlPattern));
        return node;
    }

    /**
     * @deprecated
     */
    private void save() throws IOException {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(doc, new FileWriter(source));
    }

}
