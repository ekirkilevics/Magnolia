/**
 * This file Copyright (c) 2007-2011 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Path;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 * Contains utility methods to check workspace.xml.
 *
 * @version $Id$
 */
public class WorkspaceXmlUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WorkspaceXmlUtil.class);

    /**
     * @return the names of all workspace descriptor files containing the indexer attribute.
     *
     * @deprecated since 4.5 - directly use {@link #getWorkspaceNamesMatching(String, String)} instead.
     */
    public static List<String> getWorkspaceNamesWithIndexer() {
        return getWorkspaceNamesMatching("/Workspace/SearchIndex/param[@name='textFilterClasses']/@value", ".*\\.jackrabbit\\.extractor\\..*");
    }

    /**
     * @return the names of all workspace descriptor files containing the provided XPath expression.
     */
    public static List<String> getWorkspaceNamesMatching(String xPathExpression) {
        return getWorkspaceNamesMatching(xPathExpression, ".*");
    }

    /**
     * @return the names of all workspace descriptor files containing the provided XPath expression that match the expectation.
     */
    public static List<String> getWorkspaceNamesMatching(String xPathExpression, String expectation) {
        final List<String> names = new ArrayList<String>();
        final File sourceDir = new File(Path.getAppRootDir() + "/repositories/magnolia/workspaces/");
        File[] files = sourceDir.listFiles();
        if (files == null) {
            // new repo
            return names;
        }
        final SAXBuilder builder = new SAXBuilder();
        for (File f : files) {
            if (!f.isDirectory()) {
                continue;
            }
            final File wks = new File(f, "workspace.xml");
            if (!wks.exists() || !wks.canRead()) {
                continue;
            }
            log.debug("Checking {} for old indexer.", f.getName());
            try {
                // check for the indexer def in wks
                final List<Attribute> list = getElementsFromXPath(builder.build(wks), xPathExpression);
                if (list.size() > 0 && list.get(0).getValue().matches(expectation)) {
                    names.add(wks.getAbsolutePath());
                }
            } catch (JDOMException e) {
                throw new RuntimeException(e); // TODO
            } catch (IOException e) {
                throw new RuntimeException(e); // TODO
            }
        }
        return names;
    }

    @SuppressWarnings("unchecked")
    private static List<Attribute> getElementsFromXPath(Document doc, String xpathExpr) throws JDOMException {
        final XPath xpath = XPath.newInstance(xpathExpr);
        // must add the namespace and use it: there is no default namespace elsewise
        xpath.addNamespace("ws", "file://workspace.xml");

        return xpath.selectNodes(doc);
    }
}
