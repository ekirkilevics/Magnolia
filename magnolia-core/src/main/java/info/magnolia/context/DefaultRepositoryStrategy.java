/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.context;

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.util.WorkspaceAccessUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class DefaultRepositoryStrategy extends AbstractRepositoryStrategy {
    static final Logger log = LoggerFactory.getLogger(DefaultRepositoryStrategy.class);

    private static final long serialVersionUID = 222L;


    private Map accessManagers = new HashMap();
    protected UserContext context;

    public DefaultRepositoryStrategy(UserContext context) {
        this.context = context;
    }


    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        AccessManager accessManager = null;

        final String amAttrName = repositoryId + "_" + workspaceId;
        accessManager = (AccessManager) accessManagers.get(amAttrName);

        if (accessManager == null) {
            accessManager = WorkspaceAccessUtil.getInstance().createAccessManager(getSubject(), repositoryId, workspaceId);
            accessManagers.put(amAttrName, accessManager);
        }

        return accessManager;
    }

    protected Subject getSubject() {
        return this.context.getUser().getSubject();
    }

    protected String getUserId() {
        return this.context.getUser().getName();
    }

}

