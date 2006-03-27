package info.magnolia.module.owfe.commands.intreecommands.flow;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.owfe.commands.MgnlCommand;
import info.magnolia.module.owfe.commands.intreecommands.AbstractInTreeCommand;

import java.util.HashMap;

import openwfe.org.engine.workitem.StringAttribute;

public class ScheduledActivationCommand extends AbstractInTreeCommand {

	public String getTargetCommand() {
		return "scheduledFlowActivate";
	}

	public HashMap translateParam(HashMap param) {
		HashMap params = new HashMap();
		String pathSelected = (String)param.get(MgnlCommand.P_PATH);
		log.info("param = " + param);
		log.info("params = " + params);
		log.info("param.get(MgnlCommand.P_PATH)=" + pathSelected);
		
        // get content
        HierarchyManager hm =  ContentRepository
		.getHierarchyManager(ContentRepository.WEBSITE);
        Content ct = null;
        try{
        	ct = hm.getContent(pathSelected);
        }catch(Exception e){
        	log.error("can not get content node for path " + pathSelected, e);
        }
        
        
		params.put(MgnlCommand.P_PATH, pathSelected);
		params.put(MgnlCommand.P_RECURSIVE, MgnlCommand.P_RECURSIVE);
		params.put("startDate", ct.getMetaData("startDate").getStartTime());
		params.put("endDate", ct.getMetaData("endDate").getEndTime());
		
		return params;
	}

}
