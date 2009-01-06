/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
import info.magnolia.cms.core.NodeData;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BackupTask;
import info.magnolia.module.delta.BootstrapResourcesTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.ConditionalDelegateTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.module.delta.NewPropertyTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.delta.WarnTask;
import info.magnolia.module.delta.WebXmlConditionsUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
        final List conditions = new ArrayList();
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
                new WarnTask("", "The cache filter was not found, and was just added. If you removed it on purpose, you should consider disabling it instead.")
        )
        );
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
                                "/mgnl-bootstrap/cache/config.modules.cache.config.configurations.default.executors.store.cacheContent.compressible.xml",
                                "/mgnl-bootstrap/cache/config.modules.cache.config.configurations.default.executors.store.serveContent.xml"
                        };
                    }
                })
                .addTask(new FilterOrderingTask("cache", "The gzip filter should now be placed before the cache filter.", new String[]{"gzip"}))
        );

        register(DeltaBuilder.update("4.0", "Update gzip and cache compression configuration.")
                .addTask(new BootstrapResourcesTask("Updated configuration", "Bootstraps cache compression configuration.") {
                    protected String[] getResourcesToBootstrap(final InstallContext installContext) {
                        return new String[]{
                                "/mgnl-bootstrap/cache/config.modules.cache.config.compression.xml"
                        };
                    }
                })
                .addTask(
                        new ConditionalDelegateTask(
                                "Cache cleanup", 
                                "Removes obsolete cache compression list in favor of new global configuration.", 
                                new RemoveNodeTask(
                                        "Remove obsolete compression list configuration.", 
                                        "Removes cache executor specific compression list configuration in favor of using one global list for both cache and gzip.", 
                                        ContentRepository.CONFIG, 
                                        "/modules/cache/config/configurations/default/executors/store/cacheContent/compressible"), 
                                new WarnTask("Warning", "The compression list configuration have been relocated to /modules/cache/compression/voters/contentType, since you have modified the default configuration, please make sure your customization is applied also to new configuration.")
                        ) {
                            protected boolean condition(InstallContext installContext) throws TaskExecutionException {
                                
                                try {
                                    List vals = new ArrayList();
                                    Collection compressionList = installContext.getConfigHierarchyManager().getContent("/modules/cache/config/configurations/default/executors/store/cacheContent/compressible").getNodeDataCollection();
                                    for (Iterator iterator = compressionList.iterator(); iterator.hasNext();) {
                                        NodeData data = (NodeData) iterator.next();
                                        vals.add(data.getString());
                                    }
                                    return vals.remove("text/html") && vals.remove("text/css") && vals.remove("application/x-javascript") && vals.isEmpty();
                                    
                                } catch (RepositoryException e) {
                                    log.error(e.getMessage(), e);
                                }
                                return false;
                            }}
                )
                .addTask(
                        new ConditionalDelegateTask(
                                "GZip cleanup", 
                                "Removes obsolete gzip bypass in favor of new global configuration.", 
                                new RemoveNodeTask(
                                        "Remove obsolete bypass.", 
                                        "Removes content type bypass from gzip filter.", 
                                        ContentRepository.CONFIG, 
                                        "/server/filters/gzip/bypasses/contentType"), 
                                new WarnTask("Warning", "The list of compressible types have been relocated to /modules/cache/compression/voters/contentType, since you have modified the default configuration, please make sure your customization is applied also to new configuration.")
                        ) {
                            protected boolean condition(InstallContext installContext) throws TaskExecutionException {
                                
                                try {
                                    List vals = new ArrayList();
                                    Collection compressionList = installContext.getConfigHierarchyManager().getContent("/server/filters/gzip/bypasses/contentType/allowed").getNodeDataCollection();
                                    for (Iterator iterator = compressionList.iterator(); iterator.hasNext();) {
                                        NodeData data = (NodeData) iterator.next();
                                        vals.add(data.getString());
                                    }
                                    return vals.remove("text/html") && vals.remove("text/css") && vals.remove("application/x-javascript") && vals.isEmpty();
                                    
                                } catch (RepositoryException e) {
                                    log.error(e.getMessage(), e);
                                }
                                return false;
                            }}
                ).addTask(new AbstractRepositoryTask("Add bypass", "Adds new bypass for GZip filter using global configuration.") {
                    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
                        final HierarchyManager hm = installContext.getHierarchyManager(ContentRepository.CONFIG);
                        Content content = hm.createContent("/server/filters/gzip/bypasses", "deletageBypass", ItemType.CONTENTNODE.getSystemName());
                        content.createNodeData("class","info.magnolia.voting.voters.VoterSet");
                        content.createNodeData("delegatePath","/modules/cache/config/compression/voters");
                    }
                })
        );


    }

    protected List getExtraInstallTasks(InstallContext installContext) {
        final List tasks = new ArrayList();
        tasks.add(new FilterOrderingTask("gzip", new String[]{"context", "multipartRequest", "activation"}));
        tasks.add(new FilterOrderingTask("cache", new String[]{"gzip"}));
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
