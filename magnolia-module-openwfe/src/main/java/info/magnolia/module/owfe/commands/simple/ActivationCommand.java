package info.magnolia.module.owfe.commands.simple;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.module.owfe.commands.MgnlCommand;
import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

public class ActivationCommand extends MgnlCommand {

    public boolean exec(HashMap params, Context ctx) {
        String path;
        boolean recursive;
        path = (String) params.get(P_PATH);
        recursive = ((Boolean) params.get(P_RECURSIVE)).booleanValue();
        try {
            doActivate(path, recursive);
        } catch (Exception e) {
            log.error("cannot do activate", e);
            return false;
        }
        return true;
    }

    private void doActivate(String path, boolean recursive) throws Exception {
        Rule rule = new Rule();
        rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
        rule.addAllowType(ItemType.NT_FILE);
        if (recursive) {
            rule.addAllowType(ItemType.CONTENT.getSystemName());
        }

        Syndicator syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
        syndicator.init(MgnlContext.getUser(), REPOSITORY, ContentRepository.getDefaultWorkspace(REPOSITORY), rule);

        String parentPath = StringUtils.substringBeforeLast(path, "/");
        if (StringUtils.isEmpty(parentPath)) {
            parentPath = "/";
        }
        syndicator.activate(parentPath, path);

    }

}
