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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.VirtualURIManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;

/**
 * A simple module which just set a default URI in a public instance from the module definition's
 * defaultPublicURI property.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 * @deprecated Since 3.5 please use the SimpleContentVersionHandler instead
 */
public class SimpleContentModule extends AbstractAdminModule {
    private static final String SERVER_ADMIN_NODEPATH = "/server/admin";
    private static final String DEFAULT_URI_NODEPATH = "/modules/adminInterface/virtualURIMapping/default";
    private static final String DEFAULT_URI_PROPNAME = "defaultPublicURI";

    protected void onInit() throws InitializationException {
        // nothing to do ...
    }

    protected void onRegister(int registerState) throws RegisterException {
        super.onRegister(registerState);

        // set the default URI to features.html if we're in a public instance
        if (isPublicInstance() && (registerState == REGISTER_STATE_INSTALLATION || registerState == REGISTER_STATE_NEW_VERSION)) {
            final String defaultPublicURI = getModuleDefinition().getProperty(DEFAULT_URI_PROPNAME);
            // TODO : inverse this check if this code is moved to AbstractAdminModule
            if (defaultPublicURI == null) {
                throw new RegisterException("Can't register module " + getName() + ", " + DEFAULT_URI_PROPNAME + " property is not set.");
            }
            setupDefaultPublicUri(defaultPublicURI);
        }
    }

    private void setupDefaultPublicUri(final String defaultPublicURI) throws RegisterException {
        try {
            final Content defaultUriNode = ContentUtil.getContent(ContentRepository.CONFIG, DEFAULT_URI_NODEPATH);
            final NodeData toUriData = defaultUriNode.getNodeData(VirtualURIManager.TO_URI_NODEDATANAME);
            toUriData.setValue(defaultPublicURI);
            toUriData.save();
        } catch (javax.jcr.RepositoryException e) {
            throw new RegisterException("Could not change the default URI: " + e.getMessage(), e);
        }
    }

    private boolean isPublicInstance() {
        final String isAdmin = NodeDataUtil.getString(ContentRepository.CONFIG, SERVER_ADMIN_NODEPATH);
        return !("true".equalsIgnoreCase(isAdmin));
    }
}
