/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.cache.setup;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;


/**
 * Task that adds a cache voter to <code>/modules/cache/config/voters</code>.
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class AddCacheVoterTask extends AbstractRepositoryTask {

    private String name;

    private Class voterClass;

    private Map properties;

    public AddCacheVoterTask(String name, Class voterClass, Map properties) {
        super("New cache voter: " + name, "Adds the " + name + " cache voter");
        this.name = name;
        this.voterClass = voterClass;
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getConfigHierarchyManager();
        Content configNode = hm.getContent("/modules/cache/config");

        Content voters = ContentUtil.getOrCreateContent(configNode, "voters", ItemType.CONTENT);

        if (!voters.hasContent(name)) {
            Content m = voters.createContent(name, ItemType.CONTENTNODE);
            m.createNodeData("class").setValue(voterClass.getName());

            if (properties != null) {
                Iterator it = properties.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    m.createNodeData((String) entry.getKey(), entry.getValue());
                }
            }
        }
    }

}
