/**
 * This file Copyright (c) 2007-2012 Magnolia International
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

import info.magnolia.cms.core.SystemProperty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains utility methods to check workspace.xml.
 *
 * @version $Id$
 */
public class WorkspaceXmlUtil {

    private final static Logger log = LoggerFactory.getLogger(WorkspaceXmlUtil.class);

    /**
     * @deprecated since 4.5 - directly use {@link #getWorkspaceNamesMatching(String, String, boolean)} instead.
     */
    public static List<String> getWorkspaceNamesWithIndexer() {
        return getWorkspaceNames("/Workspace/SearchIndex/param[@name='textFilterClasses']/@value",
                ".*\\.jackrabbit\\.extractor\\..*");
    }

    public static List<String> getWorkspaceNamesMatching(String xPathExpression) {
        return getWorkspaceNames(xPathExpression, ".*");
    }

    /**
     * Create and return list of workspaces names. If expectation is not null, all workspace with configs containing
     * entries identified by the xPathExpression will be contained if they match the expectation. If expectation is
     * null, all workspace names with configs that don't contain anything matching the xPathExpression will be returned.
     *
     * @param xPathExpression
     *            the xpath expression
     * @param expectation
     *            value the matches of the xPathExpression should be compared with - special meaning of 'null': in that
     *            case xPathExpression should not be contained!
     *
     * @return the list of workspace names
     */
    public static List<String> getWorkspaceNames(String xPathExpression, String expectation) {
        final List<String> names = new ArrayList<String>();
        final String dir = SystemProperty.getProperty(SystemProperty.MAGNOLIA_REPOSITORIES_HOME) + "/magnolia/workspaces/";
        log.debug("Checking directory " + dir);
        final File sourceDir = new File(dir);
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
            try {
                log.debug("Analysing file " + wks.getAbsolutePath());
                // check for the xPathExpression in wks
                final List<Attribute> list = getElementsFromXPath(builder.build(wks), xPathExpression);
                if (expectation == null) {
                    if (list.size() == 0) {
                        names.add(wks.getAbsolutePath());
                    }
                } else {
                    if (list.size() > 0 && list.get(0).getValue().matches(expectation)) {
                        names.add(wks.getAbsolutePath());
                    }
                }
            } catch (JDOMException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
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
