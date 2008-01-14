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
package info.magnolia.module.workflow.setup.for3_5;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.workflow.WorkflowConstants;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.value.StringValue;


/**
 * @author vsteller
 * @version $Id$
 *
 */
public class CheckAndUpdateDefaultWorkflowDefinition extends AbstractTask {

    public CheckAndUpdateDefaultWorkflowDefinition() {
        super("Update the default activation workflow definition", "Checks and updates the default activation workflow definition under the /modules/workflow/config/flows/activation config node.");
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        InputStream stream = null;
        InputStream stream30 = null;
        try {
            stream = ClasspathResourcesUtil.getStream("info/magnolia/module/workflow/default-activation-workflow.xml");
            final String wfDef = IOUtils.toString(stream);
            stream30 = ClasspathResourcesUtil.getStream("info/magnolia/module/workflow/from30/activation-workflow-from-3.0-ee-bootstrap-file.xml");
            final String wfDef30 = IOUtils.toString(stream30);
            final Content cfg = ctx.getOrCreateCurrentModuleConfigNode();

            final Content flows = ContentUtil.getOrCreateContent(cfg, "flows", ItemType.CONTENT);
            final Content flowNode = ContentUtil.getOrCreateContent(flows, "activation", ItemType.CONTENTNODE);
            
            if (flowNode.hasNodeData(WorkflowConstants.FLOW_VALUE)) {
                final String oldWfDef = NodeDataUtil.getString(flowNode, WorkflowConstants.FLOW_VALUE);
                if (StringUtils.deleteWhitespace(oldWfDef).equals(StringUtils.deleteWhitespace(wfDef30))) {
                    flowNode.setNodeData(WorkflowConstants.FLOW_VALUE, new StringValue(wfDef));
                } else {
                    ctx.warn("The activation workflow definition has been modified. Magnolia will not update it but instead install the new workflow definition for 3.5 in /modules/workflow/config/flows/activation35. Please review the changes manually.");
                    final Content flowNode35 = flows.createContent("activation35", ItemType.CONTENTNODE);
                    flowNode35.setNodeData(WorkflowConstants.FLOW_VALUE, new StringValue(wfDef));
                }
            } else {
                // this case happens if we e.g. update from 3.0.x CE
                flowNode.createNodeData(WorkflowConstants.FLOW_VALUE, wfDef);
            }
        } catch (IOException e) {
            ctx.error("Could not read default activation workflow definition", e);
        } catch (RepositoryException e) {
            ctx.error("Could not store default activation workflow definition", e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                    stream30.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
