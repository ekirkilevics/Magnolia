/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.workflow.setup.for3_1;

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
public class InstallDefaultWorkflowDefinition extends AbstractTask {

    public InstallDefaultWorkflowDefinition() {
        super("Setup default activation workflow definition", "Adds the default activation workflow definition under the /modules/workflow/config/flows/activation config node.");
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        InputStream stream = null;
        try {
            stream = ClasspathResourcesUtil.getStream("info/magnolia/module/workflow/default-activation-workflow.xml");
            final String wfDef = IOUtils.toString(stream);
            final Content cfg = ctx.getOrCreateCurrentModuleConfigNode();

            final Content flows = ContentUtil.getOrCreateContent(cfg, "flows", ItemType.CONTENT);
            final Content flowNode = ContentUtil.getOrCreateContent(flows, "activation", ItemType.CONTENTNODE);
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
