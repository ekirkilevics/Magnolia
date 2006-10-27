/**
 * 
 */
package info.magnolia.module.workflow.flows;

import openwfe.org.engine.workitem.LaunchItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Configures the item to use a specifig flow.
 * @author philipp
 * @version $Id$
 *
 */
public interface FlowDefinitionManager {

    /**
     * @param launchItem
     * @param workflowName
     */
    void configure(LaunchItem launchItem, String workflowName);
    
    void saveDefinition(String workflowName, String definition);
    
    String readDefinition(String workflowName);
}
