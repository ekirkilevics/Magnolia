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
 * Update FTL properties (config.virtualURIMapping) in order to create the equivalent JSP configuration.
 *
 * @version $Id$
 *
 */
public class UpdateFromFtlToJspVirtualUriMapping extends AbstractRepositoryTask{

    private final String workspaceName;
    private final String nodePath;

    public UpdateFromFtlToJspVirtualUriMapping(String name, String description, String workspaceName, String nodePath) {
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


        //Change fromURI
        if(node.hasProperty("fromURI")) {
            String currentValue = node.getProperty("fromURI").getString();
            String newValue = StringUtils.replace(currentValue,"/ftl-","/jsp-");
            node.getProperty("fromURI").setValue(newValue);
        }
        //Change toURI
        if(node.hasProperty("toURI")) {
            String currentValue = node.getProperty("toURI").getString();
            String newValue = StringUtils.replace(currentValue,"/ftl-","/jsp-");
            node.getProperty("toURI").setValue(newValue);
        }
    }

}
