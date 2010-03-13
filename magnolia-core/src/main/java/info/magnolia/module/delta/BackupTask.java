/**
 * This file Copyright (c) 2007-2010 Magnolia International
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
package info.magnolia.module.delta;

import info.magnolia.module.InstallContext;

import org.apache.commons.lang.StringUtils;


/**
 * Backs up a node in the same workspace under a specific backup path.
 * @author vsteller
 * @version $Id$
 */
public class BackupTask extends ArrayDelegateTask {

    private static final String DEFAULT_BACKUP_PATH = "/server/install/backup";

    private final String workspace;
    private final String path;
    private final boolean info;
    private String backupPath;

    public BackupTask(String workspace, String path) {
        this(workspace, path, false);
    }
    
    /**
     * @param workspace the workspace that contains path
     * @param path the path to the node that is to be backed up.
     * @param info indicates if an info message should be displayed.
     */
    public BackupTask(String workspace, String path, boolean info) {
        super("Backup", "Does a backup of the node path '" + path + "' in the " + workspace + " workspace.");
        this.workspace = workspace;
        this.path = path;
        this.info = info;

        final String parentPath = StringUtils.substringBeforeLast(path, "/");
        final String backupParentPath = getBackupPath() + parentPath;
        this.backupPath = backupParentPath + "/" + StringUtils.substringAfterLast(path, "/");
        final CreateNodePathTask backupParent = new CreateNodePathTask("Create node", "Creates the " + path + " backup location.", workspace, backupParentPath);
        final MoveNodeTask moveNodeToBackupPath = new MoveNodeTask("Move node", "Moves " + path + " to the " + backupPath + " backup location.", workspace, path, backupPath, true);
        addTask(backupParent);
        addTask(moveNodeToBackupPath);
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        super.execute(ctx);
        if (info) {
            ctx.info("Stored a backup of node " + workspace + ":" + path + " in " + backupPath + ".");
        }
    }

    protected String getBackupPath() {
        return DEFAULT_BACKUP_PATH;
    }
}
