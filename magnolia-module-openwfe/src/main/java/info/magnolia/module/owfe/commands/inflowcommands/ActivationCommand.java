package info.magnolia.module.owfe.commands.inflowcommands;

import java.util.HashMap;

import org.apache.commons.chain.Context;

import info.magnolia.module.owfe.commands.CommandsMap;
import info.magnolia.module.owfe.commands.MgnlCommand;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.WorkItem;

public class ActivationCommand extends AbstractInFlowCommand{


	static final String TARGETCMD_NAME = "realActivate";
	
	public String  getTargetCommand() {
		return TARGETCMD_NAME;		
	}



	public HashMap translateParam(WorkItem workItem) {
      String path = "";
      boolean recursive = false; // is initialized at false    
      if (workItem != null) { // if call from flow
          path = (workItem.getAttribute(P_PATH)).toString();
          recursive = (workItem.getAttribute(P_RECURSIVE)).equals("true");
      } 
      
      HashMap params = new HashMap();
      params.put(MgnlCommand.P_PATH, path);
      params.put(MgnlCommand.P_RECURSIVE, new Boolean(recursive));
      return params;
      
	}

//	public void onExecute(WorkItem workItem) {
//      String path;
//      boolean recursive; // is initialized at false    
//      if (workItem != null) { // if call from flow
//          path = (workItem.getAttribute(P_PATH)).toString();
//          recursive = (workItem.getAttribute(P_RECURSIVE)).equals("true");
//      } 
//      
//      HashMap params = new HashMap();
//      params.
//		
//	}
	

}
