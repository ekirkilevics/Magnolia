package info.magnolia.module.workflow.trees;

import info.magnolia.module.workflow.trees.WorkflowTreeConfiguration;
import info.magnolia.module.admininterface.trees.ConfigTreeHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Tree to view workitems and expressions
 */
public class WorkflowTreeHandler extends ConfigTreeHandler {

    public WorkflowTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        this.setConfiguration(new WorkflowTreeConfiguration());
    }

    
}
