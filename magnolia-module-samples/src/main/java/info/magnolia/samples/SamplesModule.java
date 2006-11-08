/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.samples;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.VirtualURIManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.module.AbstractModule;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.module.InvalidConfigException;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SamplesModule extends AbstractModule {
    private static final String SERVER_ADMIN_NODEPATH = "/server/admin";
    private static final String DEFAULT_URI_NODEPATH = "/modules/adminInterface/virtualURIMapping/default";
    private static final String DEFAULT_URI_VALUE = "redirect:/features.html";

    protected void onRegister(int registerState) throws RegisterException {
        super.onRegister(registerState);

        // set the default URI to features.html if we're in a public instance
        if (isPublicInstance() && (registerState == REGISTER_STATE_INSTALLATION || registerState == REGISTER_STATE_NEW_VERSION)) {
            setupDefaultUriForSamples();
        }
    }

    private void setupDefaultUriForSamples() throws RegisterException {
        try {
            final Content defaultUriNode = ContentUtil.getContent(ContentRepository.CONFIG, DEFAULT_URI_NODEPATH);
            final NodeData toUriData = defaultUriNode.getNodeData(VirtualURIManager.TO_URI_NODEDATANAME);
            toUriData.setValue(DEFAULT_URI_VALUE);
            toUriData.save();
        } catch (javax.jcr.RepositoryException e) {
            throw new RegisterException("Could not change the default URI: " + e.getMessage(), e);
        }
    }

    public void init(Content configNode) throws InvalidConfigException, InitializationException {
        // nothing to do ...
    }

    private boolean isPublicInstance() {
        final String isAdmin = NodeDataUtil.getString(ContentRepository.CONFIG, SERVER_ADMIN_NODEPATH);
        return !("true".equalsIgnoreCase(isAdmin));
    }
}
