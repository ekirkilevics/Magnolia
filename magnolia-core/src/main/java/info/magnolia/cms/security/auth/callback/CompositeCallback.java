/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.security.auth.callback;

import info.magnolia.cms.util.PatternDelegate;
import info.magnolia.cms.util.UrlPatternDelegate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;


/**
 * <p>
 * A simple "composite" callback that delegates to other callbacks based on rules (a list of {@link PatternDelegate}).
 * </p>
 * <p>
 * It can be used to configured different callbacks for different urls, see this sample configuration for an example:
 * </p>
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
 *        - url          ./magnolia*
 *      + public
 *        + delegate
 *          - class      info.magnolia.cms.security.auth.callback.FormClientCallback
 *          - loginForm  /mgnl-resources/public-login.html
 *          - realmName  Magnolia
 *        - class        info.magnolia.cms.util.UrlPatternDelegate
 *        - url          /*
 *    - class            info.magnolia.cms.security.auth.callback.CompositeCallback
 * </pre>
 * @author fgiust
 * @version $Id$
 */
public class CompositeCallback implements HttpClientCallback {

    private PatternDelegate[] patterns = new UrlPatternDelegate[0];

    /**
     * Delegates the processing to the first matching Callback in patterns.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        for (int i = 0; i < patterns.length; i++) {
            PatternDelegate currentPattern = patterns[i];
            if (currentPattern.match(request)) {
                ((HttpClientCallback) currentPattern.getDelegate()).handle(request, response);
                break;
            }
        }
    }

    /**
     * @return array of configured PatternDelegate.
     */
    public PatternDelegate[] getPatterns() {
        return this.patterns;
    }

    /**
     * Adds a new PatternDelegate. Used by Content2Bean .
     * @param pattern PatternDelegate instance
     */
    public void addPattern(PatternDelegate pattern) {
        this.patterns = (PatternDelegate[]) ArrayUtils.add(this.patterns, pattern);
    }
}