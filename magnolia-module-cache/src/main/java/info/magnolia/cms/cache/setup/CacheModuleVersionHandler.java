package info.magnolia.cms.cache.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.voting.voters.AuthenticatedVoter;
import info.magnolia.voting.voters.ExtensionVoter;
import info.magnolia.voting.voters.OnAdminVoter;
import info.magnolia.voting.voters.RequestHasParametersVoter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class CacheModuleVersionHandler extends DefaultModuleVersionHandler {

    /**
     * {@inheritDoc}
     */
    protected List getExtraInstallTasks(InstallContext installContext) {
        final List tasks = super.getBasicInstallTasks(installContext);
        tasks.add(new FilterOrderingTask("cache", new String[]{"i18n"}));
        return tasks;
    }

    /**
     * {@inheritDoc}
     */
    public List getStartupTasks(InstallContext installContext) {
        List tasks = new ArrayList();

        // standard voters that should be always available. They can be disabled by setting the enable flag to false,
        // but their presence will always be checked

        Map config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("trueValue", new Long(-1));
        tasks.add(new AddCacheVoterTask("notWithParametersVoter", RequestHasParametersVoter.class, config));

        config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("falseValue", new Long(-1));
        config.put("trueValue", new Long(0));
        config.put("allow", "html,css,js,jpg,gif,png");
        tasks.add(new AddCacheVoterTask("extensionVoter", ExtensionVoter.class, config));

        config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("trueValue", new Long(-1));
        tasks.add(new AddCacheVoterTask("notOnAdminVoter", OnAdminVoter.class, config));

        config = new HashMap();
        config.put("enabled", Boolean.FALSE);
        config.put("trueValue", new Long(-1));
        tasks.add(new AddCacheVoterTask("notAuthenticatedVoter", AuthenticatedVoter.class, config));

        return tasks;
    }

}
