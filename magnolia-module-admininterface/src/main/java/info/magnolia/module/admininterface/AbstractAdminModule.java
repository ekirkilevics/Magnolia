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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.module.AbstractModule;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.util.ContentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation. registers dialogs , paragraphs, ...
 * @author philipp
 * @deprecated since 3.5 see info.magnolia.module
 */
public abstract class AbstractAdminModule extends AbstractModule {

    /**
     * Logger
     */
    protected Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Initialize the module. Registers the dialogs, paragraphs and templates of this modules. Calls the abstract onInit
     * method.
     * @throws InitializationException
     */
    public final void init(Content configNode) throws InitializationException {
        try {
            onInit();

            this.setInitialized(true);
        }
        catch (Throwable e) {
            throw new InitializationException("can't initialize module [" + this.getName() + "]", e);
        }
    }

    private void initEntry(String nodeName, ObservedManager manager) {
        Content node = ContentUtil.getCaseInsensitive(moduleNode, nodeName);
        if (node != null) {
            manager.register(node);
        }
    }

    /**
     * Template pattern. Implement to perfome somem module specific stuff
     */
    protected abstract void onInit() throws InitializationException;

}
