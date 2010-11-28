/**
 * This file Copyright (c) 2007-2010 Magnolia International
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
package info.magnolia.module.delta;

import info.magnolia.cms.util.ClasspathResourcesUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;


/**
 * Util class to extract nodes, properties from bootstrap files.
 * @author tmiyar
 *
 */
public class BootstrapFileUtil {

    private static Document getDocument(String fileName) {

        final SAXBuilder builder = new SAXBuilder();
        try {
            InputStream is = ClasspathResourcesUtil.getStream(fileName);
            return builder.build(is);
        } catch (JDOMException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List getElementsFromXPath(String fileName, String xpathExpr) {
        try {
            Document doc = getDocument(fileName);

            final XPath xpath = XPath.newInstance(xpathExpr);
            // must add the namespace and use it: there is no default namespace otherwise
            List namespaces = new ArrayList();
            namespaces.add(doc.getRootElement().getNamespace());
            List additionalNs = doc.getRootElement().getAdditionalNamespaces();
            for (int i=0; i < additionalNs.size(); i++) {
                Namespace namespace = (Namespace) additionalNs.get(i);
                xpath.addNamespace(namespace);
            }

            return xpath.selectNodes(doc);
        } catch (JDOMException e) {
            throw new RuntimeException(e);
        }
    }

    private static Element getElementFromXPath(String fileName, String xpathExpr) {
        final List list = getElementsFromXPath(fileName, xpathExpr);
        return (Element) (list.size() > 0 ? list.get(0) : null);
    }

    public static InputStream getElementAsStream(String fileName, String nodePath) {

        String xpathExpr = BootstrapFileUtil.getXPathNodeQueryString(nodePath);

        Element ele = getElementFromXPath(fileName, xpathExpr);
        ele.detach();
        Document doc = new Document(ele);

        Format format = Format.getRawFormat();
        format.setExpandEmptyElements(true);


        XMLOutputter outputter = new XMLOutputter(format);

        return new ByteArrayInputStream(outputter.outputString(doc).getBytes());

    }

    public static String getPropertyValue(String fileName, String xpathExpr) {

        String xpath = getXpathPropertyQueryString(xpathExpr);

        Element ele = getElementFromXPath(fileName, xpath);

        return ele.getChildText("value", ele.getNamespace("sv"));
    }

    public static String getXPathNodeQueryString(String path) {

        String xpathExpr = "";

        String[] slices = path.split("/");
        for(int i=0; i < slices.length; i++) {
            if(slices[i] != null && slices[i].length() > 0) {
                xpathExpr += "/sv:node[@sv:name='" + slices[i] + "']";
            }
        }
        return xpathExpr;

    }

    protected static String getXpathPropertyQueryString(String path) {
        String xpathExpr = "";

        String[] slices = path.split("/");
        for(int i=0; i < slices.length - 1; i++) {
            if(slices[i] != null && slices[i].length()  > 0) {
                xpathExpr += "/sv:node[@sv:name='" + slices[i] + "']";
            }
        }
        xpathExpr += "/sv:property[@sv:name='" + slices[slices.length -1] + "']";

        return xpathExpr;
    }
}
