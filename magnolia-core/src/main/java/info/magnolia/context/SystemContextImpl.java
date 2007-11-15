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

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is the system context using the not secured HierarchyManagers. The context uses only one scope.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class SystemContextImpl extends AbstractContext implements SystemContext {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(SystemContextImpl.class);

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static ThreadLocal reposiotryStrategyThreadLocal = new ThreadLocal();

    /**
     * DON'T CREATE AN OBJECT. The SystemContext is set by magnolia system itself. Init the scopes
     */
    public SystemContextImpl() {
        setAttributeStrategy(new MapAttributeStrategy());
    }

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

    public RepositoryAcquiringStrategy getRepositoryStrategy() {
        if(reposiotryStrategyThreadLocal.get() == null){
            reposiotryStrategyThreadLocal.set(new SystemRepositoryStrategy(this));
        }
        return (RepositoryAcquiringStrategy) reposiotryStrategyThreadLocal.get();
    }

    public void release() {
        reposiotryStrategyThreadLocal.remove();
    }
        
    public Locale getLocale() { 
    	if (locale != null) 
    		return locale;
    	else
    		return Locale.getDefault();
    }
}
