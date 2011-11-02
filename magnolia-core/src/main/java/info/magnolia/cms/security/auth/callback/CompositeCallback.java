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
package info.magnolia.cms.security.auth.callback;

import info.magnolia.cms.util.PatternDelegate;
import info.magnolia.cms.util.UrlPatternDelegate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;


/**
 * A simple "composite" callback that delegates to other callbacks based on rules (a list of {@link PatternDelegate}).
 * It can be used to configured different callbacks for different urls, see this sample configuration for an example:
 *
 * <pre>
 * + clientCallback
 *    + patterns
 *      + admin
 *        + delegate
 *          - class      info.magnolia.cms.security.auth.callback.FormClientCallback
 *          - loginForm  /mgnl-resources/loginForm/login.html
 *          - realmName  Magnolia
 *        - class        info.magnolia.cms.util.UrlPatternDelegate
 *        - url          /.magnolia*
 *      + public
 *        + delegate
 *          - class      info.magnolia.cms.security.auth.callback.FormClientCallback
 *          - loginForm  /mgnl-resources/public-login.html
 *          - realmName  Magnolia
 *        - class        info.magnolia.cms.util.UrlPatternDelegate
 *        - url          /*
 *    - class            info.magnolia.cms.security.auth.callback.CompositeCallback
 * </pre>
 *
 * @author fgiust
 * @version $Id$
 * @deprecated since 4.5 - not needed anymore. The {@link info.magnolia.cms.security.SecurityCallbackFilter} can
 *             be configured to accept multiple {@link HttpClientCallback}s, and the callback themselves accept a request or not.
 */
public class CompositeCallback implements HttpClientCallback {

    private PatternDelegate[] patterns = new UrlPatternDelegate[0];

    @Override
    public boolean accepts(HttpServletRequest request) {
        for (PatternDelegate pattern : patterns) {
            if (pattern.match(request)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Delegates the processing to the first matching Callback in patterns.
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        for (PatternDelegate pattern : patterns) {
            if (pattern.match(request)) {
                ((HttpClientCallback) pattern.getDelegate()).handle(request, response);
                break;
            }
        }
    }

    // ----- configuration methods
    public PatternDelegate[] getPatterns() {
        return this.patterns;
    }

    public void addPattern(PatternDelegate pattern) {
        this.patterns = (PatternDelegate[]) ArrayUtils.add(this.patterns, pattern);
    }
}
