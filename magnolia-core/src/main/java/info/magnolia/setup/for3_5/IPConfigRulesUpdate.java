/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.setup.for3_5;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllChildrenNodesOperation;
import info.magnolia.module.delta.TaskExecutionException;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Updates pre 3.5 IP configuration rules to the format used since 3.5.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class IPConfigRulesUpdate extends AllChildrenNodesOperation {

    public IPConfigRulesUpdate() {
        super("IPConfig rules changed", "Updates the existing ip access rules to match the new configuration structure.",
                ContentRepository.CONFIG, "/server/IPConfig");
    }

    /**
     * Update made for each subnode.
     *  old configuration:
     *  rule-name (p) IP
     *            (n) Access
     *                (n) 0001
     *                    (p) Method = GET
     *                (n) 0002
     *                    (p) Method = POST
     *
     *  new configuration:
     *  rule-name (p) IP = *
     *            (p) methods = GET,POST
     */
    @Override
    protected void operateOnChildNode(Content node, InstallContext ctx) throws RepositoryException, TaskExecutionException {
        if (node.hasContent("Access")) {
            final Content accessNode = node.getContent("Access");
            final Set methods = new TreeSet(String.CASE_INSENSITIVE_ORDER);
            final Iterator it = accessNode.getChildren().iterator();
            while (it.hasNext()) {
                final Content methodNode = (Content) it.next();
                if (methodNode.hasNodeData("Method")) {
                    final String method = methodNode.getNodeData("Method").getString();
                    methods.add(method.toUpperCase());
                }
            }
            final String methodsStr = StringUtils.join(methods, ',');
            node.createNodeData("methods", methodsStr);
            accessNode.delete();
        }
    }

}
