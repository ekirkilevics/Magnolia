/**
 *
 */
package info.magnolia.module.workflow.flows;

import info.magnolia.cms.util.DeprecationUtil;
import openwfe.org.engine.workitem.LaunchItem;

import java.util.List;

/**
 *
 * @author philipp
 * @version $Id$
 * @deprecated This was used in the 3.0 community edition to restrict to a single flow definition. Since 3.1,
 *             DefaultFlowDefinitionManager is part of CE.
 */
public class SimpleFlowDefinitionManager extends DefaultFlowDefinitionManager {

    public void configure(LaunchItem launchItem, String workflowName) throws FlowDefinitionException {
        DeprecationUtil.isDeprecated("Use DefaultFlowDefinitionManager");
        super.configure(launchItem, workflowName);
    }

    public String readDefinition(String workflowName) throws FlowDefinitionException {
        DeprecationUtil.isDeprecated("Use DefaultFlowDefinitionManager");
        return super.readDefinition(workflowName);
    }

    public void saveDefinition(String definition) throws FlowDefinitionException {
        DeprecationUtil.isDeprecated("Use DefaultFlowDefinitionManager");
        super.saveDefinition(definition);
    }

    public List getDefinitionNames() {
        DeprecationUtil.isDeprecated("Use DefaultFlowDefinitionManager");
        return super.getDefinitionNames();
    }
}
