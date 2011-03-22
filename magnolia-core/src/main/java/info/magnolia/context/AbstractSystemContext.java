/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.context;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.i18n.MessagesManager;


/**
 * This is the system context using the not secured HierarchyManagers. The context uses only one scope.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractSystemContext extends AbstractContext implements SystemContext {

    private static final Logger log = LoggerFactory.getLogger(AbstractSystemContext.class);

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    protected static ThreadLocal<RepositoryAcquiringStrategy> repositoryStrategyThreadLocal = new ThreadLocal<RepositoryAcquiringStrategy>();

    /**
     * DON'T CREATE AN OBJECT. The SystemContext is set by magnolia system itself. Init the scopes
     */
    public AbstractSystemContext() {
        setAttributeStrategy(new MapAttributeStrategy());
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        if (scope == Context.LOCAL_SCOPE || scope == Context.SESSION_SCOPE) {
            log.warn("you should not set an attribute in the system context in request or session scope. You are setting {}={}", name, value);
        }
        super.setAttribute(name, value, scope);
    }

    public void removeAttribute(String name, Object value, int scope) {
        if (scope == Context.LOCAL_SCOPE || scope == Context.SESSION_SCOPE) {
            log.warn("you should not manipulate an attribute in the system context in request or session scope. You are setting {}={}", name, value);
        }
        super.removeAttribute(name, scope);
    }

    /**
     * @deprecated since 4.0 - this shouldn't be exposed in the SystemContext interface. Prevent calls by
     * throwing an UnsupportedOperationException.
     */
    @Deprecated
    @Override
    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException("setLocale() should not be called on SystemContext - system default locale is handled by MessagesManager");
    }

    // TODO - See MAGNOLIA-2531
    @Override
    public Locale getLocale() {
        return MessagesManager.getInstance().getDefaultLocale();
    }
}
