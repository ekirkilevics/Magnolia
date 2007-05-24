/**
 *
 */
package info.magnolia.module.workflow.flows;

import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.module.workflow.WorkflowConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import openwfe.org.engine.workitem.LaunchItem;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Used in the community edition. Only the default activation flow is supported.
 * @author philipp
 * @version $Id$
 * @deprecated this was used in the comunity edition to restrict to a single flow definition
 */
public class SimpleFlowDefinitionManager implements FlowDefinitionManager {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SimpleFlowDefinitionManager.class);

    public void configure(LaunchItem launchItem, String workflowName) throws FlowDefinitionException {
        if(!StringUtils.equals(workflowName, WorkflowConstants.DEFAULT_WORKFLOW)){
            throw new FlowDefinitionException("only default workflow supported in Community Edition, will fallback to default flow");
        }

        launchItem.setWorkflowDefinitionUrl(WorkflowConstants.ATTRIBUTE_WORKFLOW_DEFINITION_URL);
        String flowDef = readDefinition(WorkflowConstants.DEFAULT_WORKFLOW);
        launchItem.getAttributes().puts(WorkflowConstants.ATTRIBUTE_DEFINITION, flowDef);
    }

    public String readDefinition(String workflowName) throws FlowDefinitionException {
        if(!StringUtils.equals(workflowName, WorkflowConstants.DEFAULT_WORKFLOW)){
            throw new FlowDefinitionException("only default workflow supported in Community Edition, will fallback to default flow");
        }

        InputStream stream = null;
        try {
            stream = ClasspathResourcesUtil.getStream("info/magnolia/module/workflow/default.xml");
            return IOUtils.toString(stream);
        }
        catch (IOException e) {
            log.error("can't read flow definition", e);
        }
        finally{
            IOUtils.closeQuietly(stream);
        }
        return StringUtils.EMPTY;
    }

    public void saveDefinition(String definition) throws FlowDefinitionException {
        throw new FlowDefinitionException("saving of workflow definitions is not supported in the Community Edition");
    }

    public List getDefinitionNames() {
        List list = new ArrayList();
        list.add("default");
        return list;
    }
}
