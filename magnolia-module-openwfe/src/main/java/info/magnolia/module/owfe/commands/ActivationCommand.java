package info.magnolia.module.owfe.commands;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import openwfe.org.engine.workitem.InFlowWorkItem;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

public class ActivationCommand extends AbstractTreeCommand {

    public boolean execute(HashMap params) {

        String path;
        boolean recursive; // is initialized at false
        InFlowWorkItem if_wi = (InFlowWorkItem) params.get(AbstractTreeCommand.P_WORKITEM);
        if (if_wi != null) { // if call from flow
            path = (if_wi.getAttribute("pathSelected")).toString();
            recursive = (if_wi.getAttribute("recursive")).equals("true");
        } else {
            path = (String) params.get("pathSelected");
            recursive = ((Boolean) params.get("recursive")).booleanValue();
        }
        try {
            doActivate(path, recursive);
        } catch (Exception e) {
            log.error("cannot do activat", e);
            return false;
        }
        return true;
    }

    private void doActivate(String path, boolean recursive) throws Exception {
        /**
         * Here rule defines which content types to collect, its a resposibility
         * of the caller ro set this, it will be different in every hierarchy,
         * for instance - in website tree recursive activation : rule will allow
         * mgnl:contentNode, mgnl:content and nt:file - in website tree
         * non-recursive activation : rule will allow mgnl:contentNode and
         * nt:file only
         */
        Rule rule = new Rule();
        rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
        rule.addAllowType(ItemType.NT_FILE);
        if (recursive) {
            rule.addAllowType(ItemType.CONTENT.getSystemName());
        }

        Syndicator syndicator = (Syndicator) FactoryUtil
                .getInstance(Syndicator.class);
        syndicator.init(MgnlContext.getUser(), "website", ContentRepository
                .getDefaultWorkspace("website"), rule);

        String parentPath = StringUtils.substringBeforeLast(path, "/");
        if (StringUtils.isEmpty(parentPath)) {
            parentPath = "/";
        }
        syndicator.activate(parentPath, path);

    }

}
