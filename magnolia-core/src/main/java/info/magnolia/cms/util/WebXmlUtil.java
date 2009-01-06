/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Path;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * Contains utility methods to register or check for the existence of elements in web.xml.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WebXmlUtil {
    private final Document doc;

    public WebXmlUtil() {
        final File source = new File(Path.getAppRootDir() + "/WEB-INF/web.xml");
        if (!source.exists()) {
            throw new IllegalStateException("Failed to locate web.xml : " + source.getAbsolutePath());
        }
        final SAXBuilder builder = new SAXBuilder();
        try {
            doc = builder.build(source);
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

    public Collection getServletMappings(String servletName) {
        final String servletMappingXPathExpr = "/webxml:web-app/webxml:servlet-mapping[webxml:servlet-name='" + servletName + "']/webxml:url-pattern";
        final List servletMappings = getElementsFromXPath(servletMappingXPathExpr);

        return CollectionUtils.collect(servletMappings, new Transformer() {
            public Object transform(Object input) {
                final Element servletMapping = (Element) input;
                return servletMapping.getText();
            }
        });
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
        final String xpathExpr = "/webxml:web-app/webxml:listener[webxml:listener-class='" + deprecatedListenerClass + "']";
        return xpathMatches(xpathExpr);
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
}
