package info.magnolia.module.owfe.commands.intreecommands.flow;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.owfe.commands.MgnlCommand;
import info.magnolia.module.owfe.commands.intreecommands.AbstractInTreeCommand;

import java.util.HashMap;

public class ScheduledActivationCommand extends AbstractInTreeCommand {
    private static final String SCHEDULED_FLOW_ACTIVATE = "scheduledFlowActivate";

    public String getTargetCommand() {
        return SCHEDULED_FLOW_ACTIVATE;
    }

    public HashMap translateParam(HashMap param) {
        HashMap params = new HashMap();
        String pathSelected = (String) param.get(MgnlCommand.P_PATH);
        if (log.isDebugEnabled()) {
            log.debug("param = " + param);
            log.debug("params = " + params);
            log.debug("param.get(MgnlCommand.P_PATH)=" + pathSelected);
        }

        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.WEBSITE);
        Content ct = null;
        try {
            ct = hm.getContent(pathSelected);
        } catch (Exception e) {
            log.error("can not get content node for path " + pathSelected, e);
        }

        params.put(MgnlCommand.P_PATH, pathSelected);
        params.put(MgnlCommand.P_RECURSIVE, MgnlCommand.P_RECURSIVE);
        params.put(MgnlCommand.P_START_DATE, ct.getMetaData(MgnlCommand.P_START_DATE).getStartTime());
        params.put(MgnlCommand.P_END_DATE, ct.getMetaData(MgnlCommand.P_END_DATE).getEndTime());

        return params;
    }

}
