/**
 * This file Copyright (c) 2007 Magnolia International
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
package info.magnolia.setup.for3_5;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.Task;

import javax.jcr.ImportUUIDBehavior;


/**
 * @author vsteller
 * @version $Id$
 *
 */
public class MigrateFilterConfiguration extends BootstrapConditionally {

    private static final String FILTER_BACKUP_PATH = "/server/install/backup/filters";

    public MigrateFilterConfiguration(String newFilterConfigurationBootstrapFile) {
        super("Filters", "Installs or updates the new filter (and secure/unsecure URIs) configuration.", newFilterConfigurationBootstrapFile, 
            new ArrayDelegateTask("Backup and transform existing filters", new Task[] {
                new CreateNodeTask("Backup", "Create install node", ContentRepository.CONFIG, "/server", "install", ItemType.CONTENT.getSystemName()),
                new CreateNodeTask("Backup", "Create backup node", ContentRepository.CONFIG, "/server/install", "backup", ItemType.CONTENT.getSystemName()),
                new MoveNodeTask("Filters", "Moves existing filter configuration to a backup location", ContentRepository.CONFIG, "/server/filters", FILTER_BACKUP_PATH, true),
                new BootstrapSingleResource("Bootstrap", "Bootstraps the new filter configuration", newFilterConfigurationBootstrapFile, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW), 
                new CheckAndUpdateExistingFilters(FILTER_BACKUP_PATH)
            }));
    }
}
