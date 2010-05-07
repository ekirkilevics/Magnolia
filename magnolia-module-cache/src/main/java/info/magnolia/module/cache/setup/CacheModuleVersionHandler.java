/**
 * This file Copyright (c) 2003-2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.cache.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.cache.RegisterWorkspaceForCacheFlushingTask;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BackupTask;
import info.magnolia.module.delta.BootstrapResourcesTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.Condition;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.NewPropertyTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.PropertyExistsDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.RemovePropertyTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.delta.WarnTask;
import info.magnolia.module.delta.WebXmlConditionsUtil;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

/**
 * @author fgiust
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CacheModuleVersionHandler extends DefaultModuleVersionHandler {

    public CacheModuleVersionHandler() {
        super();
        final List<Condition> conditions = new ArrayList<Condition>();
        final WebXmlConditionsUtil u = new WebXmlConditionsUtil(conditions);
        u.servletIsRemoved("CacheServlet");
        u.servletIsRemoved("CacheGeneratorServlet");
        register(DeltaBuilder.update("3.5.0", "").addConditions(conditions)
                // bootstrap file removed - and not needed even if we're upgrading from 3.0
                // .addTask(new BootstrapSingleResource("Configured Observation", "Adds the repositories to be observed.", "/mgnl-bootstrap/cache/config.modules.cache.config.repositories.xml"))
        );

        final NodeExistsDelegateTask addFilterIfNotExisting = new NodeExistsDelegateTask("Check cache filter", "A bug in previous 3.5 releases made it possible for the cache filter to be removed. This task readds it if necessary.",
                ContentRepository.CONFIG, "/server/filters/cache", null, new ArrayDelegateTask("",
                        new BootstrapSingleResource("", "", "/mgnl-bootstrap/cache/config.server.filters.cache.xml"),
                        new FilterOrderingTask("cache", new String[]{"i18n"}),
                        new WarnTask("", "The cache filter was not found, and was just added. If you removed it on purpose, you should consider disabling it instead.")));

        register(DeltaBuilder.update("3.5.9", "")
                .addTask(addFilterIfNotExisting)
        );

        register(DeltaBuilder.update("3.6", "New cache API and configuration.")
                .addTask(new BackupTask("config", "/modules/cache/config", true))
                .addTask(new BootstrapResourcesTask("New configuration", "Bootstraps new default cache configuration.") {
                    protected String[] getResourcesToBootstrap(final InstallContext installContext) {
                        return new String[]{
                                "/mgnl-bootstrap/cache/config.modules.cache.config.configurations.default.xml",
                                "/mgnl-bootstrap/cache/config.modules.cache.config.cacheFactory.xml"
                        };
                    }
                })
                .addTask(new ArrayDelegateTask("New cache filter", "Replaces the old cache filter with info.magnolia.module.cache.filter.CacheFilter.",
                        new CheckAndModifyPropertyValueTask("", "", ContentRepository.CONFIG, "/server/filters/cache", "class", "info.magnolia.cms.cache.CacheFilter", "info.magnolia.module.cache.filter.CacheFilter"),
                        new NewPropertyTask("", "", ContentRepository.CONFIG, "/server/filters/cache", "cacheConfigurationName", "default")
                ))
                .addTask(new FilterOrderingTask("cache", "The cache filter should now be placed before the i18n filter.", new String[]{"context", "multipartRequest", "activation"}))
                .addTask(new ArrayDelegateTask("New gzip filter", "Adds the new gzip filter.",
                        new BootstrapSingleResource("", "", "/mgnl-bootstrap/cache/config.server.filters.gzip.xml"),
                        new FilterOrderingTask("gzip", new String[]{"cache"})
                ))
        );

        register(DeltaBuilder.update("3.6.2", "Updated executors and filter configuration.")
                .addTask(new BootstrapResourcesTask("Updated configuration", "Bootstraps new default cache configuration.") {
                    protected String[] getResourcesToBootstrap(final InstallContext installContext) {
                        return new String[]{
                                "/mgnl-bootstrap/cache/setup/config.modules.cache.config.configurations.default.executors.store.cacheContent.compressible.xml",
                                "/mgnl-bootstrap/cache/setup/config.modules.cache.config.configurations.default.executors.store.serveContent.xml"
                        };
                    }
                })
                .addTask(new FilterOrderingTask("cache", "The gzip filter should now be placed before the cache filter.", new String[]{"gzip"}))
        );

        register(DeltaBuilder.update("3.6.4", "Update gzip and cache compression configuration.").addTasks(getTasksFor364()));

        register(DeltaBuilder.update("4.1", "New flush policy configuration.").addTask(
                new ArrayDelegateTask("New flush policy configuration", "Sets up the new flush policy configuration.",
                        // move existing policy to temp
                        new MoveNodeTask("","", ContentRepository.CONFIG, "/modules/cache/config/configurations/default/flushPolicy","/modules/cache/config/configurations/default/tmp", false),
                        // create new policy configuration
                        new CreateNodeTask("","", ContentRepository.CONFIG, "/modules/cache/config/configurations/default", "flushPolicy", ItemType.CONTENTNODE.getSystemName()),
                        new SetPropertyTask(ContentRepository.CONFIG,"/modules/cache/config/configurations/default/flushPolicy", "class", "info.magnolia.module.cache.DelegateFlushPolicy"),
                        new CreateNodeTask("","", ContentRepository.CONFIG, "/modules/cache/config/configurations/default/flushPolicy", "policies", ItemType.CONTENTNODE.getSystemName()),
                        // move original config under the new one
                        new MoveNodeTask("","", ContentRepository.CONFIG, "/modules/cache/config/configurations/default/tmp","/modules/cache/config/configurations/default/flushPolicy/policies/flushAll", false)
                )));

        register(DeltaBuilder.update("4.3", "Makes cache aware of different sites and locales used to access the content")
                .addTask(new NodeExistsDelegateTask("Check cache filter", "Reorder i18n filter prior to cache filter to allow cache access to i18n information.",
                        ContentRepository.CONFIG, "/server/filters/cache", new FilterOrderingTask("cache", new String[]{"i18n"}),
                                new WarnTask("", "The cache filter was not found. If you removed it on purpose, you should consider disabling it instead.")))
                .addTask(new PropertyExistsDelegateTask("Cache policy re-configuration", "Removes no longer used multihost property from default cache policy.", ContentRepository.CONFIG, "/modules/cache/config/configurations/default/cachePolicy", "multiplehosts", new RemovePropertyTask("", "", ContentRepository.CONFIG, "/modules/cache/config/configurations/default/cachePolicy", "multiplehosts")))
                .addTask(new ArrayDelegateTask("Cache Flushing","Adds new commands to flush the cache.",
                        new BootstrapSingleResource("", "", "/mgnl-bootstrap/cache/config.modules.cache.commands.cache.flushAll.xml"),
                        new BootstrapSingleResource("", "", "/mgnl-bootstrap/cache/config.modules.cache.commands.cache.flushByUUID.xml")))
                .addTask(new BootstrapSingleResource("Cache Flushing", "Adds cache configuration for uuid-key caching.", "/mgnl-bootstrap/cache/config.modules.cache.config.configurations.uuid-key-mapping.xml"))
                );

        register(DeltaBuilder.update("4.3.1", "Disables server side re-caching of requests with no-cache header (shift reload)")
                .addTask(new NewPropertyTask("Disabling no-cache requests", "Disable server side re-caching of requests with no-cache header (shift reload)", "config", "/modules/cache/config/configurations/default/cachePolicy", "refreshOnNoCacheRequests", "false"))
                .addTask(new WarnTask("Warning", "Server side re-caching of requests with no-cache header (shift reload) were disabled. This can be changed at /modules/cache/config/configurations/default/cachePolicy/refreshOnNoCacheRequests"))
                );
        register(DeltaBuilder.update("4.3.2", "Make waiting for cache entry configurable")
                .addTask(new NewPropertyTask("Set cache new entry timeout", "Makes sure incoming requests are not waiting for cache entries to be created longer then specified timeout", ContentRepository.CONFIG, "/modules/cache/config/cacheFactory", "blockingTimeout", "4000")));
    }

    private List<Task> getTasksFor364() {
        List<Task> list = new ArrayList<Task>();
        // Add new compression types and user agents configuration
        list.add(new BootstrapResourcesTask("Updated configuration", "Bootstraps cache compression configuration.") {
            protected String[] getResourcesToBootstrap(final InstallContext installContext) {
                return new String[]{"/mgnl-bootstrap/cache/config.modules.cache.config.compression.xml"};
            }
        });

        // remove previously used compressible content types configuration
        list.add(new DefaultCompressibleContentTypesCondition("Cache cleanup", "Removes obsolete cache compression list in favor of new global configuration.",
                        new RemoveNodeTask("Remove obsolete compression list configuration.", "Removes cache executor specific compression list configuration in favor of using one global list for both cache and gzip.", ContentRepository.CONFIG, "/modules/cache/config/configurations/default/executors/store/cacheContent/compressible"),
                        new WarnTask("Warning", "The compression list configuration have been relocated to /modules/cache/compression/voters/contentType, since you have modified the default configuration, please make sure your customization is applied also to new configuration."),
                        "/modules/cache/config/configurations/default/executors/store/cacheContent/compressible"));

        // remove bypass for compressible content types from gzip filter
        list.add(new DefaultCompressibleContentTypesCondition("GZip cleanup", "Removes obsolete gzip bypass in favor of new global configuration.",
                        new RemoveNodeTask("Remove obsolete bypass.", "Removes content type bypass from gzip filter.", ContentRepository.CONFIG, "/server/filters/gzip/bypasses/contentType"),
                        new WarnTask("Warning", "The list of compressible types have been relocated to /modules/cache/compression/voters/contentType, since you have modified the default configuration, please make sure your customization is applied also to new configuration."),
                        "/server/filters/gzip/bypasses/contentType/allowed"));

        // add new bypass to GzipFilter that executes voters from new compression configuration in /modules/cache/config
        list.add(new AbstractRepositoryTask("Add bypass", "Adds new bypass for GZip filter using global configuration.") {
            protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
                final HierarchyManager hm = installContext.getHierarchyManager(ContentRepository.CONFIG);
                Content content = hm.createContent("/server/filters/gzip/bypasses", "deletageBypass", ItemType.CONTENTNODE.getSystemName());
                content.setNodeData("class","info.magnolia.voting.voters.VoterSet");
                content.setNodeData("delegatePath","/modules/cache/config/compression/voters");
            }
        });

        list.add(new AbstractRepositoryTask("Cache Flushing", "Migrate old cache flushing configuration to new location." ) {
            protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
                final String reposPath= "/modules/cache/config/repositories";
                final HierarchyManager hm = installContext.getHierarchyManager(ContentRepository.CONFIG);
                if (!hm.isExist(reposPath)) {
                    return;
                }
                Content cnt = hm.getContent(reposPath);
                for (Content c : cnt.getChildren()) {
                    if (c.hasNodeData("name")) {
                        new RegisterWorkspaceForCacheFlushingTask(c.getName()).execute(installContext);
                    }
                }
                cnt.delete();
            }});
        return list;
    }

    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<Task>();
        tasks.add(new FilterOrderingTask("gzip", new String[]{"context", "multipartRequest", "activation"}));
        tasks.add(new FilterOrderingTask("cache", new String[]{"gzip", "i18n"}));
        return tasks;
    }


    /* TODO : if we keep this, they should move to cacheStrategy now
    public List getStartupTasks(InstallContext installContext) {
        List tasks = new ArrayList();

        // standard voters that should be always available. They can be disabled by setting the enable flag to false,
        // but their presence will always be checked

        Map config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("not", Boolean.TRUE);
        tasks.add(new AddCacheVoterTask("notWithParametersVoter", RequestHasParametersVoter.class, config));

        // this was replaced by a simple bypass
        config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("falseValue", new Long(-1));
        config.put("trueValue", new Long(0));
        config.put("allow", "html,css,js,jpg,gif,png");
        tasks.add(new AddCacheVoterTask("extensionVoter", ExtensionVoter.class, config));

        config = new HashMap();
        config.put("enabled", Boolean.TRUE);
        config.put("not", Boolean.TRUE);
        tasks.add(new AddCacheVoterTask("notOnAdminVoter", OnAdminVoter.class, config));

        config = new HashMap();
        config.put("enabled", Boolean.FALSE);
        config.put("not", Boolean.TRUE);
        tasks.add(new AddCacheVoterTask("notAuthenticatedVoter", AuthenticatedVoter.class, config));

        return tasks;
    }
    */

}
