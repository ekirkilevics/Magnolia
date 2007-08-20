package info.magnolia.cms.cache;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AddCacheVoterTask;
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.module.delta.Task;
import info.magnolia.voting.voters.RequestHasParametersVoter;

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
    protected List getBasicInstallTasks(InstallContext installContext) {

        List<Task> installTasks = super.getBasicInstallTasks(installContext);
        installTasks.add(new FilterOrderingTask("cache", new String[]{"activation", "i18n"}));

        Map config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("trueValue", new Long(-1));
        installTasks.add(new AddCacheVoterTask("notWithParametersVoter", RequestHasParametersVoter.class, config));

        config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("falseValue", new Long(-1));
        config.put("trueValue", new Long(0));
        installTasks.add(new AddCacheVoterTask("extensionVoter", RequestHasParametersVoter.class, config));

        config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("trueValue", new Long(-1));
        installTasks.add(new AddCacheVoterTask("notOnAdminVoter", RequestHasParametersVoter.class, config));

        config = new HashMap();
        config.put("enabled", Boolean.FALSE);
        config.put("trueValue", new Long(-1));
        installTasks.add(new AddCacheVoterTask("notAuthenticatedVoter", RequestHasParametersVoter.class, config));

        return installTasks;
    }

}
