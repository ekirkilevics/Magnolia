package info.magnolia.module.owfe.flow;

import info.magnolia.cms.beans.commands.CommandsMap;
import info.magnolia.cms.beans.commands.MgnlCommand;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.Context;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.beans.runtime.WebContextImpl;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;
import info.magnolia.module.owfe.MgnlConstants;
import info.magnolia.module.owfe.commands.ParametersSetterHelper;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * This is a subclass of the regular MVCHandler to plug in flow events. <p/> In
 * this case, only the activate method is part of a flow. We should find a way
 * to plug in flow on the different methods.
 *
 * @author jackie
 * @author Niko
 */

public abstract class FlowAdminTreeMVCHandler extends AdminTreeMVCHandler {

    private static Logger log = Logger
            .getLogger(info.magnolia.module.owfe.flow.FlowAdminTreeMVCHandler.class);

    public FlowAdminTreeMVCHandler(String name, HttpServletRequest vrequest,
                                   HttpServletResponse vresponse) {
        super(name, vrequest, vresponse);
    }

    /**
     * execute dynamic command
     */
    public String execute(String command) {
        // get command from command map in JCR repository
        MgnlCommand tc = (MgnlCommand) CommandsMap.getCommand(MgnlConstants.WEBSITE_REPOSITORY, command);
        if (tc == null) { // not found, do in the old ways
            if (log.isDebugEnabled())
                log.debug("can not find command named " + command + " in tree command map");
            return super.execute(command);
        }
        if (log.isDebugEnabled())
            log.debug("find command for " + command + ": " + tc);

        // set parameters
        HashMap params = new HashMap();
        params.put(MgnlConstants.P_REQUEST, request);
        params.put(MgnlConstants.P_TREE, tree);
        
        // add start date and end date
        HierarchyManager hm =  ContentRepository
		.getHierarchyManager(ContentRepository.WEBSITE);
        Content ct = null;
        try{
        	ct = hm.getContent(pathSelected);
        	Calendar cd = ct.getMetaData().getStartTime();
        	String date = new Timestamp(cd.getTimeInMillis()).toString();
        	log.info("start date = " +  date);
        	//date = "2006-10-10";
    		params.put("startDate", date);
    		
    		cd = ct.getMetaData().getEndTime();
    		date = new Timestamp(cd.getTimeInMillis()).toString();
        	log.info("end date = " +  date);   		
    		params.put("endDate", date);
        }catch(Exception e){
        	log.warn("can not get start/end date for path " + pathSelected+", please use sevlet FlowDef to set start/end date for node. exception:" + e);
        }        
        
		params.put(MgnlConstants.P_PATH, pathSelected);	
		
		String recursive = "false";
		if (request.getParameter("recursive")!=null)
			recursive = "true";
		params.put(MgnlConstants.P_RECURSIVE, recursive);

		// for testing
		log.info("recursive = " + recursive);
		
		Context context = (MgnlContext.hasInstance()) ? MgnlContext.getInstance() : new WebContextImpl();
        context.put(MgnlConstants.P_REQUEST, request);
        context.put(MgnlConstants.INTREE_PARAM, params);

        
       
        try {
        	// translate parameter
            new ParametersSetterHelper().translateParam(tc, context);
            // execute
            tc.execute(context);
        } catch (Exception e) {
            // TODO: check that this is processed somewhere else
            log.error("Error while executing the command:" + command, e);
        }

        return VIEW_TREE;
    }

    /**
     * Override this method to configure the tree control (define the columns,
     * ...)
     *
     * @param vtree
     * @param vrequest
     */
    protected abstract void prepareTree(Tree vtree, HttpServletRequest vrequest);

    /**
     * Prepare the context menu of the tree. This is called during renderTree
     *
     * @param vtree
     * @param vrequest
     */
    protected abstract void prepareContextMenu(Tree vtree, HttpServletRequest vrequest);

}
