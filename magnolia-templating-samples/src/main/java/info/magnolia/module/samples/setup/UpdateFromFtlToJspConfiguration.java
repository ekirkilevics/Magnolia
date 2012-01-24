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
package info.magnolia.module.samples.setup;

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;


/**
 *
 * Update FTL properties (config.templates and dialog) in order to create the equivalent JSP configuration.
 *
 * @version $Id$
 *
 */
public class UpdateFromFtlToJspConfiguration extends AbstractRepositoryTask{

    private final String workspaceName;
    private final String nodePath;

    public UpdateFromFtlToJspConfiguration(String name, String description, String workspaceName, String nodePath) {
        super(name, description);
        this.nodePath = nodePath;
        this.workspaceName = workspaceName;
    }

    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        try {
            Node currentnode = installContext.getJCRSession(workspaceName).getNode(nodePath);
            handleNode(currentnode);
            //Get all node children. Exclude MetaData
            Iterable<Node> children = NodeUtil.collectAllChildren(currentnode);
            Iterator<Node> childrenIterator = children.iterator();
            while (childrenIterator.hasNext()) {
                handleNode(childrenIterator.next());
            }


        } catch (Exception e) {
            installContext.error("Can't move attributes for node: " + nodePath, e );
        }


    }

    /**
     * Handle node properties to be changed.
     */
    private void handleNode(Node node) throws Exception {
        //Change renderType
        if(node.hasProperty("renderType")) {
            node.getProperty("renderType").setValue("jsp");
        }

        //Change templateScript
        if(node.hasProperty("templateScript")) {
            String currentValue = node.getProperty("templateScript").getString();
            String newValue = "/templates"+StringUtils.substringBefore(currentValue, ".ftl")+".jsp";
            node.getProperty("templateScript").setValue(newValue);
        }

        //Change id
        if(node.hasProperty("id")) {
            String currentValue = node.getProperty("id").getString();
            String newValue = StringUtils.replace(currentValue,"/ftl/","/jsp/");
            node.getProperty("id").setValue(newValue);
        }

        //Change templateId
        if(node.hasProperty("templateId")) {
            String currentValue = node.getProperty("templateId").getString();
            String newValue = StringUtils.replace(currentValue,"/ftl/","/jsp/");
            node.getProperty("templateId").setValue(newValue);
        }

        //Change title
        if(node.hasProperty("title")) {
            String currentValue = node.getProperty("title").getString();
            if(currentValue.contains("FTL")) {
                String newValue = StringUtils.replace(currentValue,"FTL","JSP");
                node.getProperty("title").setValue(newValue);
            }
          }

        //Change description
        if(node.hasProperty("description")) {
            String currentValue = node.getProperty("description").getString();
            if(currentValue.contains("FTL")) {
                String newValue = StringUtils.replace(currentValue,"FTL","JSP");
                node.getProperty("description").setValue(newValue);
            }
          }

        //Change target
        if(node.hasProperty("target")) {
            String currentValue = node.getProperty("target").getString();
            if(currentValue.contains("/ftl-")) {
                String newValue = StringUtils.replace(currentValue,"/ftl-","/jsp-");
                node.getProperty("target").setValue(newValue);
            }
          }

    }

}
