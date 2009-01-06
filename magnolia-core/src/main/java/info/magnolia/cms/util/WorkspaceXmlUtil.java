/**
 * This file Copyright (c) 2007-2009 Magnolia International
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
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Contains utility methods to check workspace.xml.
 *
 * @author had
 * @version $Revision: $ ($Author: $)
 */
public class WorkspaceXmlUtil {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WorkspaceXmlUtil.class);

    public static List getWorkspaceNamesWithIndexer() {
        List names = new ArrayList();
        final File sourceDir = new File(Path.getAppRootDir() + "/repositories/magnolia/workspaces/");
        final SAXBuilder builder = new SAXBuilder();
        File[] files = sourceDir.listFiles();
        if (files == null) {
            // new repo
            return names;
        }
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (!f.isDirectory()) {
                continue;
            }
            File wks = new File(f, "workspace.xml");
            if (!wks.exists() || !wks.canRead()) {
                continue;
            }
            log.debug("Checking {} for old indexer.", f.getName());
            try {
                // check for the indexer def in wks
                List list = getElementsFromXPath(builder.build(wks), "/Workspace/SearchIndex/param[@name='textFilterClasses']/@value");
                if (list.size() > 0 && ((Attribute) list.get(0)).getValue().matches(".*\\.core\\.query\\..*")) {
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

    private static List getElementsFromXPath(Document doc, String xpathExpr) {
        try {
            final XPath xpath = XPath.newInstance(xpathExpr);
            // must add the namespace and use it: there is no default namespace elsewise
            xpath.addNamespace("ws", "file://workspace.xml");
            return xpath.selectNodes(doc);
        } catch (JDOMException e) {
            throw new RuntimeException(e); // TODO
        }
    }
}
