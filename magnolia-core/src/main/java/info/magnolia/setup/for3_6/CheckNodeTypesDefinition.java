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
package info.magnolia.setup.for3_6;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractCondition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Checks custom node type definition file for occurrence of mix:versionable.
 * @author had
 * @version $Id: $
 *
 */
public class CheckNodeTypesDefinition extends AbstractCondition {

    private static Logger log = LoggerFactory.getLogger(CheckNodeTypesDefinition.class);

    public CheckNodeTypesDefinition() {
        super("Check existing node types definition", "Checks existing node types definition for occurence of mix:versionable.");
    }

    public boolean check(InstallContext installContext) {
        String home = SystemProperty.getProperty("magnolia.repositories.home");
        File repoHome = new File(Path.getAbsoluteFileSystemPath(home));

        File nodeTypeFile = new File(repoHome, "magnolia/repository/nodetypes/custom_nodetypes.xml");

        if (!nodeTypeFile.exists()) {
            // don't crash if the repository.xml has been customized and doesn't use the magnolia.repositories.home
            // property for the base path
            return true;
        }

        SAXBuilder builder = new SAXBuilder();
        Document doc;
        try {
            doc = builder.build(nodeTypeFile);
            Collection nodeTypes = doc.getRootElement().getChildren();
            for (Iterator iterator = nodeTypes.iterator(); iterator.hasNext();) {
                Element nodeType = (Element) iterator.next();
                Element supertypesElement = nodeType.getChild("supertypes");
                Collection supertypes = new ArrayList(supertypesElement.getChildren());
                for (Iterator iterator2 = supertypes.iterator(); iterator2.hasNext();) {
                    Element supertypeElement = (Element) iterator2.next();
                    String supertype = supertypeElement.getText();
                    if(supertype.equals(ItemType.MIX_VERSIONABLE)){
                        String msg = "Found mix:versionable as a supertype in the custom_nodetypes.xml. Please replace this with mix:referenceable and restart the server. Refer to Magnolia Documentation at  http://documentation.magnolia.info/releases/3-6.html for details.";
                        installContext.error(msg, new Exception(msg));
                        return false;
                    }
                }
            }
        } catch (JDOMException e) {
            String msg = "Failed to parse custom_nodetypes.xml due to " + e.getMessage();
            log.error(msg, e);
            return false;
        } catch (IOException e) {
            String msg = "Failed to access custom_nodetypes.xml due to " + e.getMessage();
            log.error(msg, e);
            return false;
        }
        return true;
    }
}
