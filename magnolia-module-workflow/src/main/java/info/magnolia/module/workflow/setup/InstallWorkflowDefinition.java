/**
 * This file Copyright (c) 2007-2008 Magnolia International
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
package info.magnolia.module.workflow.setup;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.workflow.WorkflowConstants;
import org.apache.commons.io.IOUtils;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class InstallWorkflowDefinition extends AbstractTask {
    private final String flowName;
    private final String resourcePath;

    public InstallWorkflowDefinition(String taskName, String taskDescription, String flowName, String resourcePath) {
        super(taskName, taskDescription);
        this.flowName = flowName;
        this.resourcePath = resourcePath;
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        InputStream stream = null;
        try {
            stream = ClasspathResourcesUtil.getStream(resourcePath);
            final String wfDef = IOUtils.toString(stream);
            final Content cfg = ctx.getOrCreateCurrentModuleConfigNode();

            final Content flows = ContentUtil.getOrCreateContent(cfg, "flows", ItemType.CONTENT);
            final Content flowNode = ContentUtil.getOrCreateContent(flows, flowName, ItemType.CONTENTNODE);
            flowNode.createNodeData(WorkflowConstants.FLOW_VALUE, wfDef);
        } catch (IOException e) {
            ctx.error("Could not read default activation workflow definition", e);
        } catch (RepositoryException e) {
            ctx.error("Could not store default activation workflow definition", e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
