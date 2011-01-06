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
package info.magnolia.setup.for3_5;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllChildrenNodesOperation;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.setup.AddFilterBypassTask;
import info.magnolia.voting.voters.URIStartsWithVoter;

import javax.jcr.RepositoryException;


/**
 * Transforms unsecured URIs into bypasses of uriSecurity filter.
 * @author vsteller
 * @version $Id$
 *
 */
public class CheckAndUpdateUnsecureURIs extends AllChildrenNodesOperation implements Task {
    private static final String FILTER_URI_SECURITY = "/server/filters/uriSecurity";
    private static final String PROPERTY_URI = "URI";
    private final ArrayDelegateTask subtasks;

    public CheckAndUpdateUnsecureURIs(String existingUnsecureURIList) {
        super("Filters", "Transforms old unsecure URIs to URISecurityFilter bypasses.", ContentRepository.CONFIG, existingUnsecureURIList);
        subtasks = new ArrayDelegateTask("Unsecure URI transformations");
    }

    public void execute(InstallContext installContext) throws TaskExecutionException {
        super.execute(installContext);
        subtasks.execute(installContext);
    }

    protected void operateOnChildNode(Content node, InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final String bypassName = node.getName();
        final String bypassPattern = NodeDataUtil.getString(node, PROPERTY_URI);
        final Class bypassClass = URIStartsWithVoter.class;
        subtasks.addTask(new AddFilterBypassTask(FILTER_URI_SECURITY, bypassName, bypassClass, bypassPattern));
    }
}
