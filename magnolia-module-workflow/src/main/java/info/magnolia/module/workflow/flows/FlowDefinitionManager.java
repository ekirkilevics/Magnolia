/**
 * 
 */
package info.magnolia.module.workflow.flows;

import java.util.List;

import openwfe.org.engine.workitem.LaunchItem;

/**
 * Manages flow definitions.Configures the item to use a specifig flow.
 * @author philipp
 * @version $Id$
 *
 */
public interface FlowDefinitionManager {

    /**
     * Configure the launchItem, so that it will use the workflow
     */
    void configure(LaunchItem launchItem, String workflowName) throws FlowDefinitionException;
    
    /**
     * The flow name gets extracted out of the workflow definition.
     * @param definition the workflow definition xml
     */
    void saveDefinition(String definition) throws FlowDefinitionException;
    
    /**
     * Read the workflow definition stored with this name
     */
    String readDefinition(String workflowName) throws FlowDefinitionException;

    /**
     * List all available workflow names
     */
    List getDefinitionNames() throws FlowDefinitionException;
}
