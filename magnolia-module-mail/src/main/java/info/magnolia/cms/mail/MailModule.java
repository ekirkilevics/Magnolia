/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.mail;

import info.magnolia.cms.core.Content;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MailModule implements ModuleLifecycle {
    private static final Logger log = LoggerFactory.getLogger(MailModule.class);

    public static final String SERVER_MAIL_CONFIG = "smtp";

    public static final String MAIL_TEMPLATES_PATH = "templates";

    private Content configNode;

    public Content getConfigNode() {
        return configNode;
    }

    public void setConfigNode(Content configNode) {
        this.configNode = configNode;
    }

    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        try {
            Content smtpNode = configNode.getContent(SERVER_MAIL_CONFIG);
            Content templateNode = configNode.getContent(MAIL_TEMPLATES_PATH);
            log.info("Loading mail server settings");
            MgnlMailFactory.getInstance().register(smtpNode);
            log.info("Loading mail templates");
            MgnlMailFactory.getInstance().register(templateNode);
        } catch (RepositoryException e) {
            log.error("Missing configuration for mail. Module is not properly initialized");
        }
    }

    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
    }
}
