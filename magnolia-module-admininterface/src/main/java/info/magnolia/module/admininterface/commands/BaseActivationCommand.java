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
package info.magnolia.module.admininterface.commands;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;


/**
 * @author Philipp Bracher
 * @version $Id$
 *
 */
public abstract class BaseActivationCommand extends RuleBasedCommand {

    /**
     * You can pass a syndicator to the command (optional)
     */
    public static final String ATTRIBUTE_SYNDICATOR = "syndicator";

    private Syndicator syndicator;

    public Syndicator getSyndicator() {
        // lazy bound, but only if this is a clone
        if (syndicator == null && isClone()) {
            syndicator = (Syndicator) FactoryUtil.newInstance(Syndicator.class);
            syndicator.init(
                MgnlContext.getUser(),
                this.getRepository(),
                ContentRepository.getDefaultWorkspace(this.getRepository()),
                getRule());
        }
        return syndicator;
    }

    /**
     * @param syndicator the syndicator to set
     */
    public void setSyndicator(Syndicator syndicator) {
        this.syndicator = syndicator;
    }

}
