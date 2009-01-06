/**
 * This file Copyright (c) 2007-2009 Magnolia International
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
import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AddURIPermissionTask;
import info.magnolia.module.delta.AllChildrenNodesOperation;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.LinkedHashMap;

import javax.jcr.RepositoryException;


/**
 * Checks for modifications between current secureURI configuration and the 3.0 default configuration.
 * Modified secureURIs are added as URI permissions to the anonymous user URI ACLs. <strong>If the default
 * secured URIs ("root" and "admininterface") had been removed, they are re-added (by means of the default
 * anonymous role's new permissions)
 * 
 * @author vsteller
 * @version $Id$
 */
public class CheckAndUpdateSecureURIs extends AllChildrenNodesOperation implements Task {

    private static final String PROPERTY_URI = "URI";
    private final LinkedHashMap secureURIs30 = new LinkedHashMap();
    private final ArrayDelegateTask subtasks;
    
    public CheckAndUpdateSecureURIs(String existingSecureURIs) {
        super("Secure URIs", "Backs up and removes secure ", ContentRepository.CONFIG, existingSecureURIs);
        subtasks = new ArrayDelegateTask("Secure URI transformations");
        
        // setup secureURIs from latest Magnolia 3.0.x installation
        secureURIs30.put("root", "/*");
        secureURIs30.put("admininterface", "/.magnolia*");
    }
    
    public void execute(InstallContext installContext) throws TaskExecutionException {
        super.execute(installContext);
        subtasks.execute(installContext);
    }

    protected void operateOnChildNode(Content node, InstallContext ctx) throws RepositoryException,
        TaskExecutionException {
        final String secureURIName = node.getName();
        final String secureURI = NodeDataUtil.getString(node, PROPERTY_URI);

        // ignore secureURIs from the secureURIs30 map
        if (!secureURIs30.containsKey(secureURIName) || !((String) secureURIs30.get(secureURIName)).equals(secureURI)) {
            subtasks.addTask(new AddURIPermissionTask("Permissions", "Transform secure URI " + secureURI + " to permission of anonymous role", UserManager.ANONYMOUS_USER, secureURI, AddURIPermissionTask.DENY));
            ctx.info("Existing configuration of secureURIList was modified. Magnolia put a backup in " + node.getHandle() + " and will add an URI restriction for the URI '" + secureURI + "' to the anonymous role.");
        }
    }
}
